package com.cookeduitv.launcher

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(9, 12, 24)
    private val panel = Color.rgb(24, 31, 52)
    private val cyan = Color.rgb(55, 224, 231)
    private val white = Color.rgb(247, 249, 255)
    private val muted = Color.rgb(164, 176, 202)
    private lateinit var appsRow: LinearLayout
    private val cards = mutableListOf<View>()
    private val appItems = mutableListOf<ResolveInfo>()
    private var selected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
            isFillViewport = true
            isVerticalScrollBarEnabled = false
        }
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(48), dp(24), dp(48), dp(32))
            setBackgroundColor(bg)
        }
        scroll.addView(page)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(label("COOKED", 23f, cyan, true), LinearLayout.LayoutParams(0, dp(42), 1f))
        header.addView(label(SimpleDateFormat("EEE, d MMM  •  h:mm a", Locale.getDefault()).format(Date()), 15f, muted, false))
        page.addView(header)
        page.addView(label("Your screen. Your style.", 34f, white, true), margin(0, 25, 0, 5))
        page.addView(label("Your favourite apps, all in one place.", 17f, muted, false), margin(0, 0, 0, 24))

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(26), dp(19), dp(26), dp(19))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.rgb(18, 72, 91), Color.rgb(29, 42, 77), Color.rgb(47, 31, 72))
            ).apply { cornerRadius = dp(22).toFloat() }
        }
        hero.addView(label("WELCOME HOME", 12f, cyan, true))
        hero.addView(label("What are we watching?", 25f, white, true), margin(0, 6, 0, 4))
        hero.addView(label("Pick an app and press OK to open it.", 15f, muted, false))
        page.addView(hero, margin(0, 0, 0, 25))
        page.addView(label("YOUR APPS", 14f, cyan, true), margin(0, 0, 0, 8))
        page.addView(label("Use LEFT and RIGHT on your remote. Press OK to launch.", 14f, muted, false), margin(0, 0, 0, 14))

        val horizontal = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            clipToPadding = false
            isFillViewport = false
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
        appsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        horizontal.addView(appsRow)
        page.addView(horizontal, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(174)))
        page.addView(label("COOKED UI TV  •  MADE FOR THE BIG SCREEN", 11f, muted, true), margin(0, 22, 0, 0))
        setContentView(scroll)
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        if (::appsRow.isInitialized) loadApps()
    }

    private fun loadApps() {
        if (!::appsRow.isInitialized) return
        appsRow.removeAllViews()
        cards.clear()
        appItems.clear()
        val pm = packageManager
        val intents = listOf(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        )
        val found = linkedMapOf<String, ResolveInfo>()
        intents.forEach { intent ->
            pm.queryIntentActivities(intent, 0).forEach { info ->
                if (info.activityInfo.packageName != packageName) found.putIfAbsent(info.activityInfo.packageName, info)
            }
        }
        val sorted = found.values.sortedBy { it.loadLabel(pm).toString().lowercase(Locale.getDefault()) }
        if (sorted.isEmpty()) {
            appsRow.addView(label("No launchable apps found. Try reopening Cooked UI TV.", 18f, muted, false))
            return
        }
        appItems.addAll(sorted)
        sorted.forEachIndexed { index, info ->
            val active = index == selected
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(12), dp(12), dp(12))
                isFocusable = true
                isFocusableInTouchMode = true
                isClickable = true
                background = cardBackground(active)
                contentDescription = "Open " + info.loadLabel(pm).toString()
            }
            val icon = ImageView(this).apply {
                setImageDrawable(info.loadIcon(pm))
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = null
            }
            card.addView(icon, LinearLayout.LayoutParams(dp(54), dp(54)).apply { bottomMargin = dp(10) })
            val name = label(info.loadLabel(pm).toString(), 16f, if (active) bg else white, true).apply {
                gravity = Gravity.CENTER
                maxLines = 2
            }
            card.addView(name, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42)))
            card.setOnClickListener { launch(info) }
            card.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    selected = index
                    refreshSelection()
                }
            }
            appsRow.addView(card, LinearLayout.LayoutParams(dp(190), dp(154)).apply { marginEnd = dp(14) })
            cards.add(card)
        }
        selected = selected.coerceIn(0, cards.lastIndex)
        refreshSelection()
        cards.getOrNull(selected)?.requestFocus()
    }

    private fun refreshSelection() {
        cards.forEachIndexed { index, view ->
            val active = index == selected || view.hasFocus()
            view.background = cardBackground(active)
            recolor(view, active)
        }
    }

    private fun recolor(view: View, active: Boolean) {
        if (view is TextView) view.setTextColor(if (active) bg else white)
        if (view is ViewGroup) for (i in 0 until view.childCount) recolor(view.getChildAt(i), active)
    }

    private fun cardBackground(active: Boolean) = GradientDrawable().apply {
        setColor(if (active) cyan else panel)
        cornerRadius = dp(18).toFloat()
        if (active) setStroke(dp(3), white)
    }

    private fun launch(info: ResolveInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(info.activityInfo.packageName, info.activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (_: Exception) {
            val fallback = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
            if (fallback != null) try { startActivity(fallback) } catch (_: Exception) { }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (cards.isNotEmpty()) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    selected = (selected + 1).coerceAtMost(cards.lastIndex)
                    cards[selected].requestFocus()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    selected = (selected - 1).coerceAtLeast(0)
                    cards[selected].requestFocus()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    cards.getOrNull(selected)?.performClick()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun margin(left: Int, top: Int, right: Int, bottom: Int) =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(dp(left), dp(top), dp(right), dp(bottom))
        }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
