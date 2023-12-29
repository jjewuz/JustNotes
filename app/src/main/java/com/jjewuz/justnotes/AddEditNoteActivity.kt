package com.jjewuz.justnotes


import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.transition.Fade
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.text.toHtml
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.review.ReviewManagerFactory
import com.jjewuz.justnotes.Utils.colorFormatting
import com.jjewuz.justnotes.Utils.hideKeyboard
import com.jjewuz.justnotes.Utils.textFormatting
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

    private lateinit var labelGroup: ChipGroup
    private lateinit var label1: Chip
    private lateinit var label2: Chip
    private lateinit var label3: Chip
    private var label = ""
    private var oldLabel = ""

    private lateinit var importTxtBtn: Button
    private lateinit var exportTxtBtn: Button
    private lateinit var clearBtn: Button
    private lateinit var toWidgetBtn: Button
    private lateinit var passBtn: Button

    private lateinit var viewModal: NoteViewModal
    private var noteID = -1;
    private var noteLock = "";

    private var added = false

    private var hasChanges = false

    private lateinit var textEditor: TextHelper

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
        val theme = sharedPref.getString("theme", "standart")
        val securedNote = sharedPref.getBoolean("screenSecurity", false)
        if (enabledFont and (theme=="monet")) {
            setTheme(R.style.AppTheme)
        } else if (!enabledFont and (theme=="monet")) {
            setTheme(R.style.FontMonet)
        } else if (!enabledFont and (theme=="standart")) {
            setTheme(R.style.Font)
        } else if (enabledFont and (theme=="standart")) {
            setTheme(R.style.Nothing)
        } else if (!enabledFont and (theme=="ice")){
            setTheme(R.style.BlackIceFont)
        } else if (enabledFont and (theme=="ice")){
            setTheme(R.style.BlackIce)
        }
        setContentView(R.layout.activity_add_edit_note)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(findViewById(R.id.topAppBar))
        supportActionBar?.title = ""


        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[NoteViewModal::class.java]

        noteTitleEdt = findViewById(R.id.idEdtNoteName)
        noteEdt = findViewById(R.id.idEdtNoteDesc)
        savedTxt = findViewById(R.id.savedtxt)
        countTxt = findViewById(R.id.counttxt)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        bottomSheet = findViewById(R.id.standard_bottom_sheet)

        labelGroup = findViewById(R.id.chipGroup)
        label1 = findViewById(R.id.label1)
        label2 = findViewById(R.id.label2)
        label3 = findViewById(R.id.label3)

        label1.text = sharedPref.getString("label1", "")
        label2.text = sharedPref.getString("label2", "")
        label3.text = sharedPref.getString("label3", "")

        if (label1.text == ""){
            label1.visibility = View.GONE
        }
        if (label2.text == ""){
            label2.visibility = View.GONE
        }
        if (label3.text == ""){
            label3.visibility = View.GONE
        }

        importTxtBtn = findViewById(R.id.importtxt)
        exportTxtBtn = findViewById(R.id.exporttxt)
        clearBtn = findViewById(R.id.clear_txt)
        toWidgetBtn = findViewById(R.id.towidget)
        passBtn = findViewById(R.id.pass_btn)

        //Receive text from share sheet
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                noteEdt.setText(text)
                hasChanges = true
            }
        }

        val standardBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        labelGroup.setOnCheckedStateChangeListener { group, checkedID ->
            when(group.checkedChipId) {
                R.id.important -> {
                    label = "important"
                }
                R.id.useful -> {
                    label = "useful"
                }
                R.id.hobby -> {
                    label = "hobby"
                }
                R.id.label1 -> {
                    label = "label1"
                }
                R.id.label2 -> {
                    label = "label2"
                }
                R.id.label3 -> {
                    label = "label3"
                }
                else -> label = ""

            }
            hasChanges = true
        }


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

        bottomAppBar.setNavigationOnClickListener {
            val countS: String = getString(R.string.count1)
            val countT: String = getString(R.string.count2)
            val countM: String = getString(R.string.minuts)
            val count = noteEdt.text.length
            val time = count / 512

            countTxt.text = "$countS: $count \n$countT ≈$time $countM"
            countTxt.hideKeyboard()
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.boldbtn -> {
                    textFormatting("bold", noteEdt)
                    true
                }
                R.id.italicbtn -> {
                    textFormatting("italic", noteEdt)
                    true
                }
                R.id.underlinedbtn -> {
                    textFormatting("under", noteEdt)
                    true
                }
                R.id.strikebtn -> {
                    textFormatting("strike", noteEdt)
                    true
                }
                R.id.clearformat -> {
                    textFormatting("null", noteEdt)
                    true
                }
                R.id.color -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    val inflater = this.layoutInflater.inflate(R.layout.color_picker, null)
                    val red = inflater.findViewById<Button>(R.id.red)
                    val yellow = inflater.findViewById<Button>(R.id.yellow)
                    val blue = inflater.findViewById<Button>(R.id.blue)
                    val green = inflater.findViewById<Button>(R.id.green)

                    builder.setIcon(R.drawable.palette)
                    builder.setTitle(R.string.color_picker)
                    builder.setView(inflater)
                        .setPositiveButton(R.string.close) { _, _ ->
                        }
                    builder.create()
                    val dialog = builder.show()
                    red.setOnClickListener { colorFormatting("red", noteEdt)
                        dialog.dismiss()}
                    yellow.setOnClickListener { colorFormatting("yellow", noteEdt)
                        dialog.dismiss()}
                    blue.setOnClickListener { colorFormatting("blue", noteEdt)
                        dialog.dismiss()}
                    green.setOnClickListener { colorFormatting("green", noteEdt)
                        dialog.dismiss()}
                    hasChanges = true
                    true
                }
                else -> false
            }
        }


        val noteType = intent.getStringExtra("noteType")
        if (noteType.equals("Edit")) {
            val noteTitle = intent.getStringExtra("noteTitle")
            val noteDescription = intent.getStringExtra("noteDescription")
            val date = intent.getStringExtra("timestamp")
            val label = intent.getStringExtra("label")
            val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm", Locale.getDefault())
            var currentDateAndTime = ""
            try {
                currentDateAndTime = sdf.format(date?.toLong()).toString()
            } catch (e: Exception){
                if (date != null) {
                    currentDateAndTime = date
                }
            }
            if (label == "important"){
                labelGroup.check(R.id.important)
            } else if (label == "useful"){
                labelGroup.check(R.id.useful)
            }else if (label == "hobby"){
                labelGroup.check(R.id.hobby)
            }else if (label == "label1"){
                labelGroup.check(R.id.label1)
            }else if (label == "label2"){
                labelGroup.check(R.id.label2)
            } else if (label == "label3"){
                labelGroup.check(R.id.label3)
            }
            hasChanges = false
            noteID = intent.getIntExtra("noteId", -1)
            noteLock = intent.getStringExtra("security").toString()
            noteTitleEdt.setText(noteTitle)
            supportActionBar?.title = ""
            savedTxt.text = resources.getString(R.string.saved) + ": \n" + currentDateAndTime
            noteEdt.setText(noteDescription?.let { Utils.fromHtml(it) })
        }

        passBtn.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            val inflater = this.layoutInflater.inflate(R.layout.dialog_password, null)
            val pass = inflater.findViewById<TextInputEditText>(R.id.input)
            pass.setText(noteLock)
            builder.setIcon(R.drawable.security)
            builder.setTitle(R.string.set_password)
            builder.setView(inflater)
                .setPositiveButton("OK") { dialog, id ->
                    if (pass.text.toString().length <= 5) {
                        noteLock = pass.text.toString()
                        hasChanges = true
                    }
                }
                .setNegativeButton(R.string.back) { dialog, id ->
                    dialog.cancel()
                }
            builder.create().show()
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
        textEditor = TextHelper(noteEdt)
        noteTitleEdt.addTextChangedListener(watcher)
        noteEdt.addTextChangedListener(watcher)


        if (noteLock != "" && noteLock != "0"){
            if (!securedNote){
                this.window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)
            }
            noteEdt.visibility = View.GONE
            val builder = MaterialAlertDialogBuilder(this)
            val inflater = this.layoutInflater.inflate(R.layout.dialog_password, null)
            val pass = inflater.findViewById<TextInputEditText>(R.id.input)
            val hint = inflater.findViewById<TextView>(R.id.pass_hint)
            hint.visibility = View.GONE
            builder.setIcon(R.drawable.security)
            builder.setTitle(R.string.enter_password)
            builder.setView(inflater)
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, id ->
                        if (noteLock != pass.text.toString()){
                            this.finish()
                        } else {
                            noteEdt.visibility = View.VISIBLE
                        }

                    }
                .setNegativeButton(R.string.back ) { dialog, id ->
                       this.finish()
                    }
            builder.create().show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.text_editor, menu)

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.undo -> {
                textEditor.undo()
                true
            }
            R.id.redo -> {
                textEditor.redo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun pushWidget(){
        if (sharedPref.getInt("reviewed1", 0) == 0) {
            requestReviewFlow(this)
        }
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

                val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
                val date = Date().time
                val currentDateAndTime: String = sdf.format(date)

                if (noteType.equals("Edit")) {
                    if (noteTitle.isEmpty()) {
                        noteTitle = emptyTitle
                    }
                    val updatedNote = Note(noteTitle, noteDescription.toHtml(), date.toString(), noteLock, label)
                    updatedNote.id = noteID
                    viewModal.updateNote(updatedNote)
                } else {
                    if (!added){
                        if (noteTitle.isEmpty()) {
                            noteTitle = emptyTitle
                        }
                        viewModal.addNote(Note(noteTitle,  noteDescription.toHtml(), date.toString(), noteLock, label))
                        added = true
                    }

                }
                savedTxt.text = resources.getString(R.string.saved) + ": \n" + currentDateAndTime
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
            noteEdt = findViewById(R.id.idEdtNoteDesc)
            val noteTitle = noteTitleEdt.text.toString()
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

    private fun requestReviewFlow(activity: Activity) {

        val reviewManager = ReviewManagerFactory.create(activity)

        val requestReviewFlow = reviewManager.requestReviewFlow()

        requestReviewFlow.addOnCompleteListener { request ->

            if (request.isSuccessful) {

                val reviewInfo = request.result

                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)

                flow.addOnCompleteListener {
                    sharedPref.edit().putInt("reviewed1", 1).apply()
                }

            }
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