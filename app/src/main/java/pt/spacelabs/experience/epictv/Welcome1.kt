package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper
import kotlin.math.abs

class Welcome1 : ComponentActivity() {

    private var x1: Float = 0f
    private var y1: Float = 0f
    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        val intent = Intent(this, Welcome2::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.enter_animation_right, R.anim.exit_animation_right)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.welcome1)

        if(DBHelper(this).getConfig("token") != "none"){
            val requestQueue: RequestQueue = Volley.newRequestQueue(this)

            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.loading, null)
            dialogBuilder.setView(dialogView)
            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.show()

            val stringRequest = object : StringRequest(
                Method.GET,
                Constants.baseURL + "/checkAuth",
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status = jsonObject.getString("message")
                        if(status == "ok"){
                            val intent = Intent(this, Catalog::class.java)
                            startActivity(intent)
                            finish()
                        }
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
                        val errorResponse = String(error.networkResponse.data, Charsets.UTF_8)
                        val errorObject = JSONObject(errorResponse)
                        if(errorObject.has("message")){
                            DBHelper(this).clearConfig("token");
                        }
                        alertDialog.hide()
                    } catch (e: Exception) {
                        alertDialog.hide()
                    }
                }
            ) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    val auth = "Bearer " + DBHelper(this@Welcome1).getConfig("token")
                    headers["Authorization"] = auth
                    return headers
                }
                override fun getBodyContentType(): String {
                    return "text/plain; charset=UTF-8"
                }
            }

            requestQueue.add(stringRequest)
        }else{
            handler.postDelayed(navigateRunnable, 5000)
            findViewById<Button>(R.id.button_aderir).setOnClickListener {
                val intent = Intent (this, Plans::class.java)
                handler.removeCallbacks(navigateRunnable)
                startActivity(intent)
            }

            findViewById<Button>(R.id.button_login).setOnClickListener {
                val intent = Intent (this, Login::class.java)
                handler.removeCallbacks(navigateRunnable)
                startActivity(intent)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
            }
            MotionEvent.ACTION_UP -> {
                val x2 = event.x
                val y2 = event.y

                val deltaX = x2 - x1
                val deltaY = y2 - y1

                if (abs(deltaX) > abs(deltaY)) {
                    if (deltaX > 0) {
                        val intent = Intent(this, Welcome3::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.enter_animation_left, R.anim.exit_animation_left)
                        finish()
                    } else {
                        val intent = Intent(this, Welcome2::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.enter_animation_right, R.anim.exit_animation_right)
                        finish()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }
}