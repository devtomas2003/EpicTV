package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.spacelabs.experience.epictv.adapters.OfflineItems
import pt.spacelabs.experience.epictv.entitys.Content
import pt.spacelabs.experience.epictv.utils.DBHelper

class Downloads : AppCompatActivity() {
    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.downloads_page)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        enableImmersiveMode()

        val recyclerView: RecyclerView = findViewById(R.id.offlineItems)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false

        val movies: MutableList<Content> = DBHelper(this).getMovies(true)
        val txtOfflineAlert = findViewById<TextView>(R.id.offlineAlert)

        if(movies.size != 0){
            txtOfflineAlert.visibility = View.INVISIBLE
        }

        val adapter = OfflineItems(movies)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.homepage_menu).setOnClickListener{
            if(isNetworkAvailable()){
                val intent = Intent(this, Catalog::class.java)
                startActivity(intent)
                finish()
            }else{
                AlertDialog.Builder(this)
                    .setTitle("Aviso")
                    .setMessage("Para usares esta funcionalidade, verifica a tua ligação!")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }

        findViewById<ImageView>(R.id.personpage_menu).setOnClickListener{
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
            finish()
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            if(isNetworkAvailable()){
                val intent = Intent(this, Catalog::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}