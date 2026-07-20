package com.primaloptima.scribe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.primaloptima.scribe.adapter.ThemeAdapter
import com.primaloptima.scribe.util.ThemeManager
import com.primaloptima.scribe.viewmodel.ThemeViewModel

class ThemeListActivity : AppCompatActivity() {

    private val vm: ThemeViewModel by viewModels()
    private lateinit var adapter: ThemeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_list)
        supportActionBar?.title = "Themes"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyThemeToBars()
        setupRecycler()
        observeVm()

        // FAB → new blank theme
        findViewById<FloatingActionButton>(R.id.fab_new_theme).setOnClickListener {
            val newTheme = vm.activeTheme.value?.copy(
                id = vm.generateId(),
                name = "My Theme",
                builtIn = false
            ) ?: return@setOnClickListener
            vm.save(newTheme)
            val intent = Intent(this, ThemeEditActivity::class.java)
            intent.putExtra("theme_id", newTheme.id)
            startActivity(intent)
        }
    }

    private fun applyThemeToBars() {
        val app = application as ScribeApp
        val theme = app.themeManager.activeTheme()
        window.statusBarColor = ThemeManager.parseColor(theme.colors.toolbar)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(ThemeManager.parseColor(theme.colors.toolbar))
        )
    }

    private fun setupRecycler() {
        adapter = ThemeAdapter(
            onSelect = { theme ->
                vm.setActive(theme.id)
                Toast.makeText(this, "${theme.name} applied", Toast.LENGTH_SHORT).show()
            },
            onEdit = { theme ->
                val intent = Intent(this, ThemeEditActivity::class.java)
                intent.putExtra("theme_id", theme.id)
                startActivity(intent)
            },
            onDuplicate = { theme ->
                vm.duplicate(theme.id)
                Toast.makeText(this, "Duplicated ${theme.name}", Toast.LENGTH_SHORT).show()
            },
            onDelete = { theme ->
                AlertDialog.Builder(this)
                    .setTitle("Delete \"${theme.name}\"?")
                    .setPositiveButton("Delete") { _, _ -> vm.delete(theme.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        val rv = findViewById<RecyclerView>(R.id.rv_themes)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun observeVm() {
        vm.themes.observe(this) { themes ->
            adapter.submitList(themes)
            adapter.setActiveId(vm.activeTheme.value?.id ?: "paper")
        }
        vm.activeTheme.observe(this) { theme ->
            adapter.setActiveId(theme.id)
        }
    }

    override fun onResume() {
        super.onResume()
        vm.reload()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
