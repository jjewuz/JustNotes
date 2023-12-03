package com.jjewuz.justnotes

import androidx.lifecycle.LiveData

class NoteRepository(private val notesDao: NotesDao) {

    val allNotes: LiveData<List<Note>> = notesDao.getAllSortedByTime()

    fun getLabeled(label: String): LiveData<List<Note>> {
        return notesDao.getItemsWithLabel(label)
    }

    fun getNotes(): LiveData<List<Note>> {
        return notesDao.getAllSortedByTime()
    }

    suspend fun insert(note: Note) {
        notesDao.insert(note)
    }

    suspend fun delete(note: Note){
        notesDao.delete(note)
    }

    suspend fun update(note: Note){
        notesDao.update(note)
    }
}