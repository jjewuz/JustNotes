package com.jjewuz.justnotes.Category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jjewuz.justnotes.Notes.NoteDatabase
import com.jjewuz.justnotes.Category.Category
import com.jjewuz.justnotes.Category.CategoryRepo
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CategoryRepo
    val allCategories: LiveData<List<Category>>

    init {
        val categoryDao = NoteDatabase.getDatabase(application).getCategoryDao()
        repository = CategoryRepo(categoryDao)
        allCategories = repository.getAllCategories()
    }

    fun insert(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }

    fun delete(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }
}
