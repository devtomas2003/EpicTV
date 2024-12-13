package pt.spacelabs.experience.epictv

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class SignUp : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)

        findViewById<Button>(R.id.criarconta).setOnClickListener {
            val intent = Intent (this, Plan::class.java)
            startActivity(intent)
            finish()
        }
    }

}