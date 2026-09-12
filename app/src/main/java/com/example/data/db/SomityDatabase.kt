package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.MemberEntity
import com.example.data.model.WeekRecordEntity

@Database(
    entities = [MemberEntity::class, WeekRecordEntity::class],
    version = 4,
    exportSchema = false
)
abstract class SomityDatabase : RoomDatabase() {

    abstract fun somityDao(): SomityDao

    companion object {
        @Volatile
        private var INSTANCE: SomityDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE members ADD COLUMN startDate INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE members SET startDate = createdAt WHERE startDate = 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE members ADD COLUMN phone TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE members ADD COLUMN photoUri TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE members ADD COLUMN guarantor TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): SomityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SomityDatabase::class.java,
                    "somity_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
