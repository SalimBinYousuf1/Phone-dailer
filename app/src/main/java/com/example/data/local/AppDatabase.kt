package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BlockedNumberDao
import com.example.data.local.dao.CallLogDao
import com.example.data.local.dao.ContactDao
import com.example.data.local.dao.VoicemailDao
import com.example.data.local.entity.BlockedNumberEntity
import com.example.data.local.entity.CallLogEntity
import com.example.data.local.entity.ContactEntity
import com.example.data.local.entity.VoicemailEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ContactEntity::class,
        CallLogEntity::class,
        BlockedNumberEntity::class,
        VoicemailEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun voicemailDao(): VoicemailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dialer_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate with initial contacts, call logs and voicemails
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val contactDao = database.contactDao()
            val callLogDao = database.callLogDao()
            val blockedNumberDao = database.blockedNumberDao()
            val voicemailDao = database.voicemailDao()

            val initialContacts = listOf(
                ContactEntity(
                    name = "Alex Vance",
                    phone = "+1 555-0192",
                    phoneLabel = "Mobile",
                    email = "alex@vercel.com",
                    company = "Vercel",
                    notes = "Engineering lead",
                    isFavorite = true
                ),
                ContactEntity(
                    name = "Brian Chen",
                    phone = "+1 555-0143",
                    phoneLabel = "Work",
                    email = "brian@linear.app",
                    company = "Linear",
                    notes = "Product sync on Tuesdays",
                    isFavorite = true
                ),
                ContactEntity(
                    name = "Elena Rostova",
                    phone = "+1 555-0178",
                    phoneLabel = "Mobile",
                    email = "elena@nextjs.org",
                    company = "Next.js Core",
                    notes = "Turbopack architect",
                    isFavorite = false
                ),
                ContactEntity(
                    name = "Guillermo Rauch",
                    phone = "+1 555-0100",
                    phoneLabel = "Mobile",
                    email = "rauchg@vercel.com",
                    company = "Vercel",
                    notes = "Design review & deployments",
                    isFavorite = true
                ),
                ContactEntity(
                    name = "Sarah Connor",
                    phone = "+1 555-0129",
                    phoneLabel = "Mobile",
                    email = "sarah@cyberdyne.io",
                    company = "Security Ops",
                    notes = "Infra reliability",
                    isFavorite = false
                ),
                ContactEntity(
                    name = "Thomas Anderson",
                    phone = "+1 555-0184",
                    phoneLabel = "Work",
                    email = "neo@metacortex.com",
                    company = "MetaCortex",
                    notes = "Software Architect",
                    isFavorite = false
                )
            )
            contactDao.insertContacts(initialContacts)

            val now = System.currentTimeMillis()
            val hour = 3600 * 1000L
            val day = 24 * hour

            val initialCallLogs = listOf(
                CallLogEntity(
                    number = "+1 555-0100",
                    name = "Guillermo Rauch",
                    callType = CallLogEntity.TYPE_INCOMING,
                    timestamp = now - (15 * 60 * 1000L),
                    durationSeconds = 342,
                    simSlot = 0
                ),
                CallLogEntity(
                    number = "+1 555-0192",
                    name = "Alex Vance",
                    callType = CallLogEntity.TYPE_OUTGOING,
                    timestamp = now - (2 * hour),
                    durationSeconds = 118,
                    simSlot = 0
                ),
                CallLogEntity(
                    number = "+1 555-0999",
                    name = null,
                    callType = CallLogEntity.TYPE_MISSED,
                    timestamp = now - (4 * hour),
                    durationSeconds = 0,
                    simSlot = 1
                ),
                CallLogEntity(
                    number = "+1 555-0143",
                    name = "Brian Chen",
                    callType = CallLogEntity.TYPE_INCOMING,
                    timestamp = now - day,
                    durationSeconds = 540,
                    simSlot = 0
                ),
                CallLogEntity(
                    number = "+1 555-0420",
                    name = "Unknown Telemarketer",
                    callType = CallLogEntity.TYPE_BLOCKED,
                    timestamp = now - (2 * day),
                    durationSeconds = 0,
                    simSlot = 0
                )
            )
            callLogDao.insertCallLogs(initialCallLogs)

            blockedNumberDao.insertBlockedNumber(
                BlockedNumberEntity(
                    number = "+1 555-0420",
                    name = "Spam Caller #42"
                )
            )

            voicemailDao.insertVoicemail(
                VoicemailEntity(
                    number = "+1 555-0192",
                    name = "Alex Vance",
                    timestamp = now - (3 * hour),
                    durationSeconds = 42,
                    transcript = "Hey, just following up on the edge middleware deployment. Give me a call back when you can.",
                    isRead = false
                )
            )
        }
    }
}
