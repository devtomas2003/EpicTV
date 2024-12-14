package pt.spacelabs.experience.epictv.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entitys.Plan

class PlanAdapter(private val planList: List<Plan>) :
    RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.plan_item, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = planList[position]

        Log.d("Testing", plan.name)

        holder.planName.text = plan.name
        holder.planPrice.text = plan.valueMonthly.toString() + " €";

        if(!plan.haveWatchShare){
            holder.watch2Gether.visibility = View.GONE;
        }

        if(!plan.haveDownloads){
            holder.haveDownloads.visibility = View.GONE;
        }

        if(plan.qtdProfiles > 1){
            holder.maxProfiles.text = "Conta partilhada até " + plan.qtdProfiles + " utilizadores";
        }else{
            holder.maxProfiles.text = "Conta com 1 utilizador";
        }

        if(!plan.have4k){
            holder.plan4k.visibility = View.GONE
        }

        if(!plan.have1080 or plan.have4k){
            holder.plan1080p.visibility = View.GONE
        }

        if(plan.have1080 or plan.have4k){
            holder.plan720p.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = planList.size
}