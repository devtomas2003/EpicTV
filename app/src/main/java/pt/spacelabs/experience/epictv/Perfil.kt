package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.Adapters.PlanAdapter
import pt.spacelabs.experience.epictv.entitys.Plan
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper

class Perfil : AppCompatActivity() {

    private var emailOld = "";
    private var nicknameOld = "";
    private var phoneinpOld = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.perfil)

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

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }

        val email = findViewById<EditText>(R.id.inp_email_perfil);
        val pass = findViewById<EditText>(R.id.inp_password_perfil);
        val repass = findViewById<EditText>(R.id.inp_repassword_perfil);
        val nickname = findViewById<EditText>(R.id.inp_name_perfil);
        val phoneinp = findViewById<EditText>(R.id.inp_phone_perfil);

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)


        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()

        if(validateFields(email, pass, repass, nickname, phoneinp)){
            val stringRequest = object : StringRequest(
                Method.POST,
                Constants.baseURL + "/updateProfile",
                Response.Listener { _ ->
                    try {
                        AlertDialog.Builder(this)
                            .setTitle("Sucesso")
                            .setMessage("Bem-vindo ${intent.getStringExtra("name")}, a tua conta foi atualizada com sucesso!")
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
        }

        val requestPlans = object : StringRequest(
            Method.GET,
            Constants.baseURL + "/profile",
            Response.Listener { response ->
                try {
                    val profileInfo = JSONObject(response)

                    emailOld = profileInfo.getString("email");
                    email.setText(emailOld)
                    nicknameOld = profileInfo.getString("name");
                    nickname.setText(nicknameOld)
                    phoneinpOld = profileInfo.getString("telef");
                    phoneinp.setText(phoneinpOld)

                    alertDialog.hide()
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
                alertDialog.hide()
                AlertDialog.Builder(this)
                    .setTitle("Ocorreu um erro")
                    .setMessage("Verifica a tua ligação com a internet!")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        ) {}

        requestPlans.retryPolicy = DefaultRetryPolicy(
            2000,
            2,
            1.0f
        )

        requestQueue.add(requestPlans)
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
}