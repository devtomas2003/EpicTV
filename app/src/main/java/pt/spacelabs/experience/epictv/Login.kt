package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.login)

        findViewById<Button>(R.id.buttonLogin).setOnClickListener {
            val intent = Intent(this, Catalog::class.java)
            startActivity(intent)
            finish()
        }
    }
}