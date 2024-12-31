package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class RecoverPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.recoverpass)
        findViewById<Button>(R.id.btnrecoverpass).setOnClickListener {
            val intent = Intent(this, NumberVerification::class.java)
            startActivity(intent)
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}