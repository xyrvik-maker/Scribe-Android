package com.primaloptima.scribe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.primaloptima.scribe.R
import com.primaloptima.scribe.util.MarkdownUtil
import com.primaloptima.scribe.util.model.HistorySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onClick: (HistorySnapshot) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var items: List<HistorySnapshot> = emptyList()

    fun submitList(list: List<HistorySnapshot>) {
        items = list.sortedByDescending { it.savedAt }
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tv_history_date)
        private val tvWords: TextView = itemView.findViewById(R.id.tv_history_words)
        private val tvSnippet: TextView = itemView.findViewById(R.id.tv_history_snippet)
        private val fmt = SimpleDateFormat("MMM d · h:mm a", Locale.getDefault())

        fun bind(snap: HistorySnapshot) {
            tvDate.text = fmt.format(Date(snap.savedAt))
            tvWords.text = "${MarkdownUtil.countWords(snap.content)} words"
            tvSnippet.text = snap.content.take(80).replace('\n', ' ').trim()
            itemView.setOnClickListener { onClick(snap) }
        }
    }
}
