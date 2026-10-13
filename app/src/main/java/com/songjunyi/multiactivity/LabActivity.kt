package com.songjunyi.multiactivity

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*

/** Shared visual components. All visible text comes from Android string resources. */
abstract class LabActivity : Activity() {
    protected val accent = Color.rgb(100, 244, 213)
    protected val white = Color.rgb(241, 247, 255)
    protected val muted = Color.rgb(162, 183, 206)
    protected lateinit var content: LinearLayout
    private lateinit var lifecycleLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        event("onCreate")
    }

    protected fun screen(title: Int, subtitle: Int, child: Boolean = true) {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(7, 17, 31), Color.rgb(15, 31, 52), Color.rgb(7, 17, 31)))
        }
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(28))
        }
        val available = resources.configuration.screenWidthDp
        outer.addView(content, LinearLayout.LayoutParams(dp(minOf(available, 680)), -2))
        scroll.addView(outer)
        setContentView(scroll)
        scroll.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= 30) {
                val bars = insets.getInsets(android.view.WindowInsets.Type.systemBars())
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            } else {
                @Suppress("DEPRECATION")
                view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        if (child) button(content, R.string.back, false).setOnClickListener { finish() }
        label(content, R.string.eyebrow, 11f, accent, true).letterSpacing = 0.12f
        label(content, R.string.app_name, 13f, muted)
        label(content, title, 30f, white, true)
        label(content, subtitle, 14f, muted)
        label(content, R.string.identity, 12f, accent)
    }

    protected fun footer(restored: Boolean = false) {
        if (restored) label(content, R.string.lifecycle_restored, 12f, accent)
        lifecycleLabel = label(content, R.string.lifecycle_created, 11f, muted)
    }

    protected fun card(parent: LinearLayout = content): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = shape(Color.rgb(17, 34, 56), 24f, Color.rgb(40, 62, 86))
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(18)
            })
        }
    }

    protected fun label(parent: LinearLayout, textRes: Int, size: Float = 16f,
                        color: Int = white, bold: Boolean = false): TextView =
        text(parent, getString(textRes), size, color, bold)

    protected fun text(parent: LinearLayout, value: CharSequence, size: Float = 16f,
                       color: Int = white, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setLineSpacing(dp(3).toFloat(), 1f)
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(8)
            })
        }
    }

    protected fun input(parent: LinearLayout, hint: Int, viewId: Int): EditText {
        return EditText(this).apply {
            id = viewId
            setHint(hint)
            setTextColor(white)
            setHintTextColor(muted)
            textSize = 16f
            minLines = 2
            maxLines = 5
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = shape(Color.rgb(10, 24, 43), 14f, Color.rgb(51, 78, 101))
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(14)
            })
        }
    }

    protected fun button(parent: LinearLayout, title: Int, primary: Boolean = true): Button {
        return Button(this).apply {
            setText(title)
            isAllCaps = false
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            minHeight = dp(52)
            setTextColor(if (primary) Color.rgb(7, 27, 34) else accent)
            background = shape(if (primary) accent else Color.rgb(22, 45, 64), 15f)
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(13)
            })
        }
    }

    protected fun shape(color: Int, radius: Float, stroke: Int? = null) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }

    protected fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun event(name: String) = Log.d("ActivityLifecycle", javaClass.simpleName + ":" + name)
    override fun onStart() { super.onStart(); event("onStart") }
    override fun onResume() {
        super.onResume()
        event("onResume")
        if (::lifecycleLabel.isInitialized) lifecycleLabel.setText(R.string.lifecycle_active)
    }
    override fun onPause() { event("onPause"); super.onPause() }
    override fun onStop() { event("onStop"); super.onStop() }
    override fun onDestroy() { event("onDestroy"); super.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        event("onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }
}

object Contract {
    const val MESSAGE = "message"
    const val RESULT = "result"
    const val LAST_RESULT = "last_result"
    const val RESULT_REQUEST = 202
    const val CAMERA_REQUEST = 303
}

