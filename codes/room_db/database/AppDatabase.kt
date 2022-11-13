package com.msa.audiorecorder.room_db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.msa.audiorecorder.models.Recordes
import com.msa.audiorecorder.room_db.dao.RecordeDao

@Database(entities = [Recordes::class], version = 1)
abstract class AppDatabase :RoomDatabase() {
    abstract fun recordDao() : RecordeDao
}