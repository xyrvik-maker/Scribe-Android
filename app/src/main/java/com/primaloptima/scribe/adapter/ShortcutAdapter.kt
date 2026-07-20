package com.primaloptima.scribe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.primaloptima.scribe.R
import com.primaloptima.scribe.util.model.ShortcutAction

class ShortcutAdapter(
    private val onEdit: (ShortcutAction) -> Unit,
    private val onDelete: (ShortcutAction) -> Unit,
    private val onMove: (Int, Int) -> Unit
) : ListAdapter<ShortcutAction, ShortcutAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shortcut, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_shortcut_label)
        private val tvKind: TextView = itemView.findViewById(R.id.tv_shortcut_kind)
        private val tvPayload: TextView = itemView.findViewById(R.id.tv_shortcut_payload)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_shortcut_edit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_shortcut_delete)

        fun bind(shortcut: ShortcutAction) {
            tvLabel.text = shortcut.label
            tvKind.text = shortcut.kind
            val payloadDisplay = if (shortcut.closing != null)
                "${shortcut.payload}…${shortcut.closing}"
            else shortcut.payload.take(20)
            tvPayload.text = payloadDisplay
            btnEdit.setOnClickListener { onEdit(shortcut) }
            btnDelete.setOnClickListener { onDelete(shortcut) }
        }
    }

    fun attachDragDrop(recyclerView: RecyclerView) {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(rv: RecyclerView, a: RecyclerView.ViewHolder,
                                b: RecyclerView.ViewHolder): Boolean {
                onMove(a.adapterPosition, b.adapterPosition)
                return true
            }
            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {}
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ShortcutAction>() {
            override fun areItemsTheSame(a: ShortcutAction, b: ShortcutAction) = a.id == b.id
            override fun areContentsTheSame(a: ShortcutAction, b: ShortcutAction) = a == b
        }
    }
}
