package com.example.learningcultureone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class CountryProgress(
    val moduleName: String = "",
    val progress: Int = 0
)

class CountryProgressAdapter(private val progressList: List<CountryProgress>) :
    RecyclerView.Adapter<CountryProgressAdapter.ProgressViewHolder>() {

    class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvModuleName: TextView = itemView.findViewById(R.id.tvModuleName)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tvProgressPercent: TextView = itemView.findViewById(R.id.tvProgressPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country_progress, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val currentItem = progressList[position]
        holder.tvModuleName.text = currentItem.moduleName
        holder.progressBar.progress = currentItem.progress
        holder.tvProgressPercent.text = "${currentItem.progress}%"
    }

    override fun getItemCount() = progressList.size
}
