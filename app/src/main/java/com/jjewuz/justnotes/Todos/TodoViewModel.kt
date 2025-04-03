package com.jjewuz.justnotes.Todos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jjewuz.justnotes.Notes.NoteDatabase
import com.jjewuz.justnotes.Notes.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val todoDao = TodoDatabase.getDatabase(application).todoDao()
    val allTodos: LiveData<List<Todo>>

    init {
        val dao = TodoDatabase.getDatabase(application).todoDao()
        allTodos = dao.getAllTodos()
    }

    fun insert(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.insert(todo)
        }
    }

    fun update(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.update(todo)
        }
    }

    fun delete(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.delete(todo)
        }
    }

}
