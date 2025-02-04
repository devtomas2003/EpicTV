package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import pt.spacelabs.experience.epictv.Adapters.CategoryAdapter
import pt.spacelabs.experience.epictv.Adapters.OfflineItems
import pt.spacelabs.experience.epictv.entitys.Category
import pt.spacelabs.experience.epictv.entitys.Content
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper
import pt.spacelabs.experience.epictv.utils.DownloadService

class DetailContent : AppCompatActivity() {
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
            val intent = Intent(this, Catalog::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.personpage_menu).setOnClickListener{
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.download_menu).setOnClickListener{
            val intent = Intent(this, Downloads::class.java)
            startActivity(intent)
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val imgPoster = findViewById<ImageView>(R.id.imgPoster)
        val titlo = findViewById<TextView>(R.id.titlo)
        val description = findViewById<TextView>(R.id.descricao)
        val detail = findViewById<TextView>(R.id.detail)
        val movieBtn = findViewById<Button>(R.id.startMovie)
        val downloadBtn = findViewById<Button>(R.id.downloadBtn)

        val isAvailableOffline = intent.getStringExtra("movieId")
            ?.let { DBHelper(this).checkIfIsAvailableOffline(it) }

        if(!isAvailableOffline!!){
            downloadBtn.visibility = View.INVISIBLE
        }

        val getMovieInfo = StringRequest(
            Request.Method.GET, Constants.baseURL + "/movieDetail?movieId=" + intent.getStringExtra("movieId"), { response ->
            val movie = JSONObject(response)
            alertDialog.hide()

            movieBtn.setOnClickListener {
                val intent = Intent(this, Player::class.java)
                intent.putExtra("manifestName", movie.getString("manifestName"))
                intent.putExtra("contentType", "movie")
                startActivity(intent)
            }

            downloadBtn.setOnClickListener {
                val downloadIntent = Intent(this, DownloadService::class.java)
                downloadIntent.putExtra("manifestName", movie.getString("manifestName"))
                downloadIntent.putExtra("contentName", movie.getString("name"))
                DBHelper(this).createMovie(intent.getStringExtra("movieId"), movie.getString("name"), movie.getInt("duration"), movie.getString("duration"), movie.getString("poster"), 0)
                startForegroundService(downloadIntent)
            }

            titlo.text = movie.getString("name")
            description.text = movie.getString("description")
            detail.text = buildString {
                append(movie.getString("year"))
                append(" | ")
                append(movie.getString("age"))
                append("+ | ")
                append(movie.getString("duration"))
                append("mins | ")
                append(movie.getString("categories"))
            }

                Picasso.with(this)
                    .load(Constants.contentURLPublic + movie.getString("poster"))
                    .fit()
                    .centerCrop()
                    .into(imgPoster)
        },
            { error ->
                alertDialog.hide()
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Erro ao fazer request: ${error.message}")
                    .show()
            })

        queue.add(getMovieInfo);
    }

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }
}