package com.jjewuz.justnotes

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface NotesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note :Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("Select * from notesTable order by id ASC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("Select * from notesTable where id = :noteId")
    fun getNoteById(noteId: Int): Note?

    @Query("Select * FROM notesTable Order By timeStamp DESC " )
    fun getAllSortedByTime(): LiveData<List<Note>>

    @Update
    suspend fun update(note: Note)
}
