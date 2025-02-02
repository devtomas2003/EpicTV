package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import pt.spacelabs.experience.epictv.Adapters.PlanAdapter
import pt.spacelabs.experience.epictv.entitys.Plan
import pt.spacelabs.experience.epictv.utils.Constants


class Plans : AppCompatActivity() {

    private var selectedOption: String = "Mensal"
    private var planIdSelected: String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.plans)

        val window: Window = window
        window.setNavigationBarColor(getResources().getColor(R.color.app_color))

        var plansList = mutableListOf<Plan>()
        val listRCPlans = findViewById<RecyclerView>(R.id.rvPlans)
        val btnGoPay = findViewById<Button>(R.id.btnGoPay)
        listRCPlans.layoutManager = LinearLayoutManager(this)
        listRCPlans.isNestedScrollingEnabled = false

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.loading, null)
        dialogBuilder.setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.show()

        val requestPlans = object : StringRequest(
            Method.GET,
            Constants.baseURL + "/plans",
            Response.Listener { response ->
                try {
                    val jsonArray = JSONArray(response)

                    for (index in 0 until jsonArray.length()) {
                        val planData = jsonArray.getJSONObject(index)
                            val course = Plan(
                                id = planData.getString("id"),
                                name = planData.getString("name"),
                                have1080 = planData.getBoolean("have1080"),
                                have4k = planData.getBoolean("have4k"),
                                qtdProfiles = planData.getInt("qtdProfiles"),
                                haveDownloads = planData.getBoolean("haveDownloads"),
                                haveWatchShare = planData.getBoolean("haveWatchShare"),
                                value = planData.getDouble("value"),
                                isYearly = planData.getBoolean("isYearly")
                            )
                            plansList.add(course)
                    }

                    val adapter = PlanAdapter(plansList, selectedOption){ selectedPlan ->
                        if (selectedPlan != null) {
                            planIdSelected = selectedPlan.id
                            btnGoPay.isEnabled = true
                            btnGoPay.setBackgroundResource(R.drawable.main_orange)
                        } else {
                            planIdSelected = ""
                            btnGoPay.isEnabled = false
                            btnGoPay.setBackgroundResource(R.drawable.btn_disable)
                        }
                    }
                    listRCPlans.adapter = adapter
                    findViewById<RadioGroup>(R.id.planPricing).setOnCheckedChangeListener { _,checkedId ->
                        selectedOption = when (checkedId){
                            R.id.radioMensal -> "Mensal"
                            R.id.radioAnual -> "Anual"
                            else -> selectedOption
                        }
                        (listRCPlans.adapter as? PlanAdapter)?.updateOption(selectedOption)
                    }
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

        findViewById<Button>(R.id.btnGoPay).setOnClickListener {
            if(planIdSelected == ""){
                AlertDialog.Builder(this)
                    .setTitle("Selação do plano")
                    .setMessage("Por favor, seleciona um plano!")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }else{
                val intent = Intent(this, SignUp::class.java)
                intent.putExtra("planId", planIdSelected);
                startActivity(intent)
            }
        }

        val backIcon: ImageView = findViewById(R.id.arrowpageback)
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}