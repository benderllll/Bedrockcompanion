package com.bedrockcompanion.mobile

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.content.Context
import android.view.Gravity
import android.widget.*
import android.text.InputType

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("bedrock_companion", Context.MODE_PRIVATE) }
    private lateinit var body: LinearLayout
    private lateinit var status: TextView
    private val accent = Color.rgb(85, 220, 155)
    private val bg = Color.rgb(15, 19, 26)
    private val panel = Color.rgb(27, 34, 44)
    private val textColor = Color.rgb(238, 243, 249)

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showPage("Home") }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun label(s: String, size: Float = 16f) = TextView(this).apply {
        text = s; textSize = size; setTextColor(textColor); setPadding(dp(4), dp(5), dp(4), dp(5))
    }
    private fun button(s: String, action: () -> Unit) = Button(this).apply {
        text = s; isAllCaps = false; setTextColor(textColor); setBackgroundColor(panel); setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(8) }
    }
    private fun input(h: String, v: String = "") = EditText(this).apply {
        hint = h; setText(v); setTextColor(textColor); setHintTextColor(Color.LTGRAY); setSingleLine(true)
        setPadding(dp(12), dp(8), dp(12), dp(8)); setBackgroundColor(panel)
        layoutParams = LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(8) }
    }
    private fun card(s: String) { body.addView(label(s, 18f).apply { setTypeface(null, Typeface.BOLD); setPadding(dp(8), dp(12), dp(4), dp(6)) }) }
    private fun message(s: String) { status.text = s; Toast.makeText(this, s, Toast.LENGTH_SHORT).show() }
    private fun showPage(target: String) {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(dp(14), dp(10), dp(14), dp(8)) }
        root.addView(label("BEDROCK  /  COMPANION", 13f).apply { setTextColor(accent); setTypeface(null, Typeface.BOLD) })
        root.addView(label(target, 27f).apply { setTypeface(null, Typeface.BOLD) })
        status = label("Your settings stay on this device.", 12f).apply { setTextColor(Color.LTGRAY) }; root.addView(status)
        val scroll = ScrollView(this)
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(8), 0, dp(8)) }
        scroll.addView(body); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(5), 0, 0) }
        listOf("Home", "Modules", "Build", "HUD", "Settings").forEach { name ->
            nav.addView(Button(this).apply {
                text = name; textSize = 10f; isAllCaps = false; setTextColor(if (name == target) accent else textColor); setBackgroundColor(panel)
                setPadding(dp(2), 0, dp(2), 0); setOnClickListener { showPage(name) }
            }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(3) })
        }
        root.addView(nav); setContentView(root)
        when (target) { "Home" -> home(); "Modules" -> modules(); "Build" -> buildTools(); "HUD" -> hud(); "Settings" -> settings() }
    }
    private fun home() {
        card("Your mobile companion")
        body.addView(label("Plan builds, save coordinates, and customize a local HUD preview. This app does not modify Minecraft or connect to servers."))
        body.addView(button("Open module menu") { showPage("Modules") })
        body.addView(button("Plan a build") { showPage("Build") })
        body.addView(button("Customize HUD preview") { showPage("HUD") })
        card("Quick status")
        val notes = prefs.getString("coords", "") ?: ""
        body.addView(label("Saved coordinate notes: " + if (notes.isBlank()) "0" else notes.lines().size.toString()))
        body.addView(label("Enabled demo modules: " + listOf("coords", "crosshair", "planner").count { prefs.getBoolean("module_" + it, false) }))
    }
    private fun modules() {
        card("Companion modules")
        val items = listOf(Triple("Coordinate notebook", "coords", "Save locations as notes"), Triple("Build planner", "planner", "Estimate materials"), Triple("Crosshair preview", "crosshair", "Preview a crosshair in this app"), Triple("HUD layout preview", "hud", "Adjust a sample HUD overlay"), Triple("Dark theme", "dark", "Use the dark companion theme"))
        val search = input("Search modules"); body.addView(search)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; body.addView(list)
        fun refresh(q: String = "") {
            list.removeAllViews()
            items.filter { it.first.contains(q, true) || it.third.contains(q, true) }.forEach { item ->
                list.addView(CheckBox(this).apply {
                    text = item.first + "\n" + item.third; textSize = 14f; setTextColor(textColor)
                    buttonTintList = android.content.res.ColorStateList.valueOf(accent)
                    isChecked = prefs.getBoolean("module_" + item.second, false)
                    setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("module_" + item.second, checked).apply(); message(item.first + if (checked) " enabled" else " disabled") }
                    setPadding(dp(8), dp(5), dp(8), dp(5))
                })
            }
        }
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refresh(s?.toString() ?: "") }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        refresh()
    }
    private fun buildTools() {
        card("Material calculator"); body.addView(label("Enter dimensions of a rectangular build."))
        val width = input("Width (blocks)", prefs.getString("width", "10") ?: "10")
        val height = input("Height (blocks)", prefs.getString("height", "4") ?: "4")
        val depth = input("Depth (blocks)", prefs.getString("depth", "10") ?: "10")
        listOf(width, height, depth).forEach { it.inputType = InputType.TYPE_CLASS_NUMBER }
        body.addView(width); body.addView(height); body.addView(depth)
        val result = label("Estimated blocks: —", 17f); body.addView(result)
        body.addView(button("Calculate blocks") {
            val w = width.text.toString().toIntOrNull(); val h = height.text.toString().toIntOrNull(); val d = depth.text.toString().toIntOrNull()
            if (w == null || h == null || d == null || w <= 0 || h <= 0 || d <= 0 || w.toLong() * h * d > 100000000L) message("Enter positive dimensions under 100 million blocks.")
            else { result.text = "Estimated blocks: " + (w.toLong() * h * d); prefs.edit().putString("width", "$w").putString("height", "$h").putString("depth", "$d").apply() }
        })
        card("Coordinate notebook")
        val coords = input("Example: Base: 120, 64, -30"); body.addView(coords)
        body.addView(button("Save coordinate note") {
            val note = coords.text.toString().trim()
            if (note.isEmpty()) message("Type a coordinate note first.") else {
                val old = prefs.getString("coords", "") ?: ""
                prefs.edit().putString("coords", if (old.isBlank()) note else old + "\n" + note).apply()
                coords.setText(""); message("Coordinate note saved"); showPage("Build")
            }
        })
        val saved = prefs.getString("coords", "") ?: ""
        body.addView(label(if (saved.isBlank()) "No saved notes yet." else "Saved notes:\n" + saved))
        if (saved.isNotBlank()) body.addView(button("Clear all coordinate notes") { prefs.edit().remove("coords").apply(); message("Notes cleared"); showPage("Build") })
    }
    private fun hud() {
        card("HUD editor preview"); body.addView(label("Resize the sample overlay. Changes save locally."))
        val sample = label("✛   00:00   XYZ: 120, 64, -30", 14f).apply { gravity = Gravity.CENTER; setBackgroundColor(panel); setPadding(dp(12), dp(14), dp(12), dp(14)) }
        body.addView(sample)
        val sizeLabel = label("Preview scale: " + prefs.getInt("hud_scale", 100) + "%"); body.addView(sizeLabel)
        body.addView(SeekBar(this).apply {
            max = 150; progress = prefs.getInt("hud_scale", 100) - 50
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) { val scale = p + 50; sizeLabel.text = "Preview scale: " + scale + "%"; sample.textSize = 14f * scale / 100f; prefs.edit().putInt("hud_scale", scale).apply() }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) { message("HUD preview scale saved") }
            })
        })
        card("Crosshair style")
        val styles = arrayOf("Plus", "Dot", "Circle", "Square"); val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, styles)
        spinner.setSelection(styles.indexOf(prefs.getString("crosshair_style", "Plus")).coerceAtLeast(0)); body.addView(spinner)
        body.addView(button("Save crosshair style") {
            val selected = spinner.selectedItem.toString(); prefs.edit().putString("crosshair_style", selected).apply()
            val glyph = when (selected) { "Dot" -> "•"; "Circle" -> "◎"; "Square" -> "□"; else -> "✛" }
            sample.text = glyph + "   00:00   XYZ: 120, 64, -30"; message("Crosshair preview saved")
        })
    }
    private fun settings() {
        card("Local preferences"); body.addView(label("Preferences are stored on this phone using Android app storage."))
        body.addView(button("Reset all settings") { prefs.edit().clear().apply(); message("Settings reset"); showPage("Settings") })
        body.addView(button("About this app") { message("Bedrock Companion — build planner and local preview tools.") })
        body.addView(label("This is a companion planner, not a Minecraft client or multiplayer modification."))
    }
}
