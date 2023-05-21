package com.jjewuz.justnotes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class Todo(
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "is_completed") var isCompleted: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0
}
