package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.BlockedNumbersRepository
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.DialerPreferencesRepository
import com.example.data.repository.VoicemailRepository
import com.example.telecom.CallManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface AppContainer {
    val database: AppDatabase
    val contactsRepository: ContactsRepository
    val callLogRepository: CallLogRepository
    val blockedNumbersRepository: BlockedNumbersRepository
    val voicemailRepository: VoicemailRepository
    val preferencesRepository: DialerPreferencesRepository
}

class DefaultAppContainer(private val application: Application) : AppContainer {
    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(application)
    }

    override val contactsRepository: ContactsRepository by lazy {
        ContactsRepository(database.contactDao(), application)
    }

    override val callLogRepository: CallLogRepository by lazy {
        CallLogRepository(database.callLogDao(), application)
    }

    override val blockedNumbersRepository: BlockedNumbersRepository by lazy {
        BlockedNumbersRepository(database.blockedNumberDao())
    }

    override val voicemailRepository: VoicemailRepository by lazy {
        VoicemailRepository(database.voicemailDao())
    }

    override val preferencesRepository: DialerPreferencesRepository by lazy {
        DialerPreferencesRepository(application)
    }
}

class DialerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        CallManager.init(container.callLogRepository)

        // Asynchronously check and sync system contacts/call log if permissions are present
        CoroutineScope(Dispatchers.IO).launch {
            container.contactsRepository.syncSystemContacts()
            container.callLogRepository.syncSystemCallLog()
        }
    }
}
