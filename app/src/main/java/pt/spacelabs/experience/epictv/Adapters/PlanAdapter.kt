package pt.spacelabs.experience.epictv.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entitys.Plan

class PlanAdapter(private val planList: List<Plan>, private var selectedOption: String, private val onPlanSelected: (Boolean?) -> Unit) :
    RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {

    private val filteredPlanList = mutableListOf<Plan>()
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    init {
        updateFilteredList()
    }

    inner class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val planName: TextView = view.findViewById(R.id.planName)
        val planPrice: TextView = view.findViewById(R.id.planPrice)
        val plan720p: LinearLayout = view.findViewById(R.id.plan720p)
        val plan1080p: LinearLayout = view.findViewById(R.id.plan1080p)
        val plan4k: LinearLayout = view.findViewById(R.id.plan4k)
        val haveDownloads: LinearLayout = view.findViewById(R.id.haveDownloads)
        val maxProfiles: TextView = view.findViewById(R.id.maxProfiles)
        val watch2Gether: LinearLayout = view.findViewById(R.id.watch2Gether)
    }

    fun updateOption(newOption: String) {
        selectedOption = newOption
        updateFilteredList()
        selectedPosition = -1
        notifyDataSetChanged()
        onPlanSelected(false)
    }

    private fun updateFilteredList() {
        filteredPlanList.clear()
        filteredPlanList.addAll(
            planList.filter { plan ->
                (selectedOption == "Mensal" && !plan.isYearly) || (selectedOption == "Anual" && plan.isYearly)
            }
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plan_item, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = filteredPlanList[position]

        holder.planName.text = plan.name
        holder.planPrice.text = plan.value.toString() + " €"

        holder.watch2Gether.visibility = if (plan.haveWatchShare) View.VISIBLE else View.GONE
        holder.haveDownloads.visibility = if (plan.haveDownloads) View.VISIBLE else View.GONE

        holder.maxProfiles.text = if (plan.qtdProfiles > 1) {
            "Conta partilhada até ${plan.qtdProfiles} utilizadores"
        } else {
            "Conta com 1 utilizador"
        }

        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.plan_selector_border_orange)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.plan_selector_border_cyan)
        }

        holder.plan4k.visibility = if (plan.have4k) View.VISIBLE else View.GONE
        holder.plan1080p.visibility = if (plan.have1080 && !plan.have4k) View.VISIBLE else View.GONE
        holder.plan720p.visibility = if (!plan.have1080 && !plan.have4k) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onPlanSelected(true)
        }
    }

    override fun getItemCount(): Int = filteredPlanList.size
}