package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ContactEntity
import com.example.data.repository.BlockedNumbersRepository
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.DialerPreferencesRepository
import com.example.telecom.CallManager
import com.example.telecom.TelecomHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DialerUiState(
    val enteredNumber: String = "",
    val formattedNumber: String = "",
    val selectedSim: Int = 0, // 0: SIM 1, 1: SIM 2
    val matchedContact: ContactEntity? = null,
    val ussdResponse: String? = null,
    val hapticsEnabled: Boolean = true,
    val dialpadTonesEnabled: Boolean = true
)

class DialerViewModel(
    private val contactsRepository: ContactsRepository,
    private val callLogRepository: CallLogRepository,
    private val preferencesRepository: DialerPreferencesRepository,
    private val blockedNumbersRepository: BlockedNumbersRepository
) : ViewModel() {

    private val _enteredNumber = MutableStateFlow("")
    private val _selectedSim = MutableStateFlow(0)
    private val _ussdResponse = MutableStateFlow<String?>(null)

    val allContacts: StateFlow<List<ContactEntity>> = contactsRepository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val configFlow = combine(
        preferencesRepository.hapticsEnabled,
        preferencesRepository.dialpadTonesEnabled
    ) { haptics, tones -> Pair(haptics, tones) }

    val uiState: StateFlow<DialerUiState> = combine(
        _enteredNumber,
        _selectedSim,
        _ussdResponse,
        allContacts,
        configFlow
    ) { number, sim, ussd, contacts, config ->
        val formatted = TelecomHelper.formatNumberAsTyped(number)
        val matched = if (number.isNotBlank()) {
            findMatch(number, contacts)
        } else {
            null
        }

        DialerUiState(
            enteredNumber = number,
            formattedNumber = formatted,
            selectedSim = sim,
            matchedContact = matched,
            ussdResponse = ussd,
            hapticsEnabled = config.first,
            dialpadTonesEnabled = config.second
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DialerUiState()
    )

    fun onDigitPress(digit: Char) {
        CallManager.playDtmf(digit)
        _enteredNumber.value += digit
    }

    fun onZeroLongPress() {
        CallManager.playDtmf('0')
        _enteredNumber.value += "+"
    }

    fun onBackspace() {
        val current = _enteredNumber.value
        if (current.isNotEmpty()) {
            _enteredNumber.value = current.dropLast(1)
        }
    }

    fun onClearAll() {
        _enteredNumber.value = ""
    }

    fun onPasteNumber(pasted: String) {
        val cleaned = pasted.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        _enteredNumber.value = cleaned
    }

    fun setSim(sim: Int) {
        _selectedSim.value = sim
    }

    fun onSpeedDial(digit: Int, context: Context) {
        // Speed dial logic: 1 is voicemail (*86), 2-9 from contacts
        if (digit == 1) {
            placeCall(context, "*86", "Voicemail")
            return
        }
        val contacts = allContacts.value
        if (contacts.isNotEmpty()) {
            val contact = contacts.getOrNull(digit - 2)
            if (contact != null) {
                placeCall(context, contact.phone, contact.name)
            }
        }
    }

    fun placeCall(context: Context, targetNumber: String? = null, contactName: String? = null) {
        val numberToCall = targetNumber ?: _enteredNumber.value
        if (numberToCall.isBlank()) return

        // Check for USSD code like *#06#
        if (numberToCall == "*#06#") {
            _ussdResponse.value = "Device IMEI:\n867530900142951\nIMEI SV: 02"
            return
        } else if (numberToCall.startsWith("*") && numberToCall.endsWith("#")) {
            _ussdResponse.value = "Carrier USSD Code: $numberToCall\nService request acknowledged. Balance: $42.00 active."
            return
        }

        val name = contactName ?: uiState.value.matchedContact?.name
        TelecomHelper.placeCall(context, numberToCall, name, _selectedSim.value)
    }

    fun dismissUssd() {
        _ussdResponse.value = null
    }

    fun blockCurrentNumber() {
        val number = _enteredNumber.value
        if (number.isNotBlank()) {
            viewModelScope.launch {
                blockedNumbersRepository.blockNumber(number, uiState.value.matchedContact?.name)
            }
        }
    }

    private fun findMatch(digits: String, contacts: List<ContactEntity>): ContactEntity? {
        val cleanDigits = digits.filter { it.isDigit() }
        if (cleanDigits.isEmpty()) return null

        // 1. Direct phone substring match
        val phoneMatch = contacts.find { it.phone.filter { ch -> ch.isDigit() }.contains(cleanDigits) }
        if (phoneMatch != null) return phoneMatch

        // 2. T9 name matching
        val t9Map = mapOf(
            '2' to "abc", '3' to "def", '4' to "ghi", '5' to "jkl",
            '6' to "mno", '7' to "pqrs", '8' to "tuv", '9' to "wxyz"
        )
        return contacts.find { contact ->
            val name = contact.name.lowercase()
            nameMatchesT9(name, cleanDigits, t9Map)
        }
    }

    private fun nameMatchesT9(name: String, digits: String, map: Map<Char, String>): Boolean {
        if (digits.length > name.length) return false
        for (i in digits.indices) {
            val d = digits[i]
            val allowedChars = map[d] ?: ""
            if (!allowedChars.contains(name[i])) {
                return false
            }
        }
        return true
    }
}
