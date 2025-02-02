package pt.spacelabs.experience.epictv.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entitys.Content
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper
import pt.spacelabs.experience.epictv.utils.DownloadService

class ContentAdapter(private val contentList: List<Content>) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

inner class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val poster: ImageView = view.findViewById(R.id.imgPoster)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
    val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.poster, parent, false)
    return ContentViewHolder(view)
}

override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
    val content = contentList[position]

    Picasso.with(holder.itemView.context)
        .load(Constants.contentURLPublic + content.poster)
        .fit()
        .centerCrop()
        .into(holder.poster)

    holder.poster.setOnClickListener {
        val downloadIntent = Intent(holder.itemView.context, DownloadService::class.java)
        downloadIntent.putExtra("manifestName", content.id)
        DBHelper(holder.itemView.context).createMovie(content.id, content.name, content.time, content.description, content.poster)
        holder.itemView.context.startForegroundService(downloadIntent)
    }

}



override fun getItemCount(): Int = contentList.size
}