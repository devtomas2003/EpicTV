package pt.spacelabs.experience.epictv

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ChangePassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.changepass)

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }

    }
}