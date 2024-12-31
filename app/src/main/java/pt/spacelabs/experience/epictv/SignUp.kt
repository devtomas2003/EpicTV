package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class SignUp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signup)

        val name = findViewById<EditText>(R.id.name)
        val email = findViewById<EditText>(R.id.email)
        val pass = findViewById<EditText>(R.id.pass)
        val repass = findViewById<EditText>(R.id.reppass)
        val nickname = findViewById<EditText>(R.id.nickname)
        val phoneinp = findViewById<EditText>(R.id.phoneinp)
        val chkTerms = findViewById<CheckBox>(R.id.ckbTerms)
        val planId = intent.getStringExtra("planId")

        findViewById<Button>(R.id.criarconta).setOnClickListener {
            if (validateFields(name, email, pass, repass, nickname, phoneinp, chkTerms)) {
                val intent = Intent(this, PayMethod::class.java)
                intent.putExtra("nickname", nickname.text.toString())
                intent.putExtra("name", name.text.toString())
                intent.putExtra("telef", phoneinp.text.toString())
                intent.putExtra("password", pass.text.toString())
                intent.putExtra("planId", planId)
                intent.putExtra("email", email.text.toString())

                startActivity(intent)
            }
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    private fun resetValues() {
        findViewById<EditText>(R.id.name).setText("")
        findViewById<EditText>(R.id.email).setText("")
        findViewById<EditText>(R.id.pass).setText("")
        findViewById<EditText>(R.id.reppass).setText("")
        findViewById<EditText>(R.id.nickname).setText("")
        findViewById<EditText>(R.id.phoneinp).setText("")
        findViewById<CheckBox>(R.id.ckbTerms).isChecked = false
    }

    private fun validateFields(
        name: EditText,
        email: EditText,
        pass: EditText,
        repass: EditText,
        nickname: EditText,
        phoneinp: EditText,
        chkTerms: CheckBox
    ): Boolean {
        if (name.text.isEmpty()) {
            resetValues()
            showAlert("Erro", "Por favor, coloque o seu nome!")
            return false
        }
        if (email.text.isEmpty() || !email.text.contains("@")) {
            resetValues()
            showAlert("Erro", "Por favor, coloque o seu email!")
            return false
        }
        if (pass.text.length < 8) {
            resetValues()
            showAlert("Erro", "Por favor, crie uma password com pelo menos 8 caracteres!")
            return false
        }
        if (pass.text.toString() != repass.text.toString()) {
            resetValues()
            showAlert("Erro", "As passwords não correspondem!")
            return false
        }
        if (phoneinp.text.length != 9) {
            resetValues()
            showAlert("Erro", "Por favor, coloque um telemovel válido!")
            return false
        }
        if (nickname.text.isEmpty()) {
            resetValues()
            showAlert("Erro", "Por favor, crie um nickname!")
            return false
        }
        if (nickname.text.length > 8) {
            resetValues()
            showAlert("Erro", "O nickname devera ter até 8 caracteres!")
            return false
        }
        if (!chkTerms.isChecked) {
            resetValues()
            showAlert("Erro", "Para avançar, por favor aceito os termos e condições!")
            return false
        }
        return true
    }

    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}