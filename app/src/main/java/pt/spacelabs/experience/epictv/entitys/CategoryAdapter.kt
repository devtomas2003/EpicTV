import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entities.Category

class CategoryAdapter(
    private val context: Context,
    private val categoryList: List<Category>
) : ArrayAdapter<Category>(context, 0, categoryList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val category = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.category, parent, false)

        val categoryName = view.findViewById<TextView>(R.id.categoryName)
        //val listContent = view.findViewById<ListView>(R.id.listContent)

        categoryName.text = category?.name

        val contentAdapter = ContentAdapter(context, category?.contentList ?: emptyList())
        //listContent.adapter = contentAdapter

        return view
    }
}
