package com.primaloptima.scribe

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.primaloptima.scribe.util.ThemeManager

class GuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        supportActionBar?.title = "Guide"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        applyThemeToBars()

        val webView = findViewById<WebView>(R.id.webview_guide)
        webView.settings.javaScriptEnabled = false
        webView.loadDataWithBaseURL(null, guideHtml(), "text/html", "UTF-8", null)
    }

    private fun applyThemeToBars() {
        val theme = (application as ScribeApp).themeManager.activeTheme()
        window.statusBarColor = ThemeManager.parseColor(theme.colors.toolbar)
        supportActionBar?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(ThemeManager.parseColor(theme.colors.toolbar))
        )
    }

    private fun guideHtml(): String = """
<!DOCTYPE html><html><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
body{font-family:Georgia,serif;max-width:680px;margin:24px auto;padding:0 20px;line-height:1.7;color:#2a2622;background:#f5efe4}
h1{font-size:22px;margin-top:32px}h2{font-size:17px;margin-top:24px}
code{font-family:monospace;background:#e8dece;padding:2px 5px;border-radius:3px}
blockquote{border-left:3px solid #d8cfb9;margin-left:0;padding-left:16px;color:#7a6f5d}
</style></head><body>
<h1>Scribe — Guide</h1>
<h2>Smart pairs</h2>
<p>Type <code>"</code>, <code>(</code>, <code>[</code>, <code>{</code>, or <code>'</code> and Scribe inserts the matching close character.
The cursor lands between them. Press <b>Enter</b> while the cursor is before a closing character and it skips past instead of inserting a newline.
<b>Backspace</b> between an empty pair deletes both.</p>
<h2>Shortcut bar</h2>
<p>The bar above the keyboard holds one-tap formatting: Undo, Redo, and any shortcuts you set up.
Tap <b>+</b> to add your own. Each shortcut can insert text, wrap a selection, or insert a matched pair.</p>
<h2>Files panel</h2>
<p>Swipe from the <b>left edge</b> to open the file panel (or tap the ☰ button).
Tap <b>Connect folder</b> to read and write files directly from your phone storage using Android's secure folder picker.</p>
<h2>Right panel</h2>
<p>Swipe from the <b>right edge</b> to open the outline panel. It lists all headings in the active note so you can jump to any section instantly.</p>
<h2>Themes</h2>
<p>Open <b>Menu → Themes</b> to switch or create themes. Five built-in themes: Paper, Midnight, Sepia, Typewriter, Focus.
Duplicate a theme to customise fonts, colours, spacing, and background images.</p>
<h2>Zen mode</h2>
<p><b>Double-tap</b> the editor to enter Zen mode — all chrome disappears. Double-tap again, or tap the exit button, to return.</p>
<h2>History</h2>
<p>Every three minutes (or whenever your word count changes by 40+), Scribe saves a snapshot.
Open <b>Menu → History</b> to browse snapshots and restore any version.</p>
<h2>Export</h2>
<p>Open the <b>⋮ menu → Export</b> to share a note as .txt, .md, .html, PDF, EPUB, or .docx.</p>
<h2>World building</h2>
<p>Open <b>Menu → Sheets</b> to create character and location sheets for your projects.</p>
</body></html>""".trimIndent()

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
