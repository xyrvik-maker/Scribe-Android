package com.primaloptima.scribe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.primaloptima.scribe.R
import com.primaloptima.scribe.data.Note
import com.primaloptima.scribe.util.MarkdownUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note, View) -> Unit
) : ListAdapter<Note, NoteAdapter.ViewHolder>(DIFF) {

    private var activeNoteId: String? = null

    fun setActiveNote(id: String?) {
        val old = activeNoteId
        activeNoteId = id
        // Refresh old and new active rows
        currentList.forEachIndexed { i, n ->
            if (n.id == old || n.id == id) notifyItemChanged(i)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), getItem(position).id == activeNoteId)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_note_name)
        private val tvMeta: TextView = itemView.findViewById(R.id.tv_note_meta)
        private val tvSnippet: TextView = itemView.findViewById(R.id.tv_note_snippet)
        private val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())

        fun bind(note: Note, isActive: Boolean) {
            tvName.text = note.name
            val words = MarkdownUtil.countWords(note.content)
            val dateStr = dateFmt.format(Date(note.updatedAt))
            tvMeta.text = "$words words · $dateStr"
            tvSnippet.text = note.content
                .replace(Regex("^#+\\s+", RegexOption.MULTILINE), "")
                .take(80)
                .replace('\n', ' ')
                .trim()
            tvSnippet.visibility = if (tvSnippet.text.isBlank()) View.GONE else View.VISIBLE
            itemView.isActivated = isActive
            itemView.setOnClickListener { onNoteClick(note) }
            itemView.setOnLongClickListener { onNoteLongClick(note, itemView); true }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(a: Note, b: Note) = a.id == b.id
            override fun areContentsTheSame(a: Note, b: Note) =
                a.name == b.name && a.updatedAt == b.updatedAt && a.folderPath == b.folderPath
        }
    }
}
