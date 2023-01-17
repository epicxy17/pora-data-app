package pora.data.proj

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import pora.data.proj.databinding.ViewBusStationBinding

import pora.data.proj.models.BusStationsResponse

class BusStationsAdapter(
    private var items: List<BusStationsResponse>,
    val onItemClickCallback: (BusStationsResponse) -> Unit,
 ) : RecyclerView.Adapter<BusStationsAdapter.ShowViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowViewHolder {
        val binding = ViewBusStationBinding.inflate(LayoutInflater.from(parent.context))
        return ShowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShowViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    fun toggleItems(shows: List<BusStationsResponse>) {
        items = shows
        notifyDataSetChanged()
    }

    inner class ShowViewHolder(private val binding: ViewBusStationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BusStationsResponse) {
            binding.name.text = item.name
            binding.location.text = "x: " + item.location.coordinates[0].toString() + " || y: " + item.location.coordinates[1]
            binding.cardContainer.setOnClickListener {
                onItemClickCallback(item)
            }
        }
    }
}