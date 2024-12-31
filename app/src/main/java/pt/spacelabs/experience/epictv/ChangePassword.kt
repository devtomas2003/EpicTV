package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.utils.Constants

class ChangePassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.changepass)

        val pass = findViewById<EditText>(R.id.firstpass)
        val repass = findViewById<EditText>(R.id.secundpass)

        val phoneNumber = intent.getStringExtra("phoneNumber")
        val tempCode = intent.getStringExtra("tempCode")

        findViewById<Button>(R.id.btnrecoverpass).setOnClickListener {
            if(pass.text.length < 8){
                pass.setText("")
                repass.setText("")
                showAlert("Erro", "A password têm de ter pelo menos 8 caracteres!");
            }else if(pass.text.toString() != repass.text.toString()){
                pass.setText("")
                repass.setText("")
                showAlert("Erro", "As passwords não são iguais!");
            }else{
                val requestQueue: RequestQueue = Volley.newRequestQueue(this)

                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
                val inflater = this.layoutInflater
                val dialogView: View = inflater.inflate(R.layout.loading, null)
                dialogBuilder.setView(dialogView)
                val alertDialog: AlertDialog = dialogBuilder.create()
                alertDialog.show()

                val stringRequest = object : StringRequest(
                    Method.POST,
                    Constants.baseURL + "/changeRecovery",
                    Response.Listener { response ->
                        try {
                            val jsonObject = JSONObject(response)
                            val email = jsonObject.getString("email")
                            AlertDialog.Builder(this)
                                .setTitle("Sucesso")
                                .setMessage("A password associada ao email $email foi alterada com sucesso!")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                    val intent = Intent (this, Login::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .create()
                                .show()
                        } catch (e: JSONException) {
                            alertDialog.hide()
                            pass.setText("")
                            repass.setText("")
                            AlertDialog.Builder(this)
                                .setTitle("Falha de ligação")
                                .setMessage("Ocorreu um erro com a resposta do servidor!")
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .create()
                                .show()
                        }
                    },
                    Response.ErrorListener { error ->
                        try {
                            alertDialog.hide()
                            pass.setText("")
                            repass.setText("")
                            val errorResponse =
                                String(error.networkResponse.data, Charsets.UTF_8)
                            val errorObject = JSONObject(errorResponse)
                            if (errorObject.has("message")) {
                                AlertDialog.Builder(this)
                                    .setTitle("Ocorreu um erro")
                                    .setMessage(errorObject.getString("message"))
                                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                    .create()
                                    .show()
                            }
                        } catch (e: Exception) {
                            alertDialog.hide()
                            pass.setText("")
                            repass.setText("")
                            AlertDialog.Builder(this)
                                .setTitle("Falha de ligação")
                                .setMessage("Para usares esta funcionalidade, verifica a tua ligação!")
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .create()
                                .show()
                        }
                    }
                ) {
                    override fun getBody(): ByteArray {
                        val body = JSONObject()
                        body.put("phoneNumber", phoneNumber)
                        body.put("tempCode", tempCode)
                        body.put("password", pass.text.toString())
                        return body.toString().toByteArray(Charsets.UTF_8)
                    }
                    override fun getBodyContentType(): String {
                        return "application/json; charset=UTF-8"
                    }
                }
                requestQueue.add(stringRequest)
            }
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }

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