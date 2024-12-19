package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.utils.Constants

class SignUp : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())

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

        findViewById<Button>(R.id.criarconta).setOnClickListener {
            val intent = Intent (this, PayMethod::class.java)
            startActivity(intent)
            //if(validateFields(name, email, pass, repass, nickname, phoneinp)){
            //    val requestQueue: RequestQueue = Volley.newRequestQueue(this)
//
            //    val stringRequest = object : StringRequest(
            //        Method.POST,
            //        Constants.baseURL + "/createAccount",
            //        Response.Listener { _ ->
            //            try {
            //                AlertDialog.Builder(this)
            //                    .setTitle("Sucesso")
            //                    .setMessage("Bem-vindo ${name.text}, a tua conta foi criada com sucesso!")
            //                    .setPositiveButton("OK") { dialog, _ ->
            //                        dialog.dismiss()
            //                        val intent = Intent (this, PayMethod::class.java)
            //                        startActivity(intent)
            //                        finish()
            //                    }
            //                    .create()
            //                    .show()
            //            } catch (e: JSONException) {
            //                email.setText("")
            //                name.setText("")
            //                pass.setText("")
            //                repass.setText("")
            //                nickname.setText("")
            //                phoneinp.setText("")
            //                AlertDialog.Builder(this)
            //                    .setTitle("Falha de ligação")
            //                    .setMessage("Ocorreu um erro com a resposta do servidor!")
            //                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            //                    .create()
            //                    .show()
            //            }
            //        },
            //        Response.ErrorListener { error ->
            //            try {
            //                val errorResponse =
            //                    String(error.networkResponse.data, Charsets.UTF_8)
            //                val errorObject = JSONObject(errorResponse)
            //                if (errorObject.has("message")) {
            //                    email.setText("")
            //                    name.setText("")
            //                    pass.setText("")
            //                    repass.setText("")
            //                    nickname.setText("")
            //                    phoneinp.setText("")
            //                    AlertDialog.Builder(this)
            //                        .setTitle("Ocorreu um erro")
            //                        .setMessage(errorObject.getString("message"))
            //                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            //                        .create()
            //                        .show()
            //                }
            //            } catch (e: Exception) {
            //                email.setText("")
            //                name.setText("")
            //                pass.setText("")
            //                repass.setText("")
            //                nickname.setText("")
            //                phoneinp.setText("")
            //                AlertDialog.Builder(this)
            //                    .setTitle("Falha de ligação")
            //                    .setMessage("Para usares esta funcionalidade, verifica a tua ligação!")
            //                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            //                    .create()
            //                    .show()
            //            }
            //        }
            //    ) {
            //        override fun getBody(): ByteArray {
            //            val body = JSONObject()
            //            body.put("nickname", nickname.text.toString())
            //            body.put("name", name.text.toString())
            //            body.put("telef", phoneinp.text.toString())
            //            body.put("password", pass.text.toString())
            //            body.put("planId", intent.getStringExtra("planId"))
            //            body.put("email", email.text.toString())
            //            return body.toString().toByteArray(Charsets.UTF_8)
            //        }
//
            //        override fun getBodyContentType(): String {
            //            return "application/json; charset=UTF-8"
            //        }
            //    }
            //    requestQueue.add(stringRequest)
            //}
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }

    private fun validateFields(name: EditText, email: EditText, pass: EditText, repass: EditText, nickname: EditText, phoneinp: EditText): Boolean{
        if (name.text.isEmpty()) {
            email.setText("")
            pass.setText("")
            repass.setText("")
            nickname.setText("")
            phoneinp.setText("")
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Por favor, coloque o seu nome!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false;
        }
        if (email.text.isEmpty() || !email.text.contains("@")) {
            email.setText("")
            name.setText("")
            pass.setText("")
            repass.setText("")
            nickname.setText("")
            phoneinp.setText("")
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Por favor, coloque o seu email!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false
        }
        if (pass.text.length < 8) {
            email.setText("")
            name.setText("")
            pass.setText("")
            repass.setText("")
            nickname.setText("")
            phoneinp.setText("")
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Por favor, crie uma password com pelo menos 8 caracteres!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false
        }
        if (pass.text.toString() != repass.text.toString()) {
            email.setText("")
            name.setText("")
            pass.setText("")
            repass.setText("")
            nickname.setText("")
            phoneinp.setText("")
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("As passwords não correspondem!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false
        }
        if(phoneinp.text.length !== 9) {
            name.setText("")
            email.setText("")
            pass.setText("")
            repass.setText("")
            nickname.setText("")
            phoneinp.setText("")
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Por favor, coloque um telemovel válido!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false
        }
        if(nickname.text.isEmpty()) {
            name.setText("")
            email.setText("")
            pass.setText("")
            repass.setText("")
            nickname.setText("")
            phoneinp.setText("")
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Por favor, crie um nickname!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false
        }
        if(nickname.text.length > 8) {
            name.setText("")
            email.setText("")
            pass.setText("")
            repass.setText("")
            nickname.setText("")
            phoneinp.setText("")
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("O nickname devera ter até 8 caracteres!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            return false
        }
        return true
    }
}