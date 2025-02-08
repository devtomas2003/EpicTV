package pt.spacelabs.experience.epictv

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.adapters.CategoryAdapter
import pt.spacelabs.experience.epictv.entitys.Category
import pt.spacelabs.experience.epictv.entitys.Content
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper

class Catalog : AppCompatActivity() {
    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.catalog)

        enableImmersiveMode()

        val queue = Volley.newRequestQueue(this)

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()

        val localMovies = DBHelper(this).getMovies(false)

        localMovies.forEach { movieLocal ->
            val getMovieInfo = StringRequest(
                Request.Method.GET, Constants.baseURL + "/movieDetail?movieId=" + movieLocal.id, { response ->
                    val movieDB = JSONObject(response)

                    if(movieDB.getBoolean("isActive")){
                        DBHelper(this).updateMovieData(movieLocal.id, movieDB.getString("name"), movieDB.getInt("duration"), movieDB.getString("description"), movieDB.getString("poster"), movieDB.getBoolean("isActive"))
                    }else{
                        DBHelper(this).deleteMovie(movieLocal.id)
                        try {
                            val chunksList = DBHelper(this).getChunksByMovieId(movieLocal.id)

                            chunksList.forEach { chunkData ->
                                deleteFile(chunkData)
                            }

                            Toast.makeText(this, "Foram removidos conteudos offline, devido a alterações de politicas.", Toast.LENGTH_SHORT).show()

                            DBHelper(this).deleteMovieChunks(movieLocal.id)
                        } catch (e: Exception) {
                            Log.e("test", "Error fetching chunks: ${e.message}", e)
                        }
                    }

                },
                { error ->
                    alertDialog.hide()
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Erro ao fazer request: ${error.message}")
                        .show()
                })

            queue.add(getMovieInfo)
        }

        val profileReq = object : StringRequest(
            Method.GET,
            Constants.baseURL + "/profile",
            Response.Listener { response ->
                try {
                    val profileInfo = JSONObject(response)

                    if(DBHelper(this).getConfig("email") == "none"){
                        DBHelper(this).createConfig("email", profileInfo.getString("email"))
                        DBHelper(this).createConfig("name", profileInfo.getString("name"))
                        DBHelper(this).createConfig("telef", profileInfo.getString("telef"))
                    }else{
                        DBHelper(this).updateConfig("email", profileInfo.getString("email"))
                        DBHelper(this).updateConfig("name", profileInfo.getString("name"))
                        DBHelper(this).updateConfig("telef", profileInfo.getString("telef"))
                        DBHelper(this).updateConfig("haveDownloads", profileInfo.getBoolean("haveDownloads").toString())

                        if(!profileInfo.getBoolean("haveDownloads")){
                            val localMovies = DBHelper(this).getMovies(false)

                            localMovies.forEach { movieLocal ->
                                DBHelper(this).deleteMovie(movieLocal.id)
                                try {
                                    val chunksList = DBHelper(this).getChunksByMovieId(movieLocal.id)

                                    chunksList.forEach { chunkData ->
                                        deleteFile(chunkData)
                                    }

                                    Toast.makeText(this, "Foram removidos conteudos offline, devido a alterações de politicas.", Toast.LENGTH_SHORT).show()

                                    DBHelper(this).deleteMovieChunks(movieLocal.id)
                                } catch (e: Exception) {
                                    Log.e("test", "Error fetching chunks: ${e.message}", e)
                                }
                            }
                        }
                    }

                    alertDialog.hide()
                } catch (e: JSONException) {
                    alertDialog.hide()
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Falha de ligação")
                        .setMessage("Ocorreu um erro com a resposta do servidor!")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                }
            },
            Response.ErrorListener { error ->
                alertDialog.hide()
                android.app.AlertDialog.Builder(this)
                    .setTitle("Ocorreu um erro")
                    .setMessage("Verifica a tua ligação com a internet!")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val auth = "Bearer " + DBHelper(this@Catalog).getConfig("token")
                headers["Authorization"] = auth
                return headers
            }
            override fun getBodyContentType(): String {
                return "application/json; charset=UTF-8"
            }
        }

        profileReq.retryPolicy = DefaultRetryPolicy(
            2000,
            2,
            1.0f
        )

        queue.add(profileReq)

        val bgImage = findViewById<ImageView>(R.id.bgImage)
        val movieLogo = findViewById<ImageView>(R.id.movieLogo)
        val startMovieBtn = findViewById<Button>(R.id.startMovieBtn)
        val detailMovieBtn = findViewById<Button>(R.id.detailMovieBtn)

        val recyclerView: RecyclerView = findViewById(R.id.categorias)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false
        var categoriesList = mutableListOf<Category>()

        val getRandomContent = StringRequest(Request.Method.GET, Constants.baseURL + "/getRandomContent", { response ->
            val contentObject = JSONObject(response)

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

            startMovieBtn.setOnClickListener {
                val intent = Intent (this, Player::class.java)
                intent.putExtra("manifestName", contentObject.getString("manifestName"))
                if(contentObject.getBoolean("isSerie")) {
                    intent.putExtra("movieName", "Trailer - " + contentObject.getString("name"))
                    intent.putExtra("contentType", "serieTrailer")
                }else{
                    intent.putExtra("contentType", "movie")
                    intent.putExtra("movieName", contentObject.getString("name"))
                }
                intent.putExtra("movieId", contentObject.getString("id"))
                startActivity(intent)
            }

            detailMovieBtn.setOnClickListener {
                val intent = Intent(this, DetailContent::class.java)
                intent.putExtra("movieId", contentObject.getString("id"))
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
            alertDialog.hide()

            for(index in 0 until categories.length()){
                val categoryObject = categories.getJSONObject(index)
                val listContentApi = categoryObject.getJSONArray("Content")
                var contentList = mutableListOf<Content>()

                for (a in 0 until listContentApi.length()) {
                    val contentObject = listContentApi.getJSONObject(a)

                    if(contentObject.getBoolean("isActive")){
                        val content = Content(
                            id = contentObject.getString("id"),
                            poster = contentObject.getString("poster"),
                            description = contentObject.getString("description"),
                            time = contentObject.getInt("duration"),
                            name = contentObject.getString("name"),
                            isActive = contentObject.getBoolean("isActive")
                        )
                        contentList.add(content)
                    }
                }

                val category = Category(
                    id = categoryObject.getString("id"),
                    name = categoryObject.getString("name"),
                    contents = contentList
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

        findViewById<ImageView>(R.id.personpage_menu).setOnClickListener{
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.download_menu).setOnClickListener{
            val intent = Intent(this, Downloads::class.java)
            startActivity(intent)
            finish()
        }
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

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread {
                    updateBehind()
                }
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
    }

    override fun onResume() {
        super.onResume()
        updateBehind()
    }

    fun updateBehind(){
        if(DBHelper(this).getConfig("haveToUpdateProfile") == "yes") {
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            val stringRequest = object : StringRequest(
                Method.POST,
                Constants.baseURL + "/updateProfile",
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status = jsonObject.getString("message")
                        if (status != "ok") {
                            AlertDialog.Builder(this)
                                .setTitle("Ocorreu um erro")
                                .setMessage(jsonObject.getString("message"))
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        }else{
                            DBHelper(this).clearConfig("haveToUpdateProfile")
                        }
                    } catch (e: JSONException) {}
                },
                Response.ErrorListener { error ->
                    val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                    val errorObject = JSONObject(errorResponse)

                    AlertDialog.Builder(this)
                        .setTitle("Erro de atualização")
                        .setMessage(errorObject.getString("message"))
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                }
            ) {
                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("nickname", DBHelper(this@Catalog).getConfig("name"))
                    body.put("telef", DBHelper(this@Catalog).getConfig("telef"))
                    body.put("email", DBHelper(this@Catalog).getConfig("email"))
                    body.put("pass", "")
                    return body.toString().toByteArray(Charsets.UTF_8)
                }

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@Catalog).getConfig("token")
                    headers["Authorization"] = auth
                    return headers
                }

                override fun getBodyContentType(): String {
                    return "application/json; charset=UTF-8"
                }
            }

            requestQueue.add(stringRequest)
        }
    }
}