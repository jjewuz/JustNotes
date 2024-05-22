package com.jjewuz.justnotes.Notes

import androidx.lifecycle.LiveData
import androidx.room.*
import com.jjewuz.justnotes.Notes.Note


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

    @Query("Select * from notesTable where label = :customLabel Order By timeStamp DESC")
    fun getItemsWithLabel(customLabel: String): LiveData<List<Note>>

    @Query("Select * from notesTable where Title like '%' || :search || '%'")
    fun showSearch(search: String?): LiveData<List<Note>>

    @Update
    suspend fun update(note: Note)
}
