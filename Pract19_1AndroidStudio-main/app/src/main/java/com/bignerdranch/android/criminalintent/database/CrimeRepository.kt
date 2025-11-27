package com.bignerdranch.android.criminalintent.database

import android.content.Context
import androidx.room.Room
import com.bignerdranch.android.criminalintent.Crime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CrimeRepository private constructor(context: Context) {
    private val database: CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        "crime-database"
    ).addMigrations(CrimeDatabase.migration_1_2)
        .build()

    private val crimeDao = database.crimeDao()

    suspend fun getCrimes(): List<Crime> = withContext(Dispatchers.IO) {
        crimeDao.getCrimes()
    }


    suspend fun updateCrime(crime: Crime) = withContext(Dispatchers.IO) {
        crimeDao.updateCrime(crime)
    }


    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}