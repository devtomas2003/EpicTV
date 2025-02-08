package pt.spacelabs.experience.epictv

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
import androidx.media3.ui.PlayerView
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper
import java.io.File


class Player : ComponentActivity() {
    private lateinit var player : ExoPlayer
    var statusShow = false;

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_SILENCE) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permissão necessária")
                    .setMessage("Para uma melhor experiência, ative a permissão para silenciar notificações.")
                    .setPositiveButton("Configurações") { _, _ ->
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.player)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivityForResult(intent, REQUEST_ENABLE_SILENCE)
        }else{
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        player = ExoPlayer.Builder(this).build()
        val playerItem = findViewById<PlayerView>(R.id.playerItem)
        val movieTitleView = findViewById<TextView>(R.id.movieTitle)
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val resizeBtn = findViewById<ImageView>(R.id.resizeBtn)

        movieTitleView.text = intent.getStringExtra("movieName")

        playerItem.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility -> movieTitleView.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.INVISIBLE; backBtn.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.INVISIBLE; resizeBtn.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.INVISIBLE })

        playerItem.player = player

        backBtn.setOnClickListener {
            onBackPressed()
        }

        resizeBtn.setOnClickListener {
            if(statusShow){
                statusShow = false;
                playerItem.resizeMode = RESIZE_MODE_FIT;
            }else{
                statusShow = true
                playerItem.resizeMode = RESIZE_MODE_ZOOM;
            }

        }

            val isAvailable = intent.getStringExtra("movieId")
                ?.let { DBHelper(this).checkIfIsAvailableOffline(it) }

            if(!isAvailable!!){
                val headers = mapOf(
                    "Authorization" to "Bearer " + DBHelper(this).getConfig("token")
                )
                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setDefaultRequestProperties(headers)

                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this, httpDataSourceFactory)
                val mediaItemBuilder = MediaItem.Builder()
                    .setUri(Constants.contentURLPrivate + "getManifest/" + intent.getStringExtra("manifestName") + "/" + intent.getStringExtra("contentType"))
                val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItemBuilder.build())
                player.setMediaSource(hlsMediaSource)
            }else{
                val chunks: List<String>? = intent.getStringExtra("movieId")
                    ?.let { DBHelper(this).getChunksByMovieId(it) }

                val m3u8Item = chunks?.find { it.endsWith(".m3u8", ignoreCase = true) }

                val manifestPath = "$filesDir/${m3u8Item}"
                val uri = Uri.fromFile(File(manifestPath))

                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)

                val mediaItem = MediaItem.fromUri(uri)
                val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)

                player.setMediaSource(hlsMediaSource)
            }


        player.prepare()
        player.play()
    }

    override fun onStart() {
        super.onStart()
        if (!player.isPlaying) {
            player.prepare()
        }
        player.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player.playWhenReady = false
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

    companion object {
        private const val REQUEST_ENABLE_SILENCE = 3
    }
}