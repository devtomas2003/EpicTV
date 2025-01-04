package pt.spacelabs.experience.epictv.Adapters

import ItemSpacingDecoration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entitys.Category
import pt.spacelabs.experience.epictv.entitys.Plan

class CategoryAdapter(private val categoriesList: List<Category>) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val categoryName: TextView = view.findViewById(R.id.categoryName)
    val contentRC: RecyclerView = view.findViewById(R.id.contentList)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
    val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category, parent, false)
    return CategoryViewHolder(view)
}

override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
    val category = categoriesList[position]

    holder.categoryName.text = category.name

    val spacing = 16 // set the desired spacing in pixels
    val layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
    holder.contentRC.layoutManager = layoutManager
    holder.contentRC.addItemDecoration(ItemSpacingDecoration(spacing))


    val adapter = ContentAdapter(category.contents)
    holder.contentRC.adapter = adapter
}

override fun getItemCount(): Int = categoriesList.size
}