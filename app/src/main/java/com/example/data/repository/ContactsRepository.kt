package com.example.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.data.local.dao.ContactDao
import com.example.data.local.entity.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val contactDao: ContactDao,
    private val context: Context
) {
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()
    val favorites: Flow<List<ContactEntity>> = contactDao.getFavorites()

    fun getContactById(id: Long): Flow<ContactEntity?> = contactDao.getContactById(id)

    fun searchContacts(query: String): Flow<List<ContactEntity>> = contactDao.searchContacts(query)

    suspend fun insertContact(contact: ContactEntity): Long = withContext(Dispatchers.IO) {
        contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: ContactEntity) = withContext(Dispatchers.IO) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: ContactEntity) = withContext(Dispatchers.IO) {
        contactDao.deleteContact(contact)
    }

    suspend fun deleteContactById(id: Long) = withContext(Dispatchers.IO) {
        contactDao.deleteContactById(id)
    }

    suspend fun getContactByPhone(phone: String): ContactEntity? = withContext(Dispatchers.IO) {
        contactDao.getContactByPhone(phone)
    }

    suspend fun syncSystemContacts() = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext
        }

        try {
            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.STARRED
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val typeIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                val starredIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

                val contactsToInsert = mutableListOf<ContactEntity>()
                while (c.moveToNext()) {
                    val name = if (nameIndex >= 0) c.getString(nameIndex) else "Unknown"
                    val number = if (numberIndex >= 0) c.getString(numberIndex) ?: "" else ""
                    val type = if (typeIndex >= 0) c.getInt(typeIndex) else ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    val isStarred = if (starredIndex >= 0) c.getInt(starredIndex) == 1 else false

                    val label = when (type) {
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
                        ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
                        else -> "Mobile"
                    }

                    if (number.isNotBlank()) {
                        contactsToInsert.add(
                            ContactEntity(
                                name = name,
                                phone = number,
                                phoneLabel = label,
                                isFavorite = isStarred
                            )
                        )
                    }
                }

                if (contactsToInsert.isNotEmpty()) {
                    contactDao.insertContacts(contactsToInsert)
                }
            }
        } catch (_: Exception) {
            // Silently continue with local database
        }
    }

    fun exportVCard(contacts: List<ContactEntity>): String {
        val sb = StringBuilder()
        for (c in contacts) {
            sb.append("BEGIN:VCARD\n")
            sb.append("VERSION:3.0\n")
            sb.append("FN:${c.name}\n")
            sb.append("TEL;TYPE=${c.phoneLabel.uppercase()}:${c.phone}\n")
            if (c.email.isNotBlank()) {
                sb.append("EMAIL:${c.email}\n")
            }
            if (c.company.isNotBlank()) {
                sb.append("ORG:${c.company}\n")
            }
            if (c.notes.isNotBlank()) {
                sb.append("NOTE:${c.notes}\n")
            }
            sb.append("END:VCARD\n\n")
        }
        return sb.toString()
    }
}
