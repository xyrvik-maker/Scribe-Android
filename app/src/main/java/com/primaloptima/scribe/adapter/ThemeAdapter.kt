package com.primaloptima.scribe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.primaloptima.scribe.R
import com.primaloptima.scribe.util.ThemeManager
import com.primaloptima.scribe.util.model.AppTheme

class ThemeAdapter(
    private val onSelect: (AppTheme) -> Unit,
    private val onEdit: (AppTheme) -> Unit,
    private val onDuplicate: (AppTheme) -> Unit,
    private val onDelete: (AppTheme) -> Unit
) : ListAdapter<AppTheme, ThemeAdapter.ViewHolder>(DIFF) {

    private var activeId: String = "paper"

    fun setActiveId(id: String) {
        val old = activeId
        activeId = id
        currentList.forEachIndexed { i, t ->
            if (t.id == old || t.id == id) notifyItemChanged(i)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), getItem(position).id == activeId)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val swatch: View = itemView.findViewById(R.id.theme_swatch)
        private val tvName: TextView = itemView.findViewById(R.id.tv_theme_name)
        private val tvMeta: TextView = itemView.findViewById(R.id.tv_theme_meta)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_theme_edit)
        private val btnDuplicate: ImageButton = itemView.findViewById(R.id.btn_theme_duplicate)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_theme_delete)

        fun bind(theme: AppTheme, isActive: Boolean) {
            swatch.setBackgroundColor(ThemeManager.parseColor(theme.colors.background))
            tvName.text = theme.name
            val modeLabel = if (theme.isDark) "Dark" else "Light"
            val fontLabel = when {
                theme.fontFamily.startsWith("serif") -> "Playfair"
                theme.fontFamily.startsWith("mono")  -> "Mono"
                else -> "Inter"
            }
            tvMeta.text = "$modeLabel · $fontLabel · ${theme.fontSize}sp"
            itemView.isActivated = isActive
            itemView.setOnClickListener { onSelect(theme) }
            btnDuplicate.setOnClickListener { onDuplicate(theme) }
            btnEdit.visibility = if (theme.builtIn) View.GONE else View.VISIBLE
            btnDelete.visibility = if (theme.builtIn) View.GONE else View.VISIBLE
            btnEdit.setOnClickListener { onEdit(theme) }
            btnDelete.setOnClickListener { onDelete(theme) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AppTheme>() {
            override fun areItemsTheSame(a: AppTheme, b: AppTheme) = a.id == b.id
            override fun areContentsTheSame(a: AppTheme, b: AppTheme) = a == b
        }
    }
}
