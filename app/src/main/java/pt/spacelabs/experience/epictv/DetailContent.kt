package pt.spacelabs.experience.epictv

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper
import pt.spacelabs.experience.epictv.utils.DownloadService

class DetailContent : AppCompatActivity() {
    private var currentMovie: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.detailcontent)

        enableImmersiveMode()

        val queue = Volley.newRequestQueue(this)

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()

        findViewById<ImageView>(R.id.homepage_menu).setOnClickListener{
            startActivity(Intent(this, Catalog::class.java))
        }

        findViewById<ImageView>(R.id.personpage_menu).setOnClickListener{
            startActivity(Intent(this, Perfil::class.java))
        }

        findViewById<ImageView>(R.id.download_menu).setOnClickListener{
            startActivity(Intent(this, Downloads::class.java))
        }

        findViewById<ImageView>(R.id.arrowpageback).setOnClickListener {
            onBackPressed()
        }

        val imgPoster = findViewById<ImageView>(R.id.imgPoster)
        val titlo = findViewById<TextView>(R.id.titlo)
        val description = findViewById<TextView>(R.id.descricao)
        val detail = findViewById<TextView>(R.id.detail)
        val downloadBtn = findViewById<ImageView>(R.id.downloadBtn)
        val trailerBtn = findViewById<Button>(R.id.trailerBtn)
        val startWatch = findViewById<Button>(R.id.startMovie)

        val isAvailableOffline = intent.getStringExtra("movieId")
            ?.let { DBHelper(this).checkIfIsAvailableOffline(it) }

        if (isAvailableOffline!! || !DBHelper(this).getConfig("haveDownloads").toBoolean()) {
            downloadBtn.visibility = View.INVISIBLE
        }

        val getMovieInfo = StringRequest(
            Request.Method.GET, Constants.baseURL + "/movieDetail?movieId=" + intent.getStringExtra("movieId"), { response ->
                val movie = JSONObject(response)
                alertDialog.dismiss()
                currentMovie = JSONObject(response)

                startWatch.setOnClickListener {
                    val intent = Intent(this, Player::class.java)
                    intent.putExtra("manifestName", movie.getString("manifestName"))
                    intent.putExtra("contentType", "movie")
                    intent.putExtra("movieId", movie.getString("id"))
                    intent.putExtra("movieName", movie.getString("name"))
                    startActivity(intent)
                }

                downloadBtn.setOnClickListener {
                    checkNotificationPermissionAndStartService(movie)
                }

                trailerBtn.setOnClickListener {
                    val intent = Intent(this, Player::class.java)
                    intent.putExtra("manifestName", movie.getString("trailerManifest"))
                    intent.putExtra("movieId", movie.getString("trailerManifest"))
                    intent.putExtra("contentType", "trailer")
                    intent.putExtra("movieName", "Trailer - " + movie.getString("name"))
                    startActivity(intent)
                }

                titlo.text = movie.getString("name")
                description.text = movie.getString("description")
                detail.text = "${movie.getString("year")} | ${movie.getString("age")}+ | ${movie.getString("duration")} mins | ${movie.getString("categories")}"

                Picasso.with(this)
                    .load(Constants.contentURLPublic + movie.getString("poster"))
                    .fit()
                    .centerCrop()
                    .into(imgPoster)
            },
            { error ->
                alertDialog.dismiss()
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Erro ao fazer request: ${error.message}")
                    .show()
            })

        queue.add(getMovieInfo)
    }

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun checkNotificationPermissionAndStartService(movie: JSONObject) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                startDownloadService(movie)
            } else {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATIONS)
            }
        } else {
            startDownloadService(movie)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                currentMovie?.let { startDownloadService(it) }
            }
        }
    }

    private fun startDownloadService(movie: JSONObject) {
        if(DBHelper(this).getConfig("downloadUnderway") == "none") {
            DBHelper(this).createConfig("downloadUnderway", "true")

            val downloadIntent = Intent(this, DownloadService::class.java).apply {
                putExtra("manifestName", movie.getString("id"))
                putExtra("contentName", movie.getString("name"))
                putExtra("imageUrl", movie.getString("poster"))
            }

            DBHelper(this).createMovie(
                intent.getStringExtra("movieId") ?: "",
                movie.getString("name"),
                movie.optInt("duration", 0),
                movie.getString("description"),
                movie.getString("poster"),
                0,
                movie.getBoolean("isActive")
            )

            Toast.makeText(this, "O download vai iniciar dentro de momentos.", Toast.LENGTH_SHORT).show()

            startForegroundService(downloadIntent)
        }else{
            android.app.AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setMessage("Não podes ter varios downloads em simultaneo!")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    companion object {
        private const val REQUEST_CODE_NOTIFICATIONS = 1001
    }
}