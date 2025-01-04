package pt.spacelabs.experience.epictv.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entitys.Category
import pt.spacelabs.experience.epictv.entitys.Plan

class CategoryAdapter(private val categoriesList: List<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val categoryName: TextView = view.findViewById(R.id.categoryName)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
    val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category, parent, false)
    return CategoryViewHolder(view)
}

override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
    val category = categoriesList[position]

    holder.categoryName.text = category.name
}

override fun getItemCount(): Int = categoriesList.size
}