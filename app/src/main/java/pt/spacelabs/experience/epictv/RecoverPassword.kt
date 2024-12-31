package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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

class RecoverPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.recoverpass)
        val phoneInp = findViewById<EditText>(R.id.phoneinp)

        findViewById<Button>(R.id.btnrecoverpass).setOnClickListener {
            if(phoneInp.text.length !== 9){
                AlertDialog.Builder(this)
                    .setTitle("Erro")
                    .setMessage("Por favor, indique um numero de telemóvel válido!")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
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
                    Constants.baseURL + "/recovery",
                    Response.Listener { response ->
                        try {
                            val intent = Intent(this, NumberVerification::class.java)
                            intent.putExtra("phoneNumber", phoneInp.text)
                            startActivity(intent)
                        } catch (e: JSONException) {
                            alertDialog.hide()
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
                            val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                            val errorObject = JSONObject(errorResponse)
                            AlertDialog.Builder(this)
                                .setTitle("Ocorreu um erro")
                                .setMessage(errorObject.getString("message"))
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .create()
                                .show()

                        } catch (e: Exception) {
                            alertDialog.hide()
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
                        body.put("phoneNumber", phoneInp.text)
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
}