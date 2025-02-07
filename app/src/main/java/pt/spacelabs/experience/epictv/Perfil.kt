package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper

class Perfil : AppCompatActivity() {

    private var emailOld = "";
    private var nicknameOld = "";
    private var phoneinpOld = "";
    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.perfil)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()

        val switchButton = findViewById<SwitchCompat>(R.id.switchButton)

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchButton.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                switchButton.trackDrawable  = ContextCompat.getDrawable(this, R.drawable.color_blue)
            } else {
                switchButton.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                switchButton.trackDrawable  = ContextCompat.getDrawable(this, R.drawable.plan_selector_background)
            }
        }

        val logoutButton: ImageView = findViewById(R.id.logout_icon)

        logoutButton.setOnClickListener {
            DBHelper(this).clearConfig("token")

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.homepage_menu).setOnClickListener{
            if(isNetworkAvailable()){
                val intent = Intent(this, Catalog::class.java)
                startActivity(intent)
                finish()
            }else{
                AlertDialog.Builder(this)
                    .setTitle("Aviso")
                    .setMessage("Para usares esta funcionalidade, verifica a tua ligação!")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }

        findViewById<ImageView>(R.id.download_menu).setOnClickListener{
            val intent = Intent(this, Downloads::class.java)
            startActivity(intent)
            finish()
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val email = findViewById<EditText>(R.id.inp_email_perfil);
        val pass = findViewById<EditText>(R.id.inp_password_perfil);
        val repass = findViewById<EditText>(R.id.inp_repassword_perfil);
        val nickname = findViewById<EditText>(R.id.inp_name_perfil);
        val phoneinp = findViewById<EditText>(R.id.inp_phone_perfil);
        val btnSaveInfo = findViewById<Button>(R.id.btnSaveInfo)
        val btnBlue = findViewById<Button>(R.id.btnBlue)

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()

        btnBlue.setOnClickListener {
            val intent = Intent(this, Bluetooth::class.java)
            startActivity(intent)
        }

        btnSaveInfo.setOnClickListener {
            if(validateFields(email, pass, repass, nickname, phoneinp)){
                if(isNetworkAvailable()){
                    alertDialog.show()
                    val stringRequest = object : StringRequest(
                        Method.POST,
                        Constants.baseURL + "/updateProfile",
                        Response.Listener { _ ->
                            try {
                                DBHelper(this).updateConfig("email", email.text.toString())
                                DBHelper(this).updateConfig("name", nickname.text.toString())
                                DBHelper(this).updateConfig("telef", phoneinp.text.toString())

                                AlertDialog.Builder(this)
                                    .setTitle("Sucesso")
                                    .setMessage("A tua conta foi atualizada com sucesso!")
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.dismiss()
                                        alertDialog.hide()
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
                            body.put("nickname", nickname.text.toString())
                            body.put("telef", phoneinp.text.toString())
                            body.put("email", email.text.toString())
                            body.put("pass", pass.text.toString())
                            return body.toString().toByteArray(Charsets.UTF_8)
                        }
                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()
                            val auth = "Bearer " + DBHelper(this@Perfil).getConfig("token")
                            headers["Authorization"] = auth
                            return headers
                        }
                        override fun getBodyContentType(): String {
                            return "application/json; charset=UTF-8"
                        }
                    }
                    requestQueue.add(stringRequest)
                }else{
                    if(pass.text.isNotEmpty()){
                        AlertDialog.Builder(this)
                            .setTitle("Falha de ligação")
                            .setMessage("Para alterares a tua password, verifica a tua ligação!")
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .create()
                            .show()
                    }else{
                        DBHelper(this).updateConfig("email", email.text.toString())
                        DBHelper(this).updateConfig("name", nickname.text.toString())
                        DBHelper(this).updateConfig("telef", phoneinp.text.toString())
                        DBHelper(this).createConfig("haveToUpdateProfile", "yes")

                        AlertDialog.Builder(this)
                            .setTitle("Sucesso")
                            .setMessage("A tua conta foi atualizada com sucesso!")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                                alertDialog.hide()
                            }
                            .create()
                            .show()
                    }
                }
            }
        }

        emailOld = DBHelper(this).getConfig("email")
        email.setText(emailOld)
        nicknameOld = DBHelper(this).getConfig("name")
        nickname.setText(nicknameOld)
        phoneinpOld = DBHelper(this).getConfig("telef")
        phoneinp.setText(phoneinpOld)
    }

    private fun resetValues() {
        findViewById<EditText>(R.id.inp_email_perfil).setText(phoneinpOld)
        findViewById<EditText>(R.id.inp_password_perfil).setText("")
        findViewById<EditText>(R.id.inp_repassword_perfil).setText("")
        findViewById<EditText>(R.id.inp_name_perfil).setText(nicknameOld)
        findViewById<EditText>(R.id.inp_phone_perfil).setText(phoneinpOld)
    }

    private fun validateFields(
        email: EditText,
        pass: EditText,
        repass: EditText,
        nickname: EditText,
        phoneinp: EditText,
    ): Boolean {
        if (email.text.isEmpty() || !email.text.contains("@")) {
            resetValues()
            showAlert("Erro", "Por favor, coloque o seu email!")
            return false
        }
        if (pass.text.isNotEmpty() && pass.text.length < 8) {
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

    private fun enableImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread {
                    updateBehind()
                }
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
    }

    override fun onResume() {
        super.onResume()
        updateBehind()
    }

    fun updateBehind(){
        if(DBHelper(this).getConfig("haveToUpdateProfile") == "yes") {
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            val stringRequest = object : StringRequest(
                Method.POST,
                Constants.baseURL + "/updateProfile",
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status = jsonObject.getString("message")
                        if (status != "ok") {
                            AlertDialog.Builder(this@Perfil)
                                .setTitle("Ocorreu um erro")
                                .setMessage(jsonObject.getString("Message"))
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()
                        }else{
                            DBHelper(this@Perfil).clearConfig("haveToUpdateProfile")
                        }
                    } catch (e: JSONException) {
                    }
                },
                Response.ErrorListener { }
            ) {
                override fun getBody(): ByteArray {
                    val body = JSONObject()
                    body.put("nickname", DBHelper(this@Perfil).getConfig("name"))
                    body.put("telef", DBHelper(this@Perfil).getConfig("telef"))
                    body.put("email", DBHelper(this@Perfil).getConfig("email"))
                    body.put("pass", "")
                    return body.toString().toByteArray(Charsets.UTF_8)
                }

                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@Perfil).getConfig("token")
                    headers["Authorization"] = auth
                    return headers
                }

                override fun getBodyContentType(): String {
                    return "application/json; charset=UTF-8"
                }
            }

            requestQueue.add(stringRequest)
        }
    }
}