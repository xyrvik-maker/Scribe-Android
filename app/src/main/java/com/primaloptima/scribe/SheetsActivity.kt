package com.primaloptima.scribe

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.primaloptima.scribe.adapter.WorldEntryAdapter
import com.primaloptima.scribe.data.WorldEntry
import com.primaloptima.scribe.util.ThemeManager
import com.primaloptima.scribe.viewmodel.SheetsViewModel

class SheetsActivity : AppCompatActivity() {

    private val vm: SheetsViewModel by viewModels()
    private lateinit var charAdapter: WorldEntryAdapter
    private lateinit var locAdapter: WorldEntryAdapter
    private var currentType = "character"
    private var editingEntry: WorldEntry? = null

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && editingEntry != null) {
            val updated = editingEntry!!.copy(imageUri = uri.toString())
            vm.updateEntry(updated)
            editingEntry = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sheets)
        supportActionBar?.title = "World Building"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyThemeToBars()
        setupTabs()
        setupRecyclers()
        setupFab()
        observeVm()
    }

    private fun applyThemeToBars() {
        val theme = (application as ScribeApp).themeManager.activeTheme()
        window.statusBarColor = ThemeManager.parseColor(theme.colors.toolbar)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(ThemeManager.parseColor(theme.colors.toolbar))
        )
    }

    private fun setupTabs() {
        val tabs = findViewById<TabLayout>(R.id.tabs_sheets)
        tabs.addTab(tabs.newTab().setText("Characters"))
        tabs.addTab(tabs.newTab().setText("Locations"))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = if (tab.position == 0) "character" else "location"
                updateVisibleList()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupRecyclers() {
        val makeAdapter: (String) -> WorldEntryAdapter = { type ->
            WorldEntryAdapter(
                onClick = { entry -> showEditDialog(entry) },
                onDuplicate = { entry -> vm.duplicateEntry(entry.id) },
                onDelete = { entry ->
                    AlertDialog.Builder(this)
                        .setTitle("Delete \"${entry.name}\"?")
                        .setPositiveButton("Delete") { _, _ -> vm.deleteEntry(entry.id) }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            )
        }
        charAdapter = makeAdapter("character")
        locAdapter = makeAdapter("location")

        val rvChars = findViewById<RecyclerView>(R.id.rv_characters)
        rvChars.layoutManager = LinearLayoutManager(this)
        rvChars.adapter = charAdapter

        val rvLocs = findViewById<RecyclerView>(R.id.rv_locations)
        rvLocs.layoutManager = LinearLayoutManager(this)
        rvLocs.adapter = locAdapter
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fab_new_sheet).setOnClickListener {
            showCreateDialog()
        }
    }

    private fun updateVisibleList() {
        val rvChars = findViewById<View>(R.id.rv_characters)
        val rvLocs = findViewById<View>(R.id.rv_locations)
        rvChars.visibility = if (currentType == "character") View.VISIBLE else View.GONE
        rvLocs.visibility = if (currentType == "location") View.VISIBLE else View.GONE
    }

    private fun observeVm() {
        vm.characters.observe(this) { charAdapter.submitList(it) }
        vm.locations.observe(this) { locAdapter.submitList(it) }
    }

    private fun showCreateDialog() {
        val input = EditText(this)
        input.hint = if (currentType == "character") "Character name" else "Location name"
        AlertDialog.Builder(this)
            .setTitle(if (currentType == "character") "New character" else "New location")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                vm.createEntry(currentType, name) { /* list updates via LiveData */ }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(entry: WorldEntry) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_sheet, null)
        val etName = view.findViewById<EditText>(R.id.et_sheet_name)
        val etSummary = view.findViewById<EditText>(R.id.et_sheet_summary)
        val fieldsContainer = view.findViewById<LinearLayout>(R.id.fields_container)
        val btnPhoto = view.findViewById<ImageButton>(R.id.btn_sheet_photo)

        etName.setText(entry.name)
        etSummary.setText(entry.summary)

        // Inflate field rows
        val gson = Gson()
        val type = object : TypeToken<List<SheetsViewModel.Companion.Field>>() {}.type
        val fields: MutableList<SheetsViewModel.Companion.Field> = try {
            gson.fromJson(entry.fieldsJson, type) ?: mutableListOf()
        } catch (_: Exception) { mutableListOf() }

        val fieldViews = mutableListOf<Pair<TextView, EditText>>() // label, value
        for (field in fields) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_sheet_field, fieldsContainer, false)
            val tvLabel = row.findViewById<TextView>(R.id.tv_field_label)
            val etValue = row.findViewById<EditText>(R.id.et_field_value)
            tvLabel.text = field.label
            etValue.setText(field.value)
            fieldsContainer.addView(row)
            fieldViews.add(Pair(tvLabel, etValue))
        }

        btnPhoto.setOnClickListener {
            editingEntry = entry
            photoPickerLauncher.launch("image/*")
        }

        AlertDialog.Builder(this)
            .setTitle("Edit ${entry.name}")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val updatedFields = fieldViews.mapIndexed { i, (tvLabel, etValue) ->
                    SheetsViewModel.Companion.Field(tvLabel.text.toString(), etValue.text.toString())
                }
                val updated = entry.copy(
                    name = etName.text.toString().trim().ifBlank { entry.name },
                    summary = etSummary.text.toString(),
                    fieldsJson = gson.toJson(updatedFields)
                )
                vm.updateEntry(updated)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
