package com.jjewuz.justnotes.Activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jjewuz.justnotes.Fragments.NotesFragment
import com.jjewuz.justnotes.Notes.Note
import com.jjewuz.justnotes.Notes.NoteClickInterface
import com.jjewuz.justnotes.Notes.NoteDatabase
import com.jjewuz.justnotes.Notes.NoteLongClickInterface
import com.jjewuz.justnotes.Notes.NoteRVAdapter
import com.jjewuz.justnotes.Notes.NoteViewModal
import com.jjewuz.justnotes.R
import kotlinx.coroutines.launch

class NotePickerActivity : AppCompatActivity(), NoteClickInterface, NoteLongClickInterface {

    lateinit var viewModal: NoteViewModal
    lateinit var noteRVAdapter: NoteRVAdapter

    private lateinit var database: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_picker)

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        )[NoteViewModal::class.java]

        val recyclerView = findViewById<RecyclerView>(R.id.notes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val categoryDao = NoteDatabase.getDatabase(this).getCategoryDao()
        noteRVAdapter = NoteRVAdapter(this, this, this, categoryDao )

        /*lifecycleScope.launch {
            val notes = viewModal.allNotes
            recyclerView.adapter = noteRVAdapter { note ->
                val resultIntent = Intent()
                resultIntent.putExtra("note_id", note.id)
                resultIntent.putExtra("note_content", note.content)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }*/
    }

    override fun onNoteClick(note: Note, num: Int) {
    }


    override fun onNoteLongClick(note: Note) {

    }
}