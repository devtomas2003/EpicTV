import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entities.Content

class ContentAdapter(
    private val context: Context,
    private val contentList: List<Content>
) : ArrayAdapter<Content>(context, 0, contentList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val content = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.poster, parent, false)

        val contentImg = view.findViewById<ImageView>(R.id.imgPoster)

        Picasso.with(context)
            .load("https://vis-ipv-cda.epictv.spacelabs.pt/public/" + content?.poster)
            .into(contentImg)

        return view
    }
}
