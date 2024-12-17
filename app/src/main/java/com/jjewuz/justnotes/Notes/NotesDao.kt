package com.jjewuz.justnotes.Notes

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note : Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("Select * from notesTable order by id ASC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("Select * from notesTable where id = :noteId")
    fun getNoteById(noteId: Int): Note?

    @Query("Select * from notesTable Order By timeStamp DESC " )
    fun getAllSortedByTime(): LiveData<List<Note>>

    @Query("SELECT * FROM notesTable WHERE categoryId = :categoryId")
    fun getNotesByCategory(categoryId: Int): LiveData<List<Note>>

    @Query("Select * from notesTable where Title like '%' || :search || '%'")
    fun showSearch(search: String?): LiveData<List<Note>>

    @Update
    suspend fun update(note: Note)
}
