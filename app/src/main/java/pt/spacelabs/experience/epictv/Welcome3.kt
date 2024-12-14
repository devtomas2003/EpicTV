package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import kotlin.math.abs

class Welcome3 : ComponentActivity() {

    private var x1: Float = 0f
    private var y1: Float = 0f
    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        val intent = Intent(this, Welcome1::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.enter_animation_right, R.anim.exit_animation_right)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.welcome3)

        handler.postDelayed(navigateRunnable, 5000)
        findViewById<Button>(R.id.button_aderir).setOnClickListener {
            val intent = Intent (this, SignUp::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.button_login).setOnClickListener {
            val intent = Intent ( this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
            }
            MotionEvent.ACTION_UP -> {
                val x2 = event.x
                val y2 = event.y

                val deltaX = x2 - x1
                val deltaY = y2 - y1

                if (abs(deltaX) > abs(deltaY)) {
                    if (deltaX > 0) {
                        val intent = Intent(this, Welcome2::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.enter_animation_left, R.anim.exit_animation_left)
                        finish()
                    } else {
                        val intent = Intent(this, Welcome1::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.enter_animation_right, R.anim.exit_animation_right)
                        finish()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(navigateRunnable)
    }
}