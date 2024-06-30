package com.jjewuz.justnotes.Notes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jjewuz.justnotes.Category.Category

@Entity(
    tableName = "notesTable",
)

class Note (
    @ColumnInfo(name = "Title") val noteTitle: String,
    @ColumnInfo(name = "description") val noteDescription: String,
    @ColumnInfo(name = "timestamp") val timeStamp: String,
    @ColumnInfo(name = "security", defaultValue = "") val security: String,
    @ColumnInfo(name = "label", defaultValue = "") val label: String,
    @ColumnInfo(name = "categoryId") val categoryId: Int? = null
){
    @PrimaryKey(autoGenerate = true)
    var id = 0
}