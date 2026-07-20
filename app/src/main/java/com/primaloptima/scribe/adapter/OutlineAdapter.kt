package com.primaloptima.scribe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.primaloptima.scribe.R
import com.primaloptima.scribe.util.model.OutlineEntry

class OutlineAdapter(
    private val onClick: (OutlineEntry) -> Unit
) : ListAdapter<OutlineEntry, OutlineAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_outline, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tv_outline_text)
        private val indent: View = itemView.findViewById(R.id.outline_indent)

        fun bind(entry: OutlineEntry) {
            tvText.text = entry.text
            val density = itemView.resources.displayMetrics.density
            // H1=0dp indent, H2=12dp, H3=24dp, H4=36dp
            val indentDp = (entry.level - 1) * 12
            (indent.layoutParams as ViewGroup.MarginLayoutParams).width = (indentDp * density).toInt()
            val alpha = when (entry.level) { 1 -> 1f; 2 -> 0.85f; 3 -> 0.7f; else -> 0.6f }
            tvText.alpha = alpha
            tvText.textSize = when (entry.level) { 1 -> 15f; 2 -> 14f; else -> 13f }
            itemView.setOnClickListener { onClick(entry) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OutlineEntry>() {
            override fun areItemsTheSame(a: OutlineEntry, b: OutlineEntry) =
                a.lineIndex == b.lineIndex
            override fun areContentsTheSame(a: OutlineEntry, b: OutlineEntry) = a == b
        }
    }
}
