package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class NumberVerification : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.numberverification)

        val input1: EditText = findViewById(R.id.input1)
        val input2: EditText = findViewById(R.id.input2)
        val input3: EditText = findViewById(R.id.input3)
        val input4: EditText = findViewById(R.id.input4)
        val input5: EditText = findViewById(R.id.input5)
        val input6: EditText = findViewById(R.id.input6)
        val input7: EditText = findViewById(R.id.input7)

        val inputs = arrayOf(input1, input2, input3, input4, input5, input6, input7)

        for (i in inputs.indices) {
            inputs[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < inputs.size - 1) {
                        inputs[i + 1].requestFocus()
                    } else if (s?.isEmpty() == true && i > 0) {
                        inputs[i - 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val phoneNumber = intent.getStringExtra("phoneNumber")

        findViewById<Button>(R.id.btnrecoverpass).setOnClickListener {
            val code = inputs.joinToString("") { it.text.toString() }

            val intent = Intent(this, ChangePassword::class.java)
            intent.putExtra("phoneNumber", phoneNumber)
            intent.putExtra("tempCode", code)
            startActivity(intent)
        }
    }
}
