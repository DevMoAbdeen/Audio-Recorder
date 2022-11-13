package com.msa.audiorecorder.room_db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.msa.audiorecorder.models.Recordes

@Dao
interface RecordeDao {

    @Insert
    fun insertRecorde(recorde: Recordes)

//    @Update
//    fun editRecorderName(recorde: Recordes)

    @Query("Select * From Recordes Order By record_date DESC")
    fun getAllRecordes(): MutableList<Recordes>

    @Query("Select record_path From Recordes Where Id = :id")
    fun getRecordPath(id: Int): String

    @Query("Update Recordes Set record_name = :name Where Id = :id")
    fun editRecordeName(id: Int, name: String)

    @Query("Delete From Recordes Where Id = :id")
    fun deleteRecord(id: Int)

    @Query("Delete From Recordes")
    fun deleteAllRecordes()

}