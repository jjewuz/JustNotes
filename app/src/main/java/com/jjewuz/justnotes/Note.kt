package com.jjewuz.justnotes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "notesTable")
class Note (
    @ColumnInfo(name = "Title") val noteTitle: String,
    @ColumnInfo(name = "description") val noteDescription: String,
    @ColumnInfo(name = "timestamp") val timeStamp: String,
    @ColumnInfo(name = "security", defaultValue = "") val security: String,
    @ColumnInfo(name = "label", defaultValue = "") val label: String
){
    @PrimaryKey(autoGenerate = true)
    var id = 0
}