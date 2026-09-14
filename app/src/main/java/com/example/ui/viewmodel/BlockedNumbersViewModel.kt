package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BlockedNumberEntity
import com.example.data.repository.BlockedNumbersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BlockedUiState(
    val blockedNumbers: List<BlockedNumberEntity> = emptyList(),
    val blockUnknownCallers: Boolean = false,
    val showAddDialog: Boolean = false
)

class BlockedNumbersViewModel(
    private val blockedNumbersRepository: BlockedNumbersRepository
) : ViewModel() {

    private val _blockUnknown = MutableStateFlow(false)
    private val _showAddDialog = MutableStateFlow(false)

    val uiState: StateFlow<BlockedUiState> = combine(
        blockedNumbersRepository.allBlockedNumbers,
        _blockUnknown,
        _showAddDialog
    ) { list, blockUnknown, showDialog ->
        BlockedUiState(
            blockedNumbers = list,
            blockUnknownCallers = blockUnknown,
            showAddDialog = showDialog
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BlockedUiState()
    )

    fun toggleBlockUnknown() {
        _blockUnknown.value = !_blockUnknown.value
    }

    fun openAddDialog() {
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun blockNumber(number: String, name: String? = null) {
        if (number.isNotBlank()) {
            viewModelScope.launch {
                blockedNumbersRepository.blockNumber(number, name)
                _showAddDialog.value = false
            }
        }
    }

    fun unblock(id: Long) {
        viewModelScope.launch {
            blockedNumbersRepository.unblockById(id)
        }
    }
}
