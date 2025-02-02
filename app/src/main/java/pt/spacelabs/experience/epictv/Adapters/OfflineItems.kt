package pt.spacelabs.experience.epictv.Adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import pt.spacelabs.experience.epictv.Player
import pt.spacelabs.experience.epictv.R
import pt.spacelabs.experience.epictv.entitys.Content
import pt.spacelabs.experience.epictv.utils.Constants
import pt.spacelabs.experience.epictv.utils.DBHelper
import pt.spacelabs.experience.epictv.utils.DownloadService

class OfflineItems(private val offlineList: List<Content>) : RecyclerView.Adapter<OfflineItems.OfflineItemsViewHolder>() {

inner class OfflineItemsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val movieName: TextView = view.findViewById(R.id.movieName)
    val movieImage: ImageView = view.findViewById(R.id.movieImage)
    val timelbl: TextView = view.findViewById(R.id.timelbl)
    val movieDescription: TextView = view.findViewById(R.id.movieDescription)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineItemsViewHolder {
    val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.offlineitem, parent, false)
    return OfflineItemsViewHolder(view)
}

override fun onBindViewHolder(holder: OfflineItemsViewHolder, position: Int) {
    val content = offlineList[position]

    holder.movieName.text = content.name
    holder.timelbl.text = content.time.toString() + " mins"
    holder.movieDescription.text = content.description

    Picasso.with(holder.itemView.context)
        .load(Constants.contentURLPublic + content.poster)
        .fit()
        .centerCrop()
        .into(holder.movieImage)

    holder.itemView.setOnClickListener {
        val intent = Intent(holder.itemView.context, Player::class.java)
        intent.putExtra("manifestName", content.id)
        intent.putExtra("contentType", "movie")
        holder.itemView.context.startActivity(intent)
    }
}

override fun getItemCount(): Int = offlineList.size
}