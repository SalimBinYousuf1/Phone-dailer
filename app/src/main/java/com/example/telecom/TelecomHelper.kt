package com.example.telecom

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat

object TelecomHelper {

    fun isDefaultDialer(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) ?: false
        } else {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            telecomManager?.defaultDialerPackage == context.packageName
        }
    }

    fun createDefaultDialerIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    context.packageName
                )
            }
        }
    }

    fun placeCall(
        context: Context,
        number: String,
        contactName: String? = null,
        simSlot: Int = 0
    ) {
        val cleanNumber = number.trim()
        if (cleanNumber.isEmpty()) return

        val uri = Uri.fromParts("tel", cleanNumber, null)
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        // Start call tracking in CallManager
        CallManager.startOutgoingCall(cleanNumber, contactName)

        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        if (hasCallPermission && telecomManager != null) {
            try {
                telecomManager.placeCall(uri, null)
                return
            } catch (_: SecurityException) {
                // Fallback to ACTION_CALL or ACTION_DIAL
            }
        }

        val action = if (hasCallPermission) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val intent = Intent(action, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun formatNumberAsTyped(raw: String): String {
        val digits = raw.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        if (digits.startsWith("*") || digits.startsWith("#")) {
            return digits // USSD code
        }
        if (digits.length <= 3) return digits
        if (digits.length <= 7) return "${digits.substring(0, 3)}-${digits.substring(3)}"
        if (digits.length <= 10) {
            return "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
        }
        if (digits.startsWith("+")) {
            return digits
        }
        return digits
    }
}
