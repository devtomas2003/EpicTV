package pt.spacelabs.experience.epictv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class AddCreditCard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.addcreditcard)
    }
}