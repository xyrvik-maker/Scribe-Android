package com.primaloptima.scribe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.primaloptima.scribe.util.DefaultThemes
import com.primaloptima.scribe.util.ThemeManager
import com.primaloptima.scribe.util.model.AppTheme
import com.primaloptima.scribe.util.model.ThemeColors
import com.primaloptima.scribe.viewmodel.ThemeViewModel

class ThemeEditActivity : AppCompatActivity() {

    private val vm: ThemeViewModel by viewModels()
    private var themeId: String = ""
    private var workingTheme: AppTheme? = null

    // Name
    private lateinit var etName: EditText

    // Colors
    private lateinit var etBackground: EditText
    private lateinit var etSurface: EditText
    private lateinit var etText: EditText
    private lateinit var etMutedText: EditText
    private lateinit var etAccent: EditText
    private lateinit var etBorder: EditText
    private lateinit var etSelection: EditText
    private lateinit var etToolbar: EditText
    private lateinit var etToolbarText: EditText

    // Typography
    private lateinit var tvFontFamily: TextView
    private lateinit var sliderFontSize: Slider
    private lateinit var tvFontSizeVal: TextView
    private lateinit var sliderLineHeight: Slider
    private lateinit var tvLineHeightVal: TextView

    // Layout
    private lateinit var sliderPadH: Slider
    private lateinit var tvPadHVal: TextView
    private lateinit var sliderPadV: Slider
    private lateinit var tvPadVVal: TextView
    private lateinit var sliderMaxWidth: Slider
    private lateinit var tvMaxWidthVal: TextView

    // Preview
    private lateinit var previewCard: View
    private lateinit var tvPreview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_edit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Theme"

        themeId = intent.getStringExtra("theme_id") ?: ""

        bindViews()
        vm.themes.observe(this) { themes ->
            if (workingTheme == null) {
                workingTheme = themes.firstOrNull { it.id == themeId }
                    ?: DefaultThemes.all.first()
                populateFields()
            }
        }
        vm.reload()

