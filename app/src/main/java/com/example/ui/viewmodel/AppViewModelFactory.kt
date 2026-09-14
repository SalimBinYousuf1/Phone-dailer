package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.AppContainer

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DialerViewModel::class.java) -> {
                DialerViewModel(
                    contactsRepository = container.contactsRepository,
                    callLogRepository = container.callLogRepository,
                    preferencesRepository = container.preferencesRepository,
                    blockedNumbersRepository = container.blockedNumbersRepository
                ) as T
            }
            modelClass.isAssignableFrom(RecentsViewModel::class.java) -> {
                RecentsViewModel(
                    callLogRepository = container.callLogRepository,
                    contactsRepository = container.contactsRepository,
                    blockedNumbersRepository = container.blockedNumbersRepository
                ) as T
            }
            modelClass.isAssignableFrom(ContactsViewModel::class.java) -> {
                ContactsViewModel(
                    contactsRepository = container.contactsRepository,
                    blockedNumbersRepository = container.blockedNumbersRepository
                ) as T
            }
            modelClass.isAssignableFrom(VoicemailViewModel::class.java) -> {
                VoicemailViewModel(
                    voicemailRepository = container.voicemailRepository
                ) as T
            }
            modelClass.isAssignableFrom(BlockedNumbersViewModel::class.java) -> {
                BlockedNumbersViewModel(
                    blockedNumbersRepository = container.blockedNumbersRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    preferencesRepository = container.preferencesRepository,
                    callLogRepository = container.callLogRepository,
                    contactsRepository = container.contactsRepository
                ) as T
            }
            modelClass.isAssignableFrom(InCallViewModel::class.java) -> {
                InCallViewModel(
                    preferencesRepository = container.preferencesRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
