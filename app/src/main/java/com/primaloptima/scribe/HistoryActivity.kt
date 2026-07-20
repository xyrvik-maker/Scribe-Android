package com.primaloptima.scribe

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.primaloptima.scribe.adapter.HistoryAdapter
import com.primaloptima.scribe.util.HistoryManager
import com.primaloptima.scribe.util.MarkdownUtil
import com.primaloptima.scribe.util.ThemeManager
import com.primaloptima.scribe.util.model.HistorySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryActivity : AppCompatActivity() {

    private val prefs by lazy { (application as ScribeApp).prefs }
    private val themeManager by lazy { (application as ScribeApp).themeManager }
    private val historyManager by lazy { HistoryManager(prefs) }
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.title = "Version History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyThemeToBars()
        setupRecycler()
        loadSnapshots()
    }

    private fun applyThemeToBars() {
        val theme = themeManager.activeTheme()
        window.statusBarColor = ThemeManager.parseColor(theme.colors.toolbar)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(ThemeManager.parseColor(theme.colors.toolbar))
        )
    }

    private fun setupRecycler() {
        adapter = HistoryAdapter { snap -> showSnapshotPreview(snap) }
        val rv = findViewById<RecyclerView>(R.id.rv_history)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun loadSnapshots() {
        val noteId = prefs.activeNoteId ?: ""
        val snaps = historyManager.getSnapshots(noteId)
        val tvEmpty = findViewById<TextView>(R.id.tv_history_empty)
        if (snaps.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            adapter.submitList(snaps)
        }
    }

    private fun showSnapshotPreview(snap: HistorySnapshot) {
        val words = MarkdownUtil.countWords(snap.content)
        AlertDialog.Builder(this)
            .setTitle("$words words")
            .setMessage(snap.content.take(500) + if (snap.content.length > 500) "…" else "")
            .setPositiveButton("Restore this version") { _, _ -> confirmRestore(snap) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun confirmRestore(snap: HistorySnapshot) {
        AlertDialog.Builder(this)
            .setTitle("Restore this version?")
            .setMessage("The current note content will be replaced.")
            .setPositiveButton("Restore") { _, _ ->
                val noteId = prefs.activeNoteId ?: return@setPositiveButton
                val db = (application as ScribeApp).database
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.noteDao().updateContent(noteId, snap.content, System.currentTimeMillis())
                    }
                    Toast.makeText(this@HistoryActivity, "Version restored", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
