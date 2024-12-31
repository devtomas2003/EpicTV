package pt.spacelabs.experience.epictv

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Perfil : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.perfil)

        val switchButton = findViewById<Switch>(R.id.switchButton)

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //Quando ativado (Ligado)
                switchButton.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                switchButton.trackDrawable  = ContextCompat.getDrawable(this, R.drawable.color_blue)
            } else {
                //Quando desativado (Desligado)
                switchButton.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                switchButton.trackDrawable  = ContextCompat.getDrawable(this, R.drawable.plan_selector_background)
            }
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}