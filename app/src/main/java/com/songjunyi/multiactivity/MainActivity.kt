package com.songjunyi.multiactivity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView

@Suppress("DEPRECATION")
class MainActivity : LabActivity() {
    private lateinit var messageInput: EditText
    private lateinit var resultView: TextView
    private var lastResult: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(R.string.main_title, R.string.main_subtitle, false)
        lastResult = savedInstanceState?.getString(Contract.LAST_RESULT)
        val messageCard = card()
        label(messageCard, R.string.outbound_label, 18f, white, true)
        messageInput = input(messageCard, R.string.message_hint, R.id.message_input)
        if (savedInstanceState == null) messageInput.setText(R.string.default_message)
        button(messageCard, R.string.receive_title).apply {
            id = R.id.open_receive
            setOnClickListener {
                val message = messageInput.text.toString().trim()
                if (message.isEmpty()) messageInput.error = getString(R.string.empty_message)
                else startActivity(Intent(this@MainActivity, ReceiveActivity::class.java)
                    .putExtra(Contract.MESSAGE, message))
            }
        }
        label(messageCard, R.string.receive_description, 13f, muted)

        val resultCard = card()
        button(resultCard, R.string.result_title).apply {
            id = R.id.open_result
            setOnClickListener {
                startActivityForResult(Intent(this@MainActivity, ResultActivity::class.java),
                    Contract.RESULT_REQUEST)
            }
        }
        label(resultCard, R.string.result_description, 13f, muted)
        label(resultCard, R.string.return_label, 12f, accent)
        resultView = label(resultCard, R.string.no_result).apply { id = R.id.returned_result }
        updateResult()

        val tools = card()
        button(tools, R.string.camera_title).apply {
            id = R.id.open_camera
            setOnClickListener { startActivity(Intent(this@MainActivity, CameraActivity::class.java)) }
        }
        label(tools, R.string.camera_description, 13f, muted)
        button(tools, R.string.stopwatch_title, false).apply {
            id = R.id.open_stopwatch
            setOnClickListener { startActivity(Intent(this@MainActivity, StopwatchActivity::class.java)) }
        }
        label(tools, R.string.stopwatch_description, 13f, muted)
        footer(savedInstanceState != null)
    }

    private fun updateResult() {
        resultView.text = lastResult?.let { getString(R.string.result_format, it) }
            ?: getString(R.string.no_result)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Contract.RESULT_REQUEST) {
            val value = data?.getStringExtra(Contract.RESULT)
            if (resultCode == RESULT_OK && value != null) {
                lastResult = value
                updateResult()
            } else android.widget.Toast.makeText(this, R.string.result_cancelled,
                android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(Contract.LAST_RESULT, lastResult)
        super.onSaveInstanceState(outState)
    }
}

