package pt.spacelabs.experience.epictv

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import pt.spacelabs.experience.epictv.Adapters.CategoryAdapter
import pt.spacelabs.experience.epictv.entitys.Category
import pt.spacelabs.experience.epictv.utils.Constants

class Catalog : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
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

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()

        val recyclerView: RecyclerView = findViewById(R.id.categorias)
        recyclerView.layoutManager = LinearLayoutManager(this)
        var categoriesList = mutableListOf<Category>()

        val getRandomContent = StringRequest(Request.Method.GET, Constants.baseURL + "/getRandomContent", { response ->
            val contentObject = JSONObject(response)

            movieDescription.text = contentObject.getString("description")

            if(contentObject.getBoolean("isSerie")){
                startMovieBtn.text = "Ver Trailer";
            }else{
                startMovieBtn.text = "Ver Filme";
            }

            Picasso.with(this)
                .load(Constants.contentURLPublic + contentObject.getString("miniPoster"))
                .into(movieLogo)

            Picasso.with(this)
                .load(Constants.contentURLPublic + contentObject.getString("poster"))
                .fit()
                .centerCrop()
                .into(bgImage)

            alertDialog.hide()

            startMovieBtn.setOnClickListener {
                val intent = Intent (this, Player::class.java)
                intent.putExtra("manifestName", contentObject.getString("manifestName"))
                if(contentObject.getBoolean("isSerie")) {
                    intent.putExtra("contentType", "serieTrailer")
                }else{
                    intent.putExtra("contentType", "movie")
                }
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
                alertDialog.hide()
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Erro ao fazer request: ${error.message}")
                    .show()
            })

        val getCategories = StringRequest(Request.Method.GET, Constants.baseURL + "/catalog", { response ->
            val categories = JSONArray(response)

            for(index in 0 until categories.length()){
                val cateogoryObject = categories.getJSONObject(index)
                val category = Category(
                    id = cateogoryObject.getString("id"),
                    name = cateogoryObject.getString("name")
                )
                categoriesList.add(category)
            }


            val adapter = CategoryAdapter(categoriesList)
            recyclerView.adapter = adapter
        },
            { error ->
                alertDialog.hide()
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Erro ao fazer request: ${error.message}")
                    .show()
            })

        queue.add(getRandomContent);
        queue.add(getCategories);
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