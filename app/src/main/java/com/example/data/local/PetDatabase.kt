package com.petpulse.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.petpulse.app.data.model.LostPetAlert
import com.petpulse.app.data.model.MedicalReport
import com.petpulse.app.data.model.PetListing
import com.petpulse.app.data.model.UserPet
import com.petpulse.app.data.model.VaccinationRecord

@Database(
    entities = [
        UserPet::class,
        VaccinationRecord::class,
        MedicalReport::class,
        LostPetAlert::class,
        PetListing::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao

    companion object {
        @Volatile
        private var INSTANCE: PetDatabase? = null

        fun getInstance(context: Context): PetDatabase {
            return INSTANCE ?: synchronized(this) {
                // No onCreate seed callback: the app no longer invents a demo pet,
                // demo vaccinations, demo medical reports, demo lost-pet alerts or
                // demo listings on first run. Every pet is now created by the
                // signed-in owner and stored in Firestore, so the local database
                // starts genuinely empty.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PetDatabase::class.java,
                    "jane_pals_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
