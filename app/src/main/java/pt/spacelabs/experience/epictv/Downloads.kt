package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Downloads : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.downloads_page)

        findViewById<ImageView>(R.id.homepage_menu).setOnClickListener {
            val intent = Intent(this, Catalog::class.java)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.personpage_menu).setOnClickListener {
            val intent = Intent(this, Perfil::class.java)
            startActivity(intent)
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}