package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class SignUp : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        val intent = Intent(this, Welcome2::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.enter_animation_right, R.anim.exit_animation_right)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)

        handler.postDelayed(navigateRunnable, 5000)

        findViewById<Button>(R.id.button_aderir).setOnClickListener {
            val intent = Intent (this, Plan::class.java)
            startActivity(intent)
            finish()
        }
    }

}