package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.ContactEntity
import com.example.data.repository.BlockedNumbersRepository
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.telecom.TelecomHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CallFilter {
    ALL,
    MISSED,
    INCOMING,
    OUTGOING,
    BLOCKED
}

data class RecentsGroup(
    val header: String,
    val items: List<CallLogEntity>
)

data class RecentsUiState(
    val groups: List<RecentsGroup> = emptyList(),
    val totalCount: Int = 0,
    val selectedFilter: CallFilter = CallFilter.ALL,
    val searchQuery: String = "",
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val activeDetailCall: CallLogEntity? = null
)

class RecentsViewModel(
    private val callLogRepository: CallLogRepository,
    private val contactsRepository: ContactsRepository,
    private val blockedNumbersRepository: BlockedNumbersRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(CallFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _activeDetailCall = MutableStateFlow<CallLogEntity?>(null)

    val uiState: StateFlow<RecentsUiState> = combine(
        callLogRepository.allCallLogs,
        _selectedFilter,
        _searchQuery,
        _selectedIds,
        _activeDetailCall
    ) { logs, filter, query, selected, detailCall ->
        val filtered = logs.filter { log ->
            val matchesFilter = when (filter) {
                CallFilter.ALL -> true
                CallFilter.MISSED -> log.callType == CallLogEntity.TYPE_MISSED
                CallFilter.INCOMING -> log.callType == CallLogEntity.TYPE_INCOMING
                CallFilter.OUTGOING -> log.callType == CallLogEntity.TYPE_OUTGOING
                CallFilter.BLOCKED -> log.callType == CallLogEntity.TYPE_BLOCKED
            }
            val matchesQuery = if (query.isBlank()) true else {
                log.number.contains(query, ignoreCase = true) ||
                    (log.name?.contains(query, ignoreCase = true) == true)
            }
            matchesFilter && matchesQuery
        }

        val groups = groupLogsByDate(filtered)

        RecentsUiState(
            groups = groups,
            totalCount = filtered.size,
            selectedFilter = filter,
            searchQuery = query,
            isSelectionMode = selected.isNotEmpty(),
            selectedIds = selected,
            activeDetailCall = detailCall
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RecentsUiState()
    )

    fun setFilter(filter: CallFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedIds.value = current
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val ids = _selectedIds.value.toList()
        if (ids.isNotEmpty()) {
            viewModelScope.launch {
                callLogRepository.deleteCallLogsByIds(ids)
                _selectedIds.value = emptySet()
            }
        }
    }

    fun deleteCall(callLog: CallLogEntity) {
        viewModelScope.launch {
            callLogRepository.deleteCallLog(callLog)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            callLogRepository.clearAllCallLogs()
        }
    }

    fun openDetail(call: CallLogEntity) {
        _activeDetailCall.value = call
    }

    fun closeDetail() {
        _activeDetailCall.value = null
    }

    fun callContact(context: Context, number: String, name: String? = null) {
        TelecomHelper.placeCall(context, number, name)
    }

    fun blockNumber(number: String, name: String? = null) {
        viewModelScope.launch {
            blockedNumbersRepository.blockNumber(number, name)
        }
    }

    fun addAsContact(number: String, defaultName: String = "New Contact") {
        viewModelScope.launch {
            contactsRepository.insertContact(
                ContactEntity(
                    name = defaultName,
                    phone = number
                )
            )
        }
    }

    fun exportCsv(): String {
        val allLogs = uiState.value.groups.flatMap { it.items }
        return callLogRepository.exportCsv(allLogs)
    }

    private fun groupLogsByDate(logs: List<CallLogEntity>): List<RecentsGroup> {
        val todayFmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val now = System.currentTimeMillis()
        val todayStr = todayFmt.format(Date(now))
        val yesterdayStr = todayFmt.format(Date(now - 24 * 3600 * 1000L))

        val map = linkedMapOf<String, MutableList<CallLogEntity>>()
        for (log in logs) {
            val logDay = todayFmt.format(Date(log.timestamp))
            val header = when (logDay) {
                todayStr -> "TODAY"
                yesterdayStr -> "YESTERDAY"
                else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(log.timestamp)).uppercase()
            }
            map.getOrPut(header) { mutableListOf() }.add(log)
        }
        return map.map { (k, v) -> RecentsGroup(k, v) }
    }
}
