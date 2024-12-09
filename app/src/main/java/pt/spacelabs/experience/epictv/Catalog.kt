package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.squareup.picasso.Picasso
import org.json.JSONObject
import pt.spacelabs.experience.epictv.entities.Content

class Catalog : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.catalog)

        val bgImage = findViewById<ImageView>(R.id.bgImage)
        val movieLogo = findViewById<ImageView>(R.id.movieLogo)
        val movieDescription = findViewById<TextView>(R.id.movieDescription)
        val startMovieBtn = findViewById<Button>(R.id.startMovieBtn)

        val queue = Volley.newRequestQueue(this)
        val urlApi = "https://api-master.epictv.spacelabs.pt/v1/"
        val urlContent = "https://vis-ipv-cda.epictv.spacelabs.pt/public/"

        val getRandomContent = StringRequest(Request.Method.GET, urlApi + "getRandomContent", { response ->
            val contentObject = JSONObject(response)

            val content = Content().apply {
                name = contentObject.getString("name")
                description = contentObject.getString("description")
                friendlyName = contentObject.getString("friendlyName")
                poster = contentObject.getString("poster")
                miniPoster = contentObject.getString("miniPoster")
            }

            movieDescription.text = content.description


            Picasso.with(this)
                .load(urlContent + content.miniPoster)
                .into(movieLogo)

            Picasso.with(this)
                .load(urlContent + content.poster)
                .fit()
                .centerCrop()
                .into(bgImage)

        },
            { error ->
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Erro ao fazer request: ${error.message}")
                    .show()
            })

        startMovieBtn.setOnClickListener {
            val intent = Intent (this, Player::class.java)
            startActivity(intent)
        }

        queue.add(getRandomContent);
    }

    val asd = setOf("sda", "sdfsfg")

    
}