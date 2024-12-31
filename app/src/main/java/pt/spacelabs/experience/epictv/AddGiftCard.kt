package pt.spacelabs.experience.epictv

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.utils.Constants

class AddGiftCard : ComponentActivity() {

    private val qrCodeReaderLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val scannedResult = result.data?.getStringExtra("SCANNED_RESULT")
                if (scannedResult != null) {
                    CreateAccount(scannedResult)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.addgiftcard)

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        findViewById<Button>(R.id.ler_qr).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(this, QrCodeReader::class.java)
                qrCodeReaderLauncher.launch(intent)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        }

        val giftCardCode = findViewById<EditText>(R.id.code_input_giftcard)
        val giftCardBtn = findViewById<Button>(R.id.resgatarcode)

        giftCardBtn.setOnClickListener {
            if(giftCardCode.text.isEmpty()){
                AlertDialog.Builder(this)
                    .setTitle("Erro")
                    .setMessage("Por favor, preencha o codigo do Gift Card!")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }else{
                CreateAccount(giftCardCode.text.toString())
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, QrCodeReader::class.java)
                qrCodeReaderLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun CreateAccount(giftCard: String){
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()

        val stringRequest = object : StringRequest(
            Method.POST,
            Constants.baseURL + "/createAccount",
            Response.Listener { _ ->
                try {
                    AlertDialog.Builder(this)
                        .setTitle("Sucesso")
                        .setMessage("Bem-vindo ${intent.getStringExtra("name")}, a tua conta foi criada com sucesso!")
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
                body.put("nickname", intent.getStringExtra("nickname"))
                body.put("name", intent.getStringExtra("name"))
                body.put("telef", intent.getStringExtra("telef"))
                body.put("password", intent.getStringExtra("password"))
                body.put("planId", intent.getStringExtra("planId"))
                body.put("email", intent.getStringExtra("email"))
                body.put("giftcard", giftCard)
                return body.toString().toByteArray(Charsets.UTF_8)
            }
            override fun getBodyContentType(): String {
                return "application/json; charset=UTF-8"
            }
        }
        requestQueue.add(stringRequest)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
}