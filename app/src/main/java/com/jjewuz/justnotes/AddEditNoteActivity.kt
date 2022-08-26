package com.jjewuz.justnotes


import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.text.Editable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddEditNoteActivity : AppCompatActivity() {

    lateinit var noteTitleEdt: EditText
    lateinit var noteEdt: EditText
    lateinit var saveBtn: Button

    lateinit var viewModal: NoteViewModal
    var noteID = -1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(findViewById(R.id.topAppBar))

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModal::class.java)

        noteTitleEdt = findViewById(R.id.idEdtNoteName)
        noteEdt = findViewById(R.id.idEdtNoteDesc)

        val noteType = intent.getStringExtra("noteType")
        if (noteType.equals("Edit")) {
            // on below line we are setting data to edit text.
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteDescription = intent.getStringExtra("noteDescription")
            noteID = intent.getIntExtra("noteId", -1)
            noteTitleEdt.setText(noteTitle)
            noteEdt.setText(noteDescription)
        } else {
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.mymenu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.first -> {
                val thisnote = findViewById<EditText>(R.id.idEdtNoteDesc)
                val text = ""
                thisnote.text = Editable.Factory.getInstance().newEditable(text)
                true
            }
            R.id.second -> {
                val thisnote = findViewById<EditText>(R.id.idEdtNoteDesc)
                val string: String = getString(R.string.counters)
                val count = thisnote.text.length
                val time = count / 512



                MaterialAlertDialogBuilder(this)
                    .setTitle("$string")
                    .setMessage("$count | ≈$time")
                    .setPositiveButton("OK") { dialog, which ->
                    }
                    .show()

                true
            }
            R.id.export -> {
                var targetDoc = createWordDoc()
                addParagraph(targetDoc)
                saveOurDoc(targetDoc)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val noteType = intent.getStringExtra("noteType")

        val noteTitle = noteTitleEdt.text.toString()
        val noteDescription = noteEdt.text.toString()
        if (noteType.equals("Edit")) {
            if (noteTitle.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                val currentDateAndTime: String = sdf.format(Date())
                val updatedNote = Note(noteTitle, noteDescription, currentDateAndTime)
                updatedNote.id = noteID
                viewModal.updateNote(updatedNote)
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }
        } else {
            if (noteTitle.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                val currentDateAndTime: String = sdf.format(Date())
                viewModal.addNote(Note(noteTitle, noteDescription, currentDateAndTime))
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }
        }
        this.finish()
    }

    private fun createWordDoc(): XWPFDocument {
        return XWPFDocument()
    }

    private fun addParagraph(targetDoc:XWPFDocument){
        val paragraph1 = targetDoc.createParagraph()
        paragraph1.alignment = ParagraphAlignment.LEFT
        val sentenceRun1 = paragraph1.createRun()
        noteEdt = findViewById(R.id.idEdtNoteDesc)
        sentenceRun1.fontSize = 16
        sentenceRun1.fontFamily = "Times New Roman"
        sentenceRun1.setText(noteEdt.text.toString())
        sentenceRun1.addBreak()
        Toast.makeText(this, R.string.docMade, Toast.LENGTH_SHORT).show()

    }

    private fun saveOurDoc(targetDoc:XWPFDocument){
            val path = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val ourAppFileDirectory = (path)
            if (ourAppFileDirectory != null && !ourAppFileDirectory.exists()) {
                ourAppFileDirectory.mkdirs()
            }
            val noteTitle = noteTitleEdt.text.toString()
            val wordFile = File(ourAppFileDirectory, "$noteTitle.docx")
            try {
                val fileOut = FileOutputStream(wordFile)
                targetDoc.write(fileOut)
                fileOut.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        val noteType = intent.getStringExtra("noteType")

        val noteTitle = noteTitleEdt.text.toString()
        val noteDescription = noteEdt.text.toString()
        if (noteType.equals("Edit")) {
            if (noteTitle.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                val currentDateAndTime: String = sdf.format(Date())
                val updatedNote = Note(noteTitle, noteDescription, currentDateAndTime)
                updatedNote.id = noteID
                viewModal.updateNote(updatedNote)
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }
        } else {
            if (noteTitle.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                val currentDateAndTime: String = sdf.format(Date())
                viewModal.addNote(Note(noteTitle, noteDescription, currentDateAndTime))
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
            }
        }
        this.finish()
        return false
    }

}


