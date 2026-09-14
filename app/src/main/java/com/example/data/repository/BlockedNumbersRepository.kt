package com.example.data.repository

import com.example.data.local.dao.BlockedNumberDao
import com.example.data.local.entity.BlockedNumberEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BlockedNumbersRepository(private val blockedNumberDao: BlockedNumberDao) {
    val allBlockedNumbers: Flow<List<BlockedNumberEntity>> = blockedNumberDao.getAllBlockedNumbers()

    suspend fun isNumberBlocked(number: String): Boolean = withContext(Dispatchers.IO) {
        val cleanNumber = number.replace("[^0-9+]".toRegex(), "")
        blockedNumberDao.isNumberBlocked(cleanNumber) || blockedNumberDao.isNumberBlocked(number)
    }

    suspend fun blockNumber(number: String, name: String? = null): Long = withContext(Dispatchers.IO) {
        blockedNumberDao.insertBlockedNumber(
            BlockedNumberEntity(
                number = number.trim(),
                name = name
            )
        )
    }

    suspend fun unblockNumber(number: String) = withContext(Dispatchers.IO) {
        blockedNumberDao.deleteBlockedNumber(number)
    }

    suspend fun unblockById(id: Long) = withContext(Dispatchers.IO) {
        blockedNumberDao.deleteBlockedNumberById(id)
    }
}
