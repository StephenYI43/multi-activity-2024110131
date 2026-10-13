package com.songjunyi.multiactivity

import android.os.Bundle

class ReceiveActivity : LabActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen(R.string.receive_heading, R.string.receive_subtitle)
        val panel = card()
        label(panel, R.string.received_label, 13f, accent)
        text(panel, intent.getStringExtra(Contract.MESSAGE) ?: getString(R.string.missing_message),
            24f, white, true).apply { id = R.id.received_message }
        footer(savedInstanceState != null)
    }
}

