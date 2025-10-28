package com.example.learningcultureone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CountryProgressAdapter(
    private val items: List<CountryProgress>
) : RecyclerView.Adapter<CountryProgressAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvModule: TextView = view.findViewById(R.id.tvModule)
        val tvProgress: TextView = view.findViewById(R.id.tvProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country_progress, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvModule.text = item.module
        holder.tvProgress.text = "Completed modules: ${item.progress}"

        // Optional: Better visual distinction
        holder.itemView.alpha = if (item.progress > 0) 1f else 0.5f
    }

    override fun getItemCount(): Int = items.size
}
