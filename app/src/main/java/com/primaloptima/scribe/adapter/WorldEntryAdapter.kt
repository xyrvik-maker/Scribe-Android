package com.primaloptima.scribe.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.primaloptima.scribe.R
import com.primaloptima.scribe.data.WorldEntry

class WorldEntryAdapter(
    private val onClick: (WorldEntry) -> Unit,
    private val onDuplicate: (WorldEntry) -> Unit,
    private val onDelete: (WorldEntry) -> Unit
) : ListAdapter<WorldEntry, WorldEntryAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_world_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.iv_entry_photo)
        private val tvName: TextView = itemView.findViewById(R.id.tv_entry_name)
        private val tvSummary: TextView = itemView.findViewById(R.id.tv_entry_summary)
        private val btnDuplicate: ImageButton = itemView.findViewById(R.id.btn_entry_duplicate)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_entry_delete)

        fun bind(entry: WorldEntry) {
            tvName.text = entry.name
            tvSummary.text = entry.summary.ifBlank { "(no summary)" }
            tvSummary.visibility = if (entry.summary.isBlank()) View.GONE else View.VISIBLE

            if (entry.imageUri != null) {
                ivPhoto.visibility = View.VISIBLE
                ivPhoto.load(Uri.parse(entry.imageUri)) {
                    crossfade(true)
                    transformations(RoundedCornersTransformation(8f))
                }
            } else {
                ivPhoto.visibility = View.GONE
            }

            itemView.setOnClickListener { onClick(entry) }
            btnDuplicate.setOnClickListener { onDuplicate(entry) }
            btnDelete.setOnClickListener { onDelete(entry) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WorldEntry>() {
            override fun areItemsTheSame(a: WorldEntry, b: WorldEntry) = a.id == b.id
            override fun areContentsTheSame(a: WorldEntry, b: WorldEntry) = a == b
        }
    }
}
