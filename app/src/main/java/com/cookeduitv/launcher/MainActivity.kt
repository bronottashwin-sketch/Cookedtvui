package com.cookeduitv.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(10, 12, 22)
    private val panel = Color.rgb(24, 29, 47)
    private val cyan = Color.rgb(49, 213, 224)
    private val white = Color.rgb(246, 247, 255)
    private val muted = Color.rgb(164, 172, 195)
    private lateinit var list: LinearLayout
    private val cards = mutableListOf<View>()
    private var selected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); isFillViewport = true }
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(42), dp(28), dp(42), dp(28)); setBackgroundColor(bg) }
        scroll.addView(page)
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(label("COOKED UI  /  TV", 18f, cyan, true), LinearLayout.LayoutParams(0, dp(38), 1f))
        val clock = label(SimpleDateFormat("EEE, d MMM  •  h:mm a", Locale.getDefault()).format(Date()), 15f, muted, false)
        header.addView(clock)
        page.addView(header)
        page.addView(label("Your TV. Your rules.", 34f, white, true), margin(0, 24, 0, 4))
        page.addView(label("Pick something to watch", 16f, muted, false), margin(0, 0, 0, 24))
        page.addView(label("YOUR APPS", 13f, cyan, true), margin(0, 0, 0, 12))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(list)
        setContentView(scroll)
        loadApps()
    }

    private fun loadApps() {
        if (!::list.isInitialized) return
        list.removeAllViews(); cards.clear()
        val pm = packageManager
        val intents = listOf(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER))
        val found = linkedMapOf<String, android.content.pm.ResolveInfo>()
        intents.forEach { intent -> pm.queryIntentActivities(intent, 0).forEach { info -> if (info.activityInfo.packageName != packageName) found.putIfAbsent(info.activityInfo.packageName, info) } }
        if (found.isEmpty()) {
            list.addView(label("No apps found yet. Install your streaming apps, then reopen Cooked UI TV.", 18f, muted, false), margin(0, 12, 0, 0))
            return
        }
        found.values.sortedBy { it.loadLabel(pm).toString().lowercase() }.forEachIndexed { index, info ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(18), dp(13), dp(18), dp(13)); isFocusable = true; isFocusableInTouchMode = true
                background = rounded(if (index == selected) cyan else panel, 18)
                addView(label("▶", 18f, if (index == selected) bg else cyan, true), LinearLayout.LayoutParams(dp(42), dp(42)))
                val text = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
                text.addView(label(info.loadLabel(pm).toString(), 19f, if (index == selected) bg else white, true))
                text.addView(label(info.activityInfo.packageName, 11f, if (index == selected) bg else muted, false))
                addView(text, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(label("OPEN  ›", 12f, if (index == selected) bg else muted, true))
                setOnClickListener { launch(info.activityInfo.packageName) }
                setOnFocusChangeListener { _, hasFocus -> if (hasFocus) { selected = index; refreshSelection() } }
            }
            list.addView(card, margin(0, 0, 0, 10)); cards.add(card)
        }
        if (cards.isNotEmpty()) cards[0].requestFocus()
    }

    private fun refreshSelection() { cards.forEachIndexed { i, v -> v.background = rounded(if (i == selected) cyan else panel, 18); recolor(v, i == selected) } }
    private fun recolor(view: View, active: Boolean) { if (view is TextView) view.setTextColor(if (active) bg else white); if (view is ViewGroup) for (i in 0 until view.childCount) recolor(view.getChildAt(i), active) }
    private fun launch(pkg: String) { try { startActivity(packageManager.getLaunchIntentForPackage(pkg) ?: return) } catch (_: Exception) {} }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (cards.isNotEmpty()) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { selected = (selected + 1).coerceAtMost(cards.lastIndex); cards[selected].requestFocus(); return true }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { selected = (selected - 1).coerceAtLeast(0); cards[selected].requestFocus(); return true }
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) { cards.getOrNull(selected)?.performClick(); return true }
        }
        return super.onKeyDown(keyCode, event)
    }
    private fun label(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply { text = value; textSize = size; setTextColor(color); typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL); gravity = Gravity.CENTER_VERTICAL }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun margin(l: Int, t: Int, r: Int, b: Int) = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(dp(l), dp(t), dp(r), dp(b)) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
