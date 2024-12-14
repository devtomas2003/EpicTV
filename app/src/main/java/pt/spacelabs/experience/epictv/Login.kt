package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper


class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.login)

        val inpEmail = findViewById<EditText>(R.id.inpMail)
        val inpPass = findViewById<EditText>(R.id.inpPass)

        findViewById<Button>(R.id.buttonLogin).setOnClickListener {
            if(checkFields(inpEmail.text.toString(), inpPass.text.toString())){
                val requestQueue: RequestQueue = Volley.newRequestQueue(this)

                val stringRequest = object : StringRequest(
                    Method.GET,
                    Constants.baseURL + "/login",
                    Response.Listener { response ->
                        try {
                            val jsonObject = JSONObject(response)
                            val token = jsonObject.getString("token")
                            val isAdmin = jsonObject.getBoolean("isAdmin")
                            val have1080 = jsonObject.getBoolean("have1080")
                            val have4k = jsonObject.getBoolean("have4k")
                            val haveDownloads = jsonObject.getBoolean("haveDownloads")
                            val haveWatchShare = jsonObject.getBoolean("haveWatchShare")
                            val qtdProfiles = jsonObject.getInt("qtdProfiles")
                            DBHelper(this).createConfig("token", token)
                            DBHelper(this).createConfig("isAdmin", isAdmin.toString())
                            DBHelper(this).createConfig("have1080", have1080.toString())
                            DBHelper(this).createConfig("have4k", have4k.toString())
                            DBHelper(this).createConfig("haveDownloads", haveDownloads.toString())
                            DBHelper(this).createConfig("haveWatchShare", haveWatchShare.toString())
                            DBHelper(this).createConfig("qtdProfiles", qtdProfiles.toString())
                            val intent = Intent(this, Catalog::class.java)
                            startActivity(intent)
                            finish()
                        } catch (e: JSONException) {
                            inpEmail.setText("")
                            inpPass.setText("")
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
                            val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                            val errorObject = JSONObject(errorResponse)
                            inpEmail.setText("")
                            inpPass.setText("")
                            AlertDialog.Builder(this)
                                .setTitle("Ocorreu um erro")
                                .setMessage(errorObject.getString("message"))
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .create()
                                .show()

                        } catch (e: Exception) {
                            inpEmail.setText("")
                            inpPass.setText("")
                            AlertDialog.Builder(this)
                                .setTitle("Falha de ligação")
                                .setMessage("Para usares esta funcionalidade, verifica a tua ligação!")
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .create()
                                .show()
                        }
                    }
                ) {
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        val auth = "Basic " + Base64.encodeToString("${inpEmail.text}:${inpPass.text}".toByteArray(), Base64.NO_WRAP)
                        headers["Authorization"] = auth
                        return headers
                    }
                    override fun getBodyContentType(): String {
                        return "text/plain; charset=UTF-8"
                    }
                }

                requestQueue.add(stringRequest)
            }else{
                inpEmail.setText("")
                inpPass.setText("")
            }
        }

        findViewById<Button>(R.id.buttonCreateAccount).setOnClickListener {
            val intent = Intent(this, Plans::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun checkFields(txtMail: String, txtPassword: String): Boolean {
        if(txtMail.isEmpty() || !txtMail.contains("@")){
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
        if(txtPassword.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Erro")
                .setMessage("Por favor, preencha a password!")
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