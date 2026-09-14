package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ContactEntity
import com.example.data.repository.BlockedNumbersRepository
import com.example.data.repository.ContactsRepository
import com.example.telecom.TelecomHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ContactSection(
    val letter: Char,
    val contacts: List<ContactEntity>
)

data class ContactsUiState(
    val sections: List<ContactSection> = emptyList(),
    val favorites: List<ContactEntity> = emptyList(),
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val activeContact: ContactEntity? = null,
    val isEditing: Boolean = false
)

class ContactsViewModel(
    private val contactsRepository: ContactsRepository,
    private val blockedNumbersRepository: BlockedNumbersRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeContact = MutableStateFlow<ContactEntity?>(null)
    private val _isEditing = MutableStateFlow(false)

    val favorites: StateFlow<List<ContactEntity>> = contactsRepository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ContactsUiState> = combine(
        contactsRepository.allContacts,
        favorites,
        _searchQuery,
        _activeContact,
        _isEditing
    ) { contacts, favs, query, active, isEdit ->
        val filtered = if (query.isBlank()) contacts else {
            contacts.filter { c ->
                c.name.contains(query, ignoreCase = true) ||
                    c.phone.contains(query, ignoreCase = true) ||
                    c.email.contains(query, ignoreCase = true)
            }
        }

        val grouped = filtered.groupBy { c ->
            val first = c.name.firstOrNull()?.uppercaseChar() ?: '#'
            if (first in 'A'..'Z') first else '#'
        }.toSortedMap().map { (letter, list) ->
            ContactSection(letter, list)
        }

        ContactsUiState(
            sections = grouped,
            favorites = favs,
            totalCount = filtered.size,
            searchQuery = query,
            activeContact = active,
            isEditing = isEdit
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ContactsUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectContact(contact: ContactEntity?) {
        _activeContact.value = contact
        _isEditing.value = false
    }

    fun startCreateContact() {
        _activeContact.value = ContactEntity(name = "", phone = "")
        _isEditing.value = true
    }

    fun startEditContact(contact: ContactEntity) {
        _activeContact.value = contact
        _isEditing.value = true
    }

    fun cancelEdit() {
        _isEditing.value = false
        if (_activeContact.value?.id == 0L) {
            _activeContact.value = null
        }
    }

    fun saveContact(
        name: String,
        phone: String,
        phoneLabel: String,
        email: String,
        company: String,
        notes: String,
        isFavorite: Boolean
    ) {
        val current = _activeContact.value
        val entity = ContactEntity(
            id = current?.id ?: 0L,
            name = name.trim().ifBlank { "Untitled Contact" },
            phone = phone.trim(),
            phoneLabel = phoneLabel,
            email = email.trim(),
            company = company.trim(),
            notes = notes.trim(),
            isFavorite = isFavorite
        )

        viewModelScope.launch {
            if (entity.id == 0L) {
                val newId = contactsRepository.insertContact(entity)
                _activeContact.value = entity.copy(id = newId)
            } else {
                contactsRepository.updateContact(entity)
                _activeContact.value = entity
            }
            _isEditing.value = false
        }
    }

    fun toggleFavorite(contact: ContactEntity) {
        viewModelScope.launch {
            val updated = contact.copy(isFavorite = !contact.isFavorite)
            contactsRepository.updateContact(updated)
            if (_activeContact.value?.id == contact.id) {
                _activeContact.value = updated
            }
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            contactsRepository.deleteContact(contact)
            if (_activeContact.value?.id == contact.id) {
                _activeContact.value = null
                _isEditing.value = false
            }
        }
    }

    fun blockContact(contact: ContactEntity) {
        viewModelScope.launch {
            blockedNumbersRepository.blockNumber(contact.phone, contact.name)
        }
    }

    fun callContact(context: Context, contact: ContactEntity) {
        TelecomHelper.placeCall(context, contact.phone, contact.name)
    }

    fun exportVCard(): String {
        val contacts = uiState.value.sections.flatMap { it.contacts }
        return contactsRepository.exportVCard(contacts)
    }
}
