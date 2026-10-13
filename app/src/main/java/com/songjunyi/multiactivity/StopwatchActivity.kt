package com.songjunyi.multiactivity

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class StopwatchActivity : LabActivity() {
    private var running = false
    private var accumulated = 0L
    private var startedAt = 0L
    private var laps = arrayListOf<Long>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timer: TextView
    private lateinit var status: TextView
    private lateinit var toggle: Button
    private lateinit var lapButton: Button
    private lateinit var lapList: LinearLayout
    private val ticker = object : Runnable {
        override fun run() {
            refresh()
            if (running) handler.postDelayed(this, 40)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            running = it.getBoolean("running")
            accumulated = it.getLong("accumulated")
            startedAt = it.getLong("started_at")
            laps = ArrayList(it.getLongArray("laps")?.toList() ?: emptyList())
        }
        screen(R.string.stopwatch_heading, R.string.stopwatch_subtitle)
        val panel = card()
        status = label(panel, R.string.timer_ready, 13f, accent).apply { gravity = Gravity.CENTER }
        timer = text(panel, formatTime(elapsed()), 38f, white, true).apply {
            id = R.id.timer_value
            gravity = Gravity.CENTER
            typeface = Typeface.MONOSPACE
        }
        toggle = button(panel, R.string.start).apply {
            id = R.id.timer_toggle
            setOnClickListener {
                if (running) {
                    accumulated = elapsed()
                    running = false
                    handler.removeCallbacks(ticker)
                } else {
                    startedAt = SystemClock.elapsedRealtime()
                    running = true
                    handler.removeCallbacks(ticker)
                    handler.post(ticker)
                }
                refresh()
            }
        }
        button(panel, R.string.reset, false).apply {
            id = R.id.timer_reset
            setOnClickListener {
                running = false
                accumulated = 0L
                startedAt = 0L
                laps.clear()
                handler.removeCallbacks(ticker)
                showLaps()
                refresh()
            }
        }
        lapButton = button(panel, R.string.lap, false).apply {
            id = R.id.timer_lap
            setOnClickListener { if (running) { laps.add(elapsed()); showLaps() } }
        }
        label(panel, R.string.timer_note, 13f, muted)
        val listCard = card()
        label(listCard, R.string.lap_label, 17f, white, true)
        lapList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        listCard.addView(lapList)
        showLaps()
        refresh()
        footer(savedInstanceState != null)
    }

    private fun elapsed() = accumulated + if (running)
        (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0) else 0L

    private fun formatTime(ms: Long): String {
        val seconds = ms / 1000
        return getString(R.string.timer_format, seconds / 3600, seconds / 60 % 60,
            seconds % 60, ms / 10 % 100)
    }

    private fun refresh() {
        timer.text = formatTime(elapsed())
        status.setText(if (running) R.string.timer_running else
            if (accumulated > 0L) R.string.timer_paused else R.string.timer_ready)
        toggle.setText(if (running) R.string.pause else
            if (accumulated > 0L) R.string.resume else R.string.start)
        lapButton.isEnabled = running
        lapButton.alpha = if (running) 1f else 0.45f
    }

    private fun showLaps() {
        lapList.removeAllViews()
        if (laps.isEmpty()) label(lapList, R.string.lap_empty, 13f, muted)
        else laps.asReversed().forEachIndexed { index, time ->
            text(lapList, getString(R.string.lap_format, laps.size - index, formatTime(time)), 14f)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(ticker)
        if (running) handler.post(ticker) else refresh()
    }
    override fun onPause() {
        handler.removeCallbacks(ticker)
        super.onPause()
    }
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_SPACE) {
            if (event.action == KeyEvent.ACTION_UP) toggle.performClick()
            return true
        }
        return super.dispatchKeyEvent(event)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("running", running)
        outState.putLong("accumulated", accumulated)
        outState.putLong("started_at", startedAt)
        outState.putLongArray("laps", laps.toLongArray())
        super.onSaveInstanceState(outState)
    }
}

