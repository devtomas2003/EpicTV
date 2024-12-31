package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
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
import pt.spacelabs.experience.epictv.utils.Constants

class Catalog : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.catalog)

        enableImmersiveMode()

        val bgImage = findViewById<ImageView>(R.id.bgImage)
        val movieLogo = findViewById<ImageView>(R.id.movieLogo)
        val movieDescription = findViewById<TextView>(R.id.movieDescription)
        val startMovieBtn = findViewById<Button>(R.id.startMovieBtn)

        val queue = Volley.newRequestQueue(this)

        val getRandomContent = StringRequest(Request.Method.GET, Constants.baseURL + "/getRandomContent", { response ->
            val contentObject = JSONObject(response)

            movieDescription.text = contentObject.getString("description")

            Picasso.with(this)
                .load(Constants.contentURLPublic + contentObject.getString("miniPoster"))
                .into(movieLogo)

            Picasso.with(this)
                .load(Constants.contentURLPublic + contentObject.getString("poster"))
                .fit()
                .centerCrop()
                .into(bgImage)

            startMovieBtn.setOnClickListener {
                val intent = Intent (this, Player::class.java)
                intent.putExtra("manifestName", contentObject.getString("manifestName"))
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.homepage_menu).setOnClickListener{
                val intent = Intent(this, Catalog::class.java)
                startActivity(intent)
            }

            findViewById<ImageView>(R.id.personpage_menu).setOnClickListener{
                val intent = Intent(this, Perfil::class.java)
                startActivity(intent)
            }

        },
            { error ->
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Erro ao fazer request: ${error.message}")
                    .show()
            })

        queue.add(getRandomContent);
    }

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }
}