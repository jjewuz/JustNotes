package com.jjewuz.justnotes.Activities


import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColor
import androidx.core.text.toHtml
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.review.ReviewManagerFactory
import com.jjewuz.justnotes.Category.Category
import com.jjewuz.justnotes.Category.CategoryDao
import com.jjewuz.justnotes.Category.CategoryViewModel
import com.jjewuz.justnotes.Notes.Note
import com.jjewuz.justnotes.Notes.NoteDatabase
import com.jjewuz.justnotes.Notes.NoteViewModal
import com.jjewuz.justnotes.Notes.NoteWidget
import com.jjewuz.justnotes.R
import com.jjewuz.justnotes.Utils.MobileBertInterpreter
import com.jjewuz.justnotes.Utils.TextHelper
import com.jjewuz.justnotes.Utils.Utils
import com.jjewuz.justnotes.Utils.Utils.colorFormatting
import com.jjewuz.justnotes.Utils.Utils.hideKeyboard
import com.jjewuz.justnotes.Utils.Utils.textFormatting
import com.jjewuz.justnotes.Utils.WordPieceTokenizer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.core.content.edit

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var noteTitleEdt: EditText
    private lateinit var noteEdt: EditText
    private lateinit var savedTxt: TextView
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var bottomSheet: FrameLayout
    private var bgId: Int = -1
    private lateinit var bg: CoordinatorLayout
    private lateinit var topBar: MaterialToolbar

    private var label = ""

    private lateinit var importTxtBtn: Button
    private lateinit var exportTxtBtn: Button
    private lateinit var clearBtn: LinearLayout
    private lateinit var toWidgetBtn: LinearLayout
    private lateinit var passBtn: LinearLayout
    private lateinit var aiButton: Button
    private lateinit var bgButton: Button

    private lateinit var viewModal: NoteViewModal
    private var noteID = -1
    private var noteLock = ""

    private var added = false

    private var hasChanges = false
    private var isEditable = true

    private lateinit var textEditor: TextHelper

    private lateinit var sharedPref: SharedPreferences

    private lateinit var fab: FloatingActionButton

    private lateinit var scrollView: NestedScrollView

    private lateinit var categorySpinner: Spinner

    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var categoryList: List<Category>
    private lateinit var categoryDao: CategoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = this.getSharedPreferences("prefs", Context.MODE_PRIVATE)

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
        topBar = findViewById(R.id.topAppBar)
        setSupportActionBar(topBar)
        supportActionBar?.title = ""

        enableEdgeToEdge()



        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[NoteViewModal::class.java]

        categoryDao = NoteDatabase.getDatabase(this).getCategoryDao()

        noteTitleEdt = findViewById(R.id.idEdtNoteName)
        noteEdt = findViewById(R.id.idEdtNoteDesc)
        savedTxt = findViewById(R.id.savedtxt)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        bottomSheet = findViewById(R.id.standard_bottom_sheet)
        scrollView = findViewById(R.id.nestedScrollView)
        bg = findViewById(R.id.holder)

        categorySpinner = findViewById(R.id.categorySpinner)

        importTxtBtn = findViewById(R.id.importtxt)
        exportTxtBtn = findViewById(R.id.exporttxt)
        clearBtn = findViewById(R.id.clear_txt)
        toWidgetBtn = findViewById(R.id.towidget)
        passBtn = findViewById(R.id.pass_btn)
        aiButton = findViewById(R.id.summarize)
        bgButton = findViewById(R.id.bgcolor) // change bg color

        //Receive text
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                noteEdt.setText(text)
                hasChanges = true
            }
        }

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
            sharedPreferences.edit {
                putInt("note_id", noteID)
            }
            pushWidget()
            Toast.makeText(this, R.string.note_set_to_widget, Toast.LENGTH_SHORT).show()
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        val isExp = sharedPref.getBoolean("is_exp", false)

        if (!isExp)
            aiButton.visibility = View.GONE

        aiButton.setOnClickListener {
            generateSum()
        }

        bgButton.setOnClickListener {
            showColorPickerDialog(this) { selectedColorId ->
                hasChanges = true
                setBgColorFromId(selectedColorId)
                bgId = selectedColorId
                saveNote(false)
            }
        }

        bottomAppBar.setNavigationOnClickListener {
            savedTxt.hideKeyboard()
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
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
            val category = intent.getIntExtra("categoryId", 0)

            categoryViewModel.allCategories.observe(this) { categories ->
                categoryList = categories
                setupCategorySpinner(categories, category)
            }

            val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm", Locale.getDefault())
            var currentDateAndTime = ""
            try {
                currentDateAndTime = sdf.format(date?.toLong()).toString()
            } catch (e: Exception){
                if (date != null) {
                    currentDateAndTime = date
                }
            }
            hasChanges = false
            noteID = intent.getIntExtra("noteId", -1)
            noteLock = intent.getStringExtra("security").toString()
            noteTitleEdt.setText(noteTitle)
            supportActionBar?.title = ""
            bgId = intent.getIntExtra("bgId", -1)
            setBgColorFromId(bgId)
            savedTxt.text = resources.getString(R.string.saved) + ": " + currentDateAndTime
            noteEdt.setText(noteDescription?.let { Utils.fromHtml(it) })
            isEditable = false
        } else {
            categoryViewModel.allCategories.observe(this) { categories ->
                categoryList = categories
                setupCategorySpinner(categories, 1)
            }
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position) as Category
                hasChanges = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //no action
            }
        }

        passBtn.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            val inflater = this.layoutInflater.inflate(R.layout.dialog_password, null)
            val pass = inflater.findViewById<TextInputEditText>(R.id.input)
            pass.setText(noteLock)
            builder.setIcon(R.drawable.security)
            builder.setTitle(R.string.set_password)
            builder.setView(inflater)
                .setPositiveButton("OK") { _, _ ->
                    if (pass.text.toString().length <= 5) {
                        noteLock = pass.text.toString()
                        hasChanges = true
                    }
                }
                .setNegativeButton(R.string.back) { dialog, _ ->
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
                .setPositiveButton("OK") { _, _ ->
                        if (noteLock != pass.text.toString()){
                            this.finish()
                        } else {
                            noteEdt.visibility = View.VISIBLE
                        }

                    }
                .setNegativeButton(R.string.back) { _, _ ->
                       this.finish()
                    }
            builder.create().show()
        }

        fab = findViewById(R.id.edit_fab)


        ViewCompat.setOnApplyWindowInsetsListener(fab) { vi, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = vi.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = insets.bottom + 20
            params.rightMargin = insets.right + 40
            vi.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }

        if (!isEditable){
            noteEdt.isFocusable = false
        } else {
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.save))
        }

        fab.setOnClickListener {
            if (!isEditable){
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.save))
                noteEdt.requestFocus()
                noteEdt.focus()
                isEditable = true
                noteEdt.setSelection(noteEdt.length())
            }
            else {
                fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.edit))
                saveNote(false)
                noteEdt.isFocusable = false
                isEditable = false
            }

        }
    }

    private fun setupCategorySpinner(categories: List<Category>, selectedCategoryId: Int) {
        val adapter = object : ArrayAdapter<Category>(this, R.layout.spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.text = categories[position].name
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.text = categories[position].name
                return view
            }
        }
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val selectedIndex = categories.indexOfFirst { it.id == selectedCategoryId }
        if (selectedIndex != -1) {
            categorySpinner.setSelection(selectedIndex)
        }
    }

    private fun EditText.focus() {
        this.isFocusable = true
        this.isFocusableInTouchMode = true
        this.visibility = View.VISIBLE
        this.isEnabled = true
        this.isCursorVisible = true
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
            R.id.info -> {
                val countS: String = getString(R.string.count1)
                val countT: String = getString(R.string.count2)
                val countM: String = getString(R.string.minuts)
                val count = noteEdt.text.length
                val time = count / 512
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.inf)
                    .setIcon(R.drawable.info)
                    .setMessage("$countS: $count \n$countT ≈$time $countM")
                    .setPositiveButton("OK") {_, _ ->
                    }
                    .show()
                true
            }
            R.id.copy_text -> {
                val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("UID", getText())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, R.string.copied_text, Toast.LENGTH_SHORT).show()
                true
            }
            R.id.share_text -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getText())
                }
                this.startActivity(Intent.createChooser(intent, "Share via"))
                true
            }
            R.id.jumpToEnd -> {
                scrollView.fullScroll(View.FOCUS_DOWN)
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
            val selectedCategory = categorySpinner.selectedItem as Category
            val bg = bgId

            if (noteDescription.isNotEmpty() or noteTitle.isNotEmpty()) {

                val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
                val date = Date().time
                val currentDateAndTime: String = sdf.format(date)

                if (noteType.equals("Edit")) {
                    if (noteTitle.isEmpty()) {
                        noteTitle = emptyTitle
                    }
                    val updatedNote = Note(noteTitle, noteDescription.toHtml(), date.toString(), noteLock, label, selectedCategory.id, bg)
                    updatedNote.id = noteID
                    viewModal.updateNote(updatedNote)
                } else {
                    if (!added){
                        if (noteTitle.isEmpty()) {
                            noteTitle = emptyTitle
                        }
                        viewModal.addNote(Note(noteTitle,  noteDescription.toHtml(), date.toString(), noteLock, label, selectedCategory.id, bg))
                        added = true
                    }

                }
                savedTxt.text = resources.getString(R.string.saved) + ": " + currentDateAndTime
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

    private fun getText(): String {
        return ("${noteTitleEdt.text}\n${Html.fromHtml(noteEdt.text.toString(), Html.FROM_HTML_MODE_COMPACT)}")
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
        noteEdt = findViewById(R.id.idEdtNoteDesc)
        val noteText = noteEdt.text.toString()
        val noteTitle = noteTitleEdt.text.toString()

        createAndSaveWordDocScoped(this, noteTitle, noteText)
    }

    private fun createAndSaveWordDocScoped(context: Context, noteName: String, noteText: String) {
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


    private fun setBgColorFromId(selectedColorId: Int){
        val selectedColor = if (selectedColorId == -1) null else {
            val isDarkTheme = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val colors = if (isDarkTheme) resources.getIntArray(R.array.note_colors_dark)
            else resources.getIntArray(R.array.note_colors_light)
            colors[selectedColorId]
        }
        if (selectedColor == null) {
            bg.setBackgroundColor("#00000000".toColorInt())
            topBar.setBackgroundColor("#00000000".toColorInt())
        } else {
            topBar.setBackgroundColor(selectedColor)
            bg.setBackgroundColor(selectedColor)
        }
    }

    private fun showColorPickerDialog(context: Context, onColorSelected: (Int) -> Unit) {
        val isDarkTheme = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val lightColors = context.resources.getIntArray(R.array.note_colors_light)
        val darkColors = context.resources.getIntArray(R.array.note_colors_dark)
        val colors = if (isDarkTheme) darkColors else lightColors
        val allColors = IntArray(colors.size + 1).apply {
            this[0] = -1
            for (i in colors.indices) {
                this[i + 1] = i
            }
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null)
        val container = dialogView.findViewById<GridLayout>(R.id.color_container)

        container.columnCount = 4

        for ((index, color) in allColors.withIndex()) {
            val circleView = LayoutInflater.from(context).inflate(R.layout.item_color_circle, container, false) as ImageView

            if (color == -1) {
            } else {
                circleView.setColorFilter(colors[color])
            }

            circleView.setOnClickListener {
                onColorSelected(if (index == 0) -1 else index - 1)
            }

            container.addView(circleView)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.choose_bg)
            .setView(dialogView)
            .setNegativeButton(R.string.back, null)
            .show()
    }

    private fun generateSum(){
        val tokenizer = WordPieceTokenizer(this)
        val bert = MobileBertInterpreter(this)
        val text = getText()

        val tokens: IntArray = tokenizer.tokenize(text)

        val input = arrayOf(intArrayOf(tokens.firstOrNull() ?: 0))
        Log.d("BERT", "Input tokens: ${tokens.joinToString()}")

        val result: FloatArray = bert.predict(input)[0][0]

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.ai_sum)
            .setIcon(R.drawable.ai)
            .setMessage(tokenizer.detokenize(result.map { it.toInt() }))
            .setPositiveButton("OK") { _, _ -> }
            .show()
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