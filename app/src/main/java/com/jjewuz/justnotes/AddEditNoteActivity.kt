package com.jjewuz.justnotes


import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.transition.Explode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddEditNoteActivity : AppCompatActivity() {

    lateinit var noteTitleEdt: EditText
    lateinit var noteEdt: EditText

    lateinit var viewModal: NoteViewModal
    var noteID = -1;

    private var hasChanges = false;

    private lateinit var sharedPref: SharedPreferences

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            saveNote()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            exitTransition = Explode()
        }


        val enabledFont = sharedPref.getBoolean("enabledFont", false)
        val enabledMonet = sharedPref.getBoolean("enabledMonet", true)
        if (enabledFont and enabledMonet){
            setTheme(R.style.AppTheme)
        } else if (!enabledFont and enabledMonet){
            setTheme(R.style.FontMonet)
        }
        else if (!enabledFont and !enabledMonet){
            setTheme(R.style.Font)
        } else {
            setTheme(R.style.Nothing)
        }
        setContentView(R.layout.activity_add_edit_note)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(findViewById(R.id.topAppBar))


        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModal::class.java)

        noteTitleEdt = findViewById(R.id.idEdtNoteName)
        noteEdt = findViewById(R.id.idEdtNoteDesc)


        val noteType = intent.getStringExtra("noteType")
        if (noteType.equals("Edit")) {
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteDescription = intent.getStringExtra("noteDescription")
            noteID = intent.getIntExtra("noteId", -1)
            noteTitleEdt.setText(noteTitle)
            supportActionBar?.title = ""
            noteEdt.setText(noteDescription)
        }

        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                hasChanges = true
            }

            override fun afterTextChanged(editable: Editable) {}
        }

        noteTitleEdt.addTextChangedListener(watcher)
        noteEdt.addTextChangedListener(watcher)
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
                val countS: String = getString(R.string.countSymb)
                val countT: String = getString(R.string.countTime)
                val count = thisnote.text.length
                val time = count / 512

                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.inf)
                    .setMessage("$countS $count \n$countT ≈$time")
                    .setPositiveButton("OK") { dialog, which ->
                    }
                    .show()

                true
            }
            R.id.export -> {
                saveOurDoc()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveNote() {
        if (hasChanges) {
            val noteType = intent.getStringExtra("noteType")

            val noteTitle = noteTitleEdt.text.toString()
            val noteDescription = noteEdt.text.toString()
            if (noteDescription.isNotEmpty() or noteTitle.isNotEmpty()) {
                if (noteType.equals("Edit")) {

                    val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                    val currentDateAndTime: String = sdf.format(Date())

                    if (noteTitle.isEmpty()) {
                        val updatedNote = Note(noteDescription, noteDescription, currentDateAndTime)
                    } else {
                        val updatedNote = Note(noteTitle, noteDescription, currentDateAndTime)
                        updatedNote.id = noteID
                        viewModal.updateNote(updatedNote)
                    }
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                } else {
                    val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                    val currentDateAndTime: String = sdf.format(Date())
                    if (noteTitle.isEmpty()) {
                        viewModal.addNote(
                            Note(
                                noteDescription,
                                noteDescription,
                                currentDateAndTime
                            )
                        )
                    } else {
                        viewModal.addNote(Note(noteTitle, noteDescription, currentDateAndTime))

                    }
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                }
            }
        }
        this.finishAfterTransition()
    }


    private fun saveOurDoc() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            noteEdt = findViewById(R.id.idEdtNoteDesc)

            val noteText = noteEdt.text.toString()

            val noteTitle = noteTitleEdt.text.toString()

            createAndSaveWordDocScoped(this, noteTitle, noteText)
        } else {

            val noteTitle = noteTitleEdt.text.toString()
            noteEdt = findViewById(R.id.idEdtNoteDesc)

            val noteText = noteEdt.text.toString()

            createAndSaveWordDoc(this, noteTitle, noteText)

        }


    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun createAndSaveWordDocScoped(context: Context, noteName: String, noteText: String) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, noteName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        uri.let {
            if (it != null) {
                contentResolver.openOutputStream(it).use { outputStream ->
                    outputStream?.write(noteText.toByteArray())
                }
            }
            Toast.makeText(context, R.string.docMade, Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    fun createAndSaveWordDoc(context: Context, noteName: String, noteText: String) {

        val downloadsDirectory = File(Environment.getExternalStorageDirectory(), "Download")
        if (!downloadsDirectory.exists()) {
            downloadsDirectory.mkdirs()
        }
        val file = File(downloadsDirectory, noteName)

        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(noteText.toByteArray())
            fileOutputStream.close()
            Toast.makeText(this, R.string.docMade, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "${R.string.error}: $e", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        saveNote()
        return false
    }

}