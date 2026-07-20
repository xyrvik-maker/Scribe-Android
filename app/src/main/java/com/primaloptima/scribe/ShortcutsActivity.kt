package com.primaloptima.scribe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.primaloptima.scribe.adapter.ShortcutAdapter
import com.primaloptima.scribe.util.ThemeManager
import com.primaloptima.scribe.util.model.ShortcutAction
import com.primaloptima.scribe.viewmodel.ShortcutsViewModel

class ShortcutsActivity : AppCompatActivity() {

    private val vm: ShortcutsViewModel by viewModels()
    private lateinit var adapter: ShortcutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortcuts)
        supportActionBar?.title = "Shortcuts"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyThemeToBars()
        setupRecycler()
        observeVm()

        // FAB → new shortcut
        findViewById<FloatingActionButton>(R.id.fab_new_shortcut).setOnClickListener {
            showEditDialog(null)
        }

        // Reset to defaults
        findViewById<View>(R.id.btn_reset_shortcuts).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset to defaults?")
                .setMessage("All custom shortcuts will be removed.")
                .setPositiveButton("Reset") { _, _ ->
                    vm.resetToDefaults()
                    Toast.makeText(this, "Shortcuts reset to defaults", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun applyThemeToBars() {
        val theme = (application as ScribeApp).themeManager.activeTheme()
        window.statusBarColor = ThemeManager.parseColor(theme.colors.toolbar)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(ThemeManager.parseColor(theme.colors.toolbar))
        )
    }

    private fun setupRecycler() {
        adapter = ShortcutAdapter(
            onEdit = { shortcut -> showEditDialog(shortcut) },
            onDelete = { shortcut ->
                AlertDialog.Builder(this)
                    .setTitle("Delete \"${shortcut.label}\"?")
                    .setPositiveButton("Delete") { _, _ -> vm.delete(shortcut.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onMove = { from, to -> vm.reorder(from, to) }
        )
        val rv = findViewById<RecyclerView>(R.id.rv_shortcuts)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        adapter.attachDragDrop(rv)
    }

    private fun observeVm() {
        vm.shortcuts.observe(this) { adapter.submitList(it) }
    }

    private fun showEditDialog(existing: ShortcutAction?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_shortcut, null)
        val etLabel = view.findViewById<EditText>(R.id.et_shortcut_label)
        val rgKind = view.findViewById<RadioGroup>(R.id.rg_shortcut_kind)
        val rbInsert = view.findViewById<RadioButton>(R.id.rb_insert)
        val rbWrap = view.findViewById<RadioButton>(R.id.rb_wrap)
        val rbPair = view.findViewById<RadioButton>(R.id.rb_pair)
        val etPayload = view.findViewById<EditText>(R.id.et_shortcut_payload)
        val layoutClosing = view.findViewById<View>(R.id.layout_closing)
        val etClosing = view.findViewById<EditText>(R.id.et_shortcut_closing)

        if (existing != null) {
            etLabel.setText(existing.label)
            etPayload.setText(existing.payload)
            etClosing.setText(existing.closing ?: "")
            when (existing.kind) {
                "wrap" -> rbWrap.isChecked = true
                "pair" -> rbPair.isChecked = true
                else   -> rbInsert.isChecked = true
            }
        } else {
            rbInsert.isChecked = true
        }

        val updateClosingVisibility = {
            layoutClosing.visibility =
                if (rgKind.checkedRadioButtonId == R.id.rb_wrap ||
                    rgKind.checkedRadioButtonId == R.id.rb_pair) View.VISIBLE else View.GONE
        }
        rgKind.setOnCheckedChangeListener { _, _ -> updateClosingVisibility() }
        updateClosingVisibility()

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "New Shortcut" else "Edit Shortcut")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val label = etLabel.text.toString().trim()
                val payload = etPayload.text.toString()
                val closing = etClosing.text.toString().ifBlank { null }
                val kind = when (rgKind.checkedRadioButtonId) {
                    R.id.rb_wrap -> "wrap"
                    R.id.rb_pair -> "pair"
                    else -> "insert"
                }
                if (label.isBlank() || payload.isBlank()) {
                    Toast.makeText(this, "Label and payload are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val shortcut = ShortcutAction(
                    id = existing?.id ?: vm.generateId(),
                    label = label, kind = kind, payload = payload, closing = closing
                )
                if (existing == null) vm.add(shortcut) else vm.update(shortcut)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
