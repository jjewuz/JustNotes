package com.jjewuz.justnotes.Notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModal (application: Application) :AndroidViewModel(application) {

    private val allNotes : LiveData<List<Note>>
    private val repository : NoteRepository

    init {
        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        repository = NoteRepository(dao)
        allNotes = repository.allNotes
    }

    fun getLabel(categoryId: Int): LiveData<List<Note>> {
        return repository.getLabeled(categoryId)
    }

    fun getQuery(query: String): LiveData<List<Note>>{
        return repository.getQuery(query)
    }

    fun getNotes(): LiveData<List<Note>>{
        return repository.getNotes()
    }

    fun deleteNote (note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(note)
    }

    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(note)
    }

    fun addNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(note)
    }


}