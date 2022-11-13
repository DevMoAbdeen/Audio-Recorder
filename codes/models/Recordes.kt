package com.msa.audiorecorder.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Recordes{
    companion object{
        var pathRecord = ""
        var timerRecord = ""

        var saveOrRename = "saveRecord"
    }

    @PrimaryKey(autoGenerate = true) var Id: Int = 0
    @ColumnInfo(name = "record_path") var path: String? = null
    @ColumnInfo(name = "record_name") var name: String? = null
    @ColumnInfo(name = "record_date") var date: String? = null
    @ColumnInfo(name = "record_timer") var timer: String? = null
}