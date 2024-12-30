package pt.spacelabs.experience.epictv

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper


class Player : ComponentActivity() {
    private lateinit var player : ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.player)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }else{
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        player = ExoPlayer.Builder(this).build()
        val playerItem = findViewById<PlayerView>(R.id.playerItem)

        playerItem.player = player

        val dbHelper = DBHelper(this)

        val headers = mapOf(
            "Authorization" to "Bearer " + DBHelper(this).getConfig("token")
        )
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(headers)

        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        val mediaItemBuilder = MediaItem.Builder()
            .setUri(Constants.contentURLPrivate + "getManifest/" + intent.getStringExtra("manifestName"))

        val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItemBuilder.build())
        player.setMediaSource(hlsMediaSource)
        //}

        player.prepare()
        player.play()
    }

    override fun onStart() {
        super.onStart()
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.playWhenReady = false
        player.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }
    private fun enterImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enterImmersiveMode()
        }
    }
}