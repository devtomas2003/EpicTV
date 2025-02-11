package pt.spacelabs.experience.epictv.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pt.spacelabs.experience.epictv.R

class NearbyAdapter(private val devicesList: MutableList<String>) :
    RecyclerView.Adapter<NearbyAdapter.BluetoothViewHolder>() {

    inner class BluetoothViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blueItem: TextView = view.findViewById(R.id.blueItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bluetooth_item, parent, false)
        return BluetoothViewHolder(view)
    }

    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int) {
        val content = devicesList[position]
        holder.blueItem.text = content
    }

    override fun getItemCount(): Int = devicesList.size

    fun addDevice(device: String) {
        if (!devicesList.contains(device)) {
            devicesList.add(device)
            notifyItemInserted(devicesList.size - 1)
        }
    }
}