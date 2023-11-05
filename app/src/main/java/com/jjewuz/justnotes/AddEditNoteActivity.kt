package com.jjewuz.justnotes


import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.transition.Fade
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.toHtml
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*


class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var noteTitleEdt: EditText
    private lateinit var noteEdt: EditText
    private lateinit var savedTxt: TextView
    private lateinit var countTxt: TextView
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var bottomSheet: FrameLayout

    private lateinit var importTxtBtn: Button
    private lateinit var exportTxtBtn: Button
    private lateinit var clearBtn: Button
    private lateinit var toWidgetBtn: Button

    private lateinit var viewModal: NoteViewModal
    private var noteID = -1;

    private var added = false

    private var hasChanges = false

    private lateinit var sharedPref: SharedPreferences

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            saveNote(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            enterTransition = Fade()
            exitTransition = Fade()
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
        supportActionBar?.title = ""


        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModal::class.java)

        noteTitleEdt = findViewById(R.id.idEdtNoteName)
        noteEdt = findViewById(R.id.idEdtNoteDesc)
        savedTxt = findViewById(R.id.savedtxt)
        countTxt = findViewById(R.id.counttxt)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        bottomSheet = findViewById(R.id.standard_bottom_sheet)

        importTxtBtn = findViewById(R.id.importtxt)
        exportTxtBtn = findViewById(R.id.exporttxt)
        clearBtn = findViewById(R.id.clear_txt)
        toWidgetBtn = findViewById(R.id.towidget)

        val standardBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        importTxtBtn.setOnClickListener {
            getTxtFile.launch("text/plain")
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        exportTxtBtn.setOnClickListener {
            saveOurDoc()
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        clearBtn.setOnClickListener {
            noteEdt.setText("")
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        toWidgetBtn.setOnClickListener {
            val sharedPreferences = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("note_id", noteID)
            editor.apply()
            pushWidget()
            Toast.makeText(this, R.string.note_set_to_widget, Toast.LENGTH_SHORT).show()
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        val vib = if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.S) {
            val vMan = (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
            vMan.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }



        bottomAppBar.setNavigationOnClickListener {
            vib.vibrate(VibrationEffect.createOneShot(100, 30))
            val countS: String = getString(R.string.count1)
            val countT: String = getString(R.string.count2)
            val countM: String = getString(R.string.minuts)
            val count = noteEdt.text.length
            val time = count / 512

            countTxt.text = "$countS: $count \n$countT ≈$time $countM"
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.boldbtn -> {
                    textFormatting("bold")
                    true
                }
                R.id.italicbtn -> {
                    textFormatting("italic")
                    true
                }
                R.id.underlinedbtn -> {
                    textFormatting("under")
                    true
                }
                R.id.strikebtn -> {
                    textFormatting("strike")
                    true
                }
                R.id.clearformat -> {
                    textFormatting("null")
                    true
                }
                else -> false
            }
        }


        val noteType = intent.getStringExtra("noteType")
        if (noteType.equals("Edit")) {
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteDescription = intent.getStringExtra("noteDescription")
            val currentDateAndTime = intent.getStringExtra("timestamp")
            noteID = intent.getIntExtra("noteId", -1)
            noteTitleEdt.setText(noteTitle)
            supportActionBar?.title = ""
            savedTxt.text = resources.getString(R.string.saved) + ":" + currentDateAndTime?.takeLast(6)
            noteEdt.setText(noteDescription?.let { Utils.fromHtml(it) })
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


    private fun pushWidget(){
        val intent = Intent(this, NoteWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(this).getAppWidgetIds(ComponentName(application, NoteWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    private fun saveNote(exit: Boolean) {
        if (hasChanges) {
            val noteType = intent.getStringExtra("noteType")
            val emptyTitle = getString(R.string.note)
            var noteTitle = noteTitleEdt.text.toString()
            val noteDescription = noteEdt.text

            if (noteDescription.isNotEmpty() or noteTitle.isNotEmpty()) {

                val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm", Locale.getDefault())
                val currentDateAndTime: String = sdf.format(Date().time)

                if (noteType.equals("Edit")) {
                    if (noteTitle.isEmpty()) {
                        noteTitle = emptyTitle
                    }
                    val updatedNote = Note(noteTitle, noteDescription.toHtml(), currentDateAndTime)
                    updatedNote.id = noteID
                    viewModal.updateNote(updatedNote)
                } else {
                    if (!added){
                        if (noteTitle.isEmpty()) {
                            noteTitle = emptyTitle
                        }
                        viewModal.addNote(Note(noteTitle,  noteDescription.toHtml(), currentDateAndTime))
                        added = true
                    }

                }
                savedTxt.text = resources.getString(R.string.saved) + ":" + currentDateAndTime.takeLast(6)
            }
            pushWidget()
        }
        if (exit) {
            this.finishAfterTransition()
        }
    }

    private val getTxtFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        val text = uri?.let { readTxt(this, it) }
        noteEdt.setText(text)
    }

    private fun readTxt(context: Context, fileUri: Uri): String {
        val content = StringBuilder()
        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val reader = BufferedReader (InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null){
                content.append(line).append("\n")
            }
            inputStream?.close()
        } catch (_: Exception) {

        }
        return content.toString()
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
        }
    }

    private fun createAndSaveWordDoc(context: Context, noteName: String, noteText: String) {

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

    private fun textFormatting(param: String){
        val selectedTextStart = noteEdt.selectionStart
        val selectedTextEnd = noteEdt.selectionEnd

        if (selectedTextStart != -1 && selectedTextEnd != -1) {
            val spannable = SpannableString(noteEdt.text)
            if (param == "bold"){
                spannable.setSpan(StyleSpan(Typeface.BOLD), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if (param == "italic"){
                spannable.setSpan(StyleSpan(Typeface.ITALIC), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if (param == "under"){
                spannable.setSpan(UnderlineSpan(), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if (param == "strike"){
                spannable.setSpan(StrikethroughSpan(), selectedTextStart, selectedTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }else if (param == "null"){
                val spans = spannable.getSpans(
                    selectedTextStart, selectedTextEnd,
                    CharacterStyle::class.java
                )
                for (selectSpan in spans) spannable.removeSpan(selectSpan)
            }

            noteEdt.setText(spannable)
        }
    }


    //Auto-saving note
    override fun onPause() {
        saveNote(false)
        super.onPause()
    }

    override fun onStop(){
        saveNote(false)
        super.onStop()
    }

    override fun onDestroy() {
        saveNote(false)
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        saveNote(true)
        return false
    }

}