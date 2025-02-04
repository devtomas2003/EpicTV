package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.menu)

        enableImmersiveMode()

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