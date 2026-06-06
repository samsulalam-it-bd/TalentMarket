package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Job::class, Worker::class], version = 14, exportSchema = false)
@androidx.room.TypeConverters(com.example.data.Converters::class)
abstract class TalentDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun workerDao(): WorkerDao

    private class TalentDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Instance?.let { database ->
                scope.launch {
                    val jobDao = database.jobDao()
                    val workerDao = database.workerDao()
                    
                    // Initialize Room if needed, removed dummy data seeding as we rely on firestore!
                }
            }
        }
    }

    companion object {
        @Volatile
        private var Instance: TalentDatabase? = null

        fun getDatabase(context: Context): TalentDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, TalentDatabase::class.java, "talent_database_fresh_v5")
                    .fallbackToDestructiveMigration()
                    .addCallback(TalentDatabaseCallback(CoroutineScope(Dispatchers.IO)))
                    .build()
                    .also { Instance = it }
            }
        }
    }
}

