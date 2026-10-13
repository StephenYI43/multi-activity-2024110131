package com.songjunyi.multiactivity

import android.content.Intent
import android.os.Bundle

class ResultActivity : LabActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(R.string.result_heading, R.string.result_subtitle)
        val panel = card()
        label(panel, R.string.result_label, 18f, white, true)
        val field = input(panel, R.string.result_hint, R.id.result_input)
        button(panel, R.string.submit_result).apply {
            id = R.id.submit_result
            setOnClickListener {
                val value = field.text.toString().trim()
                if (value.isEmpty()) field.error = getString(R.string.empty_message)
                else {
                    setResult(RESULT_OK, Intent().putExtra(Contract.RESULT, value))
                    finish()
                }
            }
        }
        button(panel, R.string.cancel_result, false).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        footer(savedInstanceState != null)
    }
}

