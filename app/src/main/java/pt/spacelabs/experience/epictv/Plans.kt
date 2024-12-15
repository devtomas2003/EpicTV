package pt.spacelabs.experience.epictv

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Window
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
import kotlinx.coroutines.selects.select
import org.json.JSONArray
import org.json.JSONException
import pt.spacelabs.experience.epictv.Adapters.PlanAdapter
import pt.spacelabs.experience.epictv.entitys.Plan
import pt.spacelabs.experience.epictv.utils.Constants


class Plans : AppCompatActivity() {

    private var selectedOption: String = "Mensal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.plans)

        val window: Window = window
        window.setNavigationBarColor(getResources().getColor(R.color.app_color))

        var plansList = mutableListOf<Plan>()
        val listRCPlans = findViewById<RecyclerView>(R.id.rvPlans);
        listRCPlans.layoutManager = LinearLayoutManager(this)
        listRCPlans.isNestedScrollingEnabled = false



        val requestQueue: RequestQueue = Volley.newRequestQueue(this)

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

                    val adapter = PlanAdapter(plansList, selectedOption)
                    listRCPlans.adapter = adapter
                    findViewById<RadioGroup>(R.id.planPricing).setOnCheckedChangeListener { _,checkedId ->
                        selectedOption = when (checkedId){
                            R.id.radioMensal -> "Mensal"
                            R.id.radioAnual -> "Anual"
                            else -> selectedOption
                        }
                        (listRCPlans.adapter as? PlanAdapter)?.updateOption(selectedOption)
                    }
                } catch (e: JSONException) {
                    AlertDialog.Builder(this)
                        .setTitle("Falha de ligação")
                        .setMessage("Ocorreu um erro com a resposta do servidor!")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                }
            },
            Response.ErrorListener { error ->
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
}