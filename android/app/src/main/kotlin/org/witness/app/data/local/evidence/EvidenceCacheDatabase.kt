package org.witness.app.data.local.evidence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        EvidenceEntity::class,
        EvidenceChunkEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class EvidenceCacheDatabase : RoomDatabase() {
    abstract fun evidenceDao(): EvidenceDao

    abstract fun evidenceChunkDao(): EvidenceChunkDao

    companion object {
        private const val DATABASE_NAME = "witness_evidence_cache.db"

        @Volatile
        private var instance: EvidenceCacheDatabase? = null

        // Room only notifies observers about writes made through the same instance,
        // so the upload queue UI and the workers must share one.
        fun create(context: Context): EvidenceCacheDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    EvidenceCacheDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
        }
    }
}
