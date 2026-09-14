package com.example.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import com.example.data.local.AppDatabase
import com.example.data.repository.BlockedNumbersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DialerCallScreeningService : CallScreeningService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: ""
        if (number.isBlank()) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val blockedRepo = BlockedNumbersRepository(database.blockedNumberDao())

        scope.launch {
            val isBlocked = blockedRepo.isNumberBlocked(number)
            val response = if (isBlocked) {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(true)
                    .setSkipNotification(true)
                    .build()
            } else {
                CallResponse.Builder().build()
            }
            respondToCall(callDetails, response)
        }
    }
}
