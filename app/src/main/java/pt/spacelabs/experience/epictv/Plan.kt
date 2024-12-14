package pt.spacelabs.experience.epictv

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.ScrollView
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge


class Plan : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val radioGroup: RadioGroup = findViewById(R.id.radioGroup_button_plan)
        val scrollView: ScrollView = findViewById(R.id.scrollview_plan)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId){
                R.id.radioMensal -> {
                    showPlan("Mensal", scrollView)
                }
                R.id.radioAnual -> {
                    showPlan("Anual", scrollView)
                }
            }
        }
    }

    private fun showPlan(planType: String, scrollView: ScrollView) {
        if(planType == "Mensal"){
            scrollView.visibility= ScrollView.VISIBLE
        }else if(planType == "Anual"){
            scrollView.visibility = ScrollView.VISIBLE
        }
    }
}