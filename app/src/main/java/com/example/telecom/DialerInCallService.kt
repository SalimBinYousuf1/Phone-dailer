package com.example.telecom

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.example.MainActivity

class DialerInCallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.registerTelecomCall(call)

        // Bring dialer to foreground with In-Call Screen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("SHOW_IN_CALL", true)
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.disconnectCall()
    }
}
