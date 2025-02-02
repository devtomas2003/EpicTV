package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.spacelabs.experience.epictv.Adapters.CategoryAdapter
import pt.spacelabs.experience.epictv.Adapters.OfflineItems
import pt.spacelabs.experience.epictv.entitys.Category
import pt.spacelabs.experience.epictv.entitys.Content
import pt.spacelabs.experience.epictv.utils.DBHelper

class Downloads : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.downloads_page)

        enableImmersiveMode()

        val recyclerView: RecyclerView = findViewById(R.id.offlineItems)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val movies: List<Content> = DBHelper(this).getMovies()

        val adapter = OfflineItems(movies)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.homepage_menu).setOnClickListener{
            val intent = Intent(this, Catalog::class.java)
            startActivity(intent)
            finish()
        }

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
}