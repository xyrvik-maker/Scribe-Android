package com.primaloptima.scribe

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.slider.Slider
import com.primaloptima.scribe.util.ThemeManager
import com.primaloptima.scribe.util.WritingStats

class SettingsActivity : AppCompatActivity() {

    private val prefs by lazy { (application as ScribeApp).prefs }
    private val themeManager by lazy { (application as ScribeApp).themeManager }
    private val writingStats by lazy { WritingStats(prefs) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyThemeToBars()
        setupAll()
    }

    private fun applyThemeToBars() {
        val theme = themeManager.activeTheme()
        window.statusBarColor = ThemeManager.parseColor(theme.colors.toolbar)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(ThemeManager.parseColor(theme.colors.toolbar))
        )
    }

    private fun setupAll() {
        setupStorage()
        setupAppearance()
        setupWritingOptions()
        setupGoals()
    }

    // ── Storage & Folders ──────────────────────────────────────────────────────

    private fun setupStorage() {
        val tvFolder = findViewById<TextView>(R.id.tv_current_folder)
        val root = prefs.externalRootJson?.let {
            com.google.gson.Gson().fromJson(it, com.primaloptima.scribe.util.model.ExternalRoot::class.java)
        }
        tvFolder.text = root?.name ?: prefs.vaultName

        findViewById<android.view.View>(R.id.btn_disconnect_folder).apply {
            visibility = if (root != null) android.view.View.VISIBLE else android.view.View.GONE
            setOnClickListener {
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("Disconnect folder?")
                    .setMessage("The files will remain on disk. Scribe will switch back to the local vault.")
                    .setPositiveButton("Disconnect") { _, _ ->
                        prefs.externalRootJson = null
                        tvFolder.text = prefs.vaultName
                        visibility = android.view.View.GONE
                        Toast.makeText(this@SettingsActivity, "Folder disconnected", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    // ── Appearance ─────────────────────────────────────────────────────────────

    private fun setupAppearance() {
        val tvTheme = findViewById<TextView>(R.id.tv_active_theme)
        tvTheme.text = themeManager.activeTheme().name

        findViewById<android.view.View>(R.id.btn_themes).setOnClickListener {
            startActivity(Intent(this, ThemeListActivity::class.java))
        }
    }

    // ── Writing ────────────────────────────────────────────────────────────────

    private fun setupWritingOptions() {
        // Show word count toggle
        val switchWordCount = findViewById<SwitchCompat>(R.id.switch_word_count)
        switchWordCount.isChecked = prefs.showWordCount
        switchWordCount.setOnCheckedChangeListener { _, checked -> prefs.showWordCount = checked }

        // Typewriter mode
        val switchTypewriter = findViewById<SwitchCompat>(R.id.switch_typewriter)
        switchTypewriter.isChecked = prefs.typewriterMode
        switchTypewriter.setOnCheckedChangeListener { _, checked -> prefs.typewriterMode = checked }

        // Line spacing
        val tvSpacing = findViewById<TextView>(R.id.tv_line_spacing)
        tvSpacing.text = prefs.lineSpacing.replaceFirstChar { it.uppercase() }
        findViewById<android.view.View>(R.id.btn_line_spacing).setOnClickListener {
            val options = arrayOf("Compact", "Comfortable", "Spacious")
            val keys = arrayOf("compact", "comfortable", "spacious")
            AlertDialog.Builder(this)
                .setTitle("Line spacing")
                .setItems(options) { _, which ->
                    prefs.lineSpacing = keys[which]
                    tvSpacing.text = options[which]
                }
                .show()
        }

        // Editor font size
        val sliderFontSize = findViewById<Slider>(R.id.slider_font_size)
        val tvFontSizeVal = findViewById<TextView>(R.id.tv_font_size_value)
        sliderFontSize.value = prefs.editorFontSize.toFloat()
        tvFontSizeVal.text = "${prefs.editorFontSize}sp"
        sliderFontSize.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                prefs.editorFontSize = value.toInt()
                tvFontSizeVal.text = "${value.toInt()}sp"
            }
        }
    }

    // ── Goals ──────────────────────────────────────────────────────────────────

    private fun setupGoals() {
        val tvGoal = findViewById<TextView>(R.id.tv_daily_goal)
        val tvToday = findViewById<TextView>(R.id.tv_today_words)
        val tvStreak = findViewById<TextView>(R.id.tv_streak)
        val tvLongest = findViewById<TextView>(R.id.tv_longest_streak)

        tvGoal.text = "${prefs.dailyGoal} words"
        tvToday.text = "${writingStats.todayWords} words today"
        tvStreak.text = "${writingStats.currentStreak()} day streak"
        tvLongest.text = "Longest: ${writingStats.longestStreak()} days"

        findViewById<android.view.View>(R.id.btn_set_goal).setOnClickListener {
            val input = EditText(this).also {
                it.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                it.setText("${prefs.dailyGoal}")
            }
            AlertDialog.Builder(this)
                .setTitle("Daily word goal")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val n = input.text.toString().toIntOrNull() ?: return@setPositiveButton
                    if (n >= 50) {
                        writingStats.setDailyGoal(n)
                        tvGoal.text = "$n words"
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }

    override fun onResume() {
        super.onResume()
        // Refresh active theme name if user went to Themes and changed it
        val tvTheme = findViewById<TextView>(R.id.tv_active_theme)
        tvTheme.text = themeManager.activeTheme().name
    }
}