        findViewById<View>(R.id.btn_save_theme).setOnClickListener { saveTheme() }
        setupColorWatchers()
    }

    private fun bindViews() {
        etName = findViewById(R.id.et_theme_name)
        etBackground = findViewById(R.id.et_color_background)
        etSurface = findViewById(R.id.et_color_surface)
        etText = findViewById(R.id.et_color_text)
        etMutedText = findViewById(R.id.et_color_muted_text)
        etAccent = findViewById(R.id.et_color_accent)
        etBorder = findViewById(R.id.et_color_border)
        etSelection = findViewById(R.id.et_color_selection)
        etToolbar = findViewById(R.id.et_color_toolbar)
        etToolbarText = findViewById(R.id.et_color_toolbar_text)
        tvFontFamily = findViewById(R.id.tv_font_family)
        sliderFontSize = findViewById(R.id.slider_theme_font_size)
        tvFontSizeVal = findViewById(R.id.tv_theme_font_size_val)
        sliderLineHeight = findViewById(R.id.slider_line_height)
        tvLineHeightVal = findViewById(R.id.tv_line_height_val)
        sliderPadH = findViewById(R.id.slider_pad_h)
        tvPadHVal = findViewById(R.id.tv_pad_h_val)
        sliderPadV = findViewById(R.id.slider_pad_v)
        tvPadVVal = findViewById(R.id.tv_pad_v_val)
        sliderMaxWidth = findViewById(R.id.slider_max_width)
        tvMaxWidthVal = findViewById(R.id.tv_max_width_val)
        previewCard = findViewById(R.id.theme_preview_card)
        tvPreview = findViewById(R.id.tv_theme_preview)
    }

    private fun populateFields() {
        val theme = workingTheme ?: return
        etName.setText(theme.name)
        etBackground.setText(theme.colors.background)
        etSurface.setText(theme.colors.surface)
        etText.setText(theme.colors.text)
        etMutedText.setText(theme.colors.mutedText)
        etAccent.setText(theme.colors.accent)
        etBorder.setText(theme.colors.border)
        etSelection.setText(theme.colors.selection)
        etToolbar.setText(theme.colors.toolbar)
        etToolbarText.setText(theme.colors.toolbarText)

        tvFontFamily.text = theme.fontFamily

        sliderFontSize.value = theme.fontSize.toFloat().coerceIn(14f, 24f)
        tvFontSizeVal.text = "${theme.fontSize}sp"
        sliderFontSize.addOnChangeListener { _, v, _ -> tvFontSizeVal.text = "${v.toInt()}sp" }

        sliderLineHeight.value = theme.lineHeight.coerceIn(1.2f, 2.5f)
        tvLineHeightVal.text = String.format("%.2f", theme.lineHeight)
        sliderLineHeight.addOnChangeListener { _, v, _ -> tvLineHeightVal.text = String.format("%.2f", v) }

        sliderPadH.value = theme.paddingHorizontal.toFloat().coerceIn(8f, 48f)
        tvPadHVal.text = "${theme.paddingHorizontal}dp"
        sliderPadH.addOnChangeListener { _, v, _ -> tvPadHVal.text = "${v.toInt()}dp" }

        sliderPadV.value = theme.paddingVertical.toFloat().coerceIn(8f, 48f)
        tvPadVVal.text = "${theme.paddingVertical}dp"
        sliderPadV.addOnChangeListener { _, v, _ -> tvPadVVal.text = "${v.toInt()}dp" }

        sliderMaxWidth.value = theme.maxWidth.toFloat().coerceIn(400f, 900f)
        tvMaxWidthVal.text = "${theme.maxWidth}dp"
        sliderMaxWidth.addOnChangeListener { _, v, _ -> tvMaxWidthVal.text = "${v.toInt()}dp" }

        tvFontFamily.setOnClickListener { pickFontFamily() }
        updatePreview()
    }

    private fun setupColorWatchers() {
        for (et in listOf(etBackground, etSurface, etText, etMutedText, etAccent,
                          etBorder, etSelection, etToolbar, etToolbarText)) {
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) { updatePreview() }
            })
        }
    }

    private fun updatePreview() {
        try {
            val bg = ThemeManager.parseColor(etBackground.text.toString())
            val txt = ThemeManager.parseColor(etText.text.toString())
            previewCard.setBackgroundColor(bg)
            tvPreview.setTextColor(txt)
            tvPreview.typeface = ThemeManager.resolveTypeface(this, tvFontFamily.text.toString())
            tvPreview.textSize = sliderFontSize.value
        } catch (_: Exception) {}
    }

    private fun pickFontFamily() {
        val options = arrayOf(
            "serif", "serif-medium", "serif-bold",
            "sans", "sans-medium", "sans-semibold", "sans-bold",
            "mono", "mono-medium"
        )
        val labels = arrayOf(
            "Playfair Regular", "Playfair Medium", "Playfair Bold",
            "Inter Regular", "Inter Medium", "Inter SemiBold", "Inter Bold",
            "JetBrains Mono", "JetBrains Mono Medium"
        )
        AlertDialog.Builder(this)
            .setTitle("Font family")
            .setItems(labels) { _, which ->
                tvFontFamily.text = options[which]
                updatePreview()
            }
            .show()
    }

    private fun saveTheme() {
        val name = etName.text.toString().trim().ifBlank { "Untitled Theme" }
        try {
            val colors = ThemeColors(
                background = etBackground.text.toString(),
                surface = etSurface.text.toString(),
                text = etText.text.toString(),
                mutedText = etMutedText.text.toString(),
                accent = etAccent.text.toString(),
                border = etBorder.text.toString(),
                selection = etSelection.text.toString(),
                toolbar = etToolbar.text.toString(),
                toolbarText = etToolbarText.text.toString()
            )
            val updated = (workingTheme ?: DefaultThemes.all.first()).copy(
                name = name,
                colors = colors,
                fontFamily = tvFontFamily.text.toString(),
                fontSize = sliderFontSize.value.toInt(),
                lineHeight = sliderLineHeight.value,
                paddingHorizontal = sliderPadH.value.toInt(),
                paddingVertical = sliderPadV.value.toInt(),
                maxWidth = sliderMaxWidth.value.toInt(),
                builtIn = false
            )
            vm.save(updated)
            workingTheme = updated
            Toast.makeText(this, "Theme saved", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid colour value: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
