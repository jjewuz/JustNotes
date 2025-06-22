package com.jjewuz.justnotes.Fragments

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.loadingindicator.LoadingIndicator
import com.jjewuz.justnotes.Activities.AddEditNoteActivity
import com.jjewuz.justnotes.Activities.Profile
import com.jjewuz.justnotes.Category.Category
import com.jjewuz.justnotes.Category.CategoryViewModel
import com.jjewuz.justnotes.Notes.Note
import com.jjewuz.justnotes.Notes.NoteClickInterface
import com.jjewuz.justnotes.Notes.NoteDatabase
import com.jjewuz.justnotes.Notes.NoteLongClickInterface
import com.jjewuz.justnotes.Notes.NoteRVAdapter
import com.jjewuz.justnotes.Notes.NoteViewModal
import com.jjewuz.justnotes.Notes.NoteWidget
import com.jjewuz.justnotes.R
import com.jjewuz.justnotes.Utils.Utils
import com.jjewuz.justnotes.Utils.Utils.fromHtml

class NotesFragment : Fragment(), NoteClickInterface, NoteLongClickInterface {
    lateinit var viewModal: NoteViewModal
    lateinit var notesRV: RecyclerView
    lateinit var progressBar: LoadingIndicator
    private lateinit var addFAB: FloatingActionButton
    lateinit var nothing: TextView

    private lateinit var labelGroup: ChipGroup

    lateinit var noteRVAdapter: NoteRVAdapter
    lateinit var allItems: LiveData<List<Note>>
    private lateinit var searchView: SearchView
    private lateinit var accountButton: Button

    private lateinit var sharedPref: SharedPreferences

    private lateinit var categoryViewModel: CategoryViewModel
    private var selectedCategoryId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        var reverse = sharedPref.getBoolean("reversed", false)
        var isGrid = sharedPref.getBoolean("grid", false)

        val v = inflater.inflate(R.layout.fragment_notes, container, false)
        notesRV = v.findViewById(R.id.notes)
        progressBar = v.findViewById(R.id.progress_bar)
        addFAB = v.findViewById(R.id.idFAB)
        nothing = v.findViewById(R.id.nothing)
        labelGroup = v.findViewById(R.id.chipGroup)


        ViewCompat.setOnApplyWindowInsetsListener(addFAB) { vi, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = vi.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = insets.bottom + 20
            params.rightMargin = insets.right + 40
            vi.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }

        ViewCompat.setOnApplyWindowInsetsListener(notesRV) { vi, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = vi.layoutParams as ViewGroup.MarginLayoutParams
            params.leftMargin = insets.left
            params.rightMargin = insets.right
            vi.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }

        accountButton = v.findViewById(R.id.account)
        accountButton.setOnClickListener {
            val intent = Intent(requireActivity(), Profile::class.java)
            startActivity(intent)
        }
        val searchPanel = v.findViewById<LinearLayout>(R.id.searchBar)

        ViewCompat.setOnApplyWindowInsetsListener(searchPanel) { vi, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = vi.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = insets.bottom + 90
            vi.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }


        if (isGrid){
            val layoutManager = GridLayoutManager(requireActivity(), 2, VERTICAL, reverse)
            notesRV.layoutManager = layoutManager
        }else{
            val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, reverse)
            notesRV.layoutManager = layoutManager
        }
        val categoryDao = NoteDatabase.getDatabase(requireContext()).getCategoryDao()
        noteRVAdapter = NoteRVAdapter(requireActivity(), this, this, categoryDao )

        notesRV.adapter = noteRVAdapter

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[NoteViewModal::class.java]

        allItems = viewModal.getNotes()

        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            setupCategoryChips(categories)
        }

        updateList(allItems)

        noteRVAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                progressBar.visibility = View.GONE
                val count = noteRVAdapter.itemCount
                if (count == 0){
                    notesRV.visibility = View.GONE
                    nothing.visibility = View.VISIBLE
                } else {
                    notesRV.visibility = View.VISIBLE
                    nothing.visibility = View.GONE
                }
            }
        })

        addFAB.setOnClickListener {
            val intent = Intent(requireActivity(), AddEditNoteActivity::class.java)
            startActivity(intent)
        }

        searchView = v.findViewById(R.id.search_bar)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                allItems = query?.let { viewModal.getQuery(it) }!!
                updateList(allItems)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                allItems = if (newText == ""){
                    viewModal.getNotes()
                } else {
                    newText?.let { viewModal.getQuery(it) }!!
                }
                updateList(allItems)
                return false
            }
        })




        return v
    }

    private fun setupCategoryChips(categories: List<Category>) {
        labelGroup.removeAllViews()
        val lText = resources.getString(R.string.label_selected)

        for (category in categories) {
            val chip = Chip(requireContext())
            chip.text = category.name
            if (category.name == "jjewuz" || category.name == "JustNotes"){
                chip.chipIcon = ResourcesCompat.getDrawable(resources ,R.drawable.star, context?.theme)
            }
            chip.isClickable = true
            chip.isCheckable = true
            chip.isChecked = category.id == selectedCategoryId

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Toast.makeText(requireContext(), "$lText ${chip.text}", Toast.LENGTH_SHORT).show()
                    selectedCategoryId = category.id
                    updateList(viewModal.getLabel(selectedCategoryId))
                } else {
                    selectedCategoryId = -1
                    updateList(allItems)
                }
            }

            labelGroup.addView(chip)
        }
    }

    private fun openNote(note: Note){
        val intent = Intent(requireActivity(), AddEditNoteActivity::class.java)
        intent.putExtra("noteType", "Edit")
        intent.putExtra("noteTitle", note.noteTitle)
        intent.putExtra("noteDescription", note.noteDescription)
        intent.putExtra("timestamp", note.timeStamp)
        intent.putExtra("security", note.security)
        intent.putExtra("label", note.label)
        intent.putExtra("noteId", note.id)
        intent.putExtra("categoryId", note.categoryId)
        intent.putExtra("bgId", note.bgId)
        startActivity(intent)
    }

    override fun onNoteClick(note: Note, num: Int) {
        openNote(note)
    }


    override fun onNoteLongClick(note: Note) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inf = requireActivity().layoutInflater.inflate(R.layout.note_options, null)
        val widget = inf.findViewById<MaterialCardView>(R.id.tohome)
        val delete = inf.findViewById<MaterialCardView>(R.id.delete)
        val notify = inf.findViewById<MaterialCardView>(R.id.tonoti)
        val share = inf.findViewById<MaterialCardView>(R.id.share_note)

        builder.setIcon(R.drawable.note)
        builder.setTitle(note.noteTitle)
        builder.setView(inf)
            .setPositiveButton(R.string.close) { _, _ ->
            }
        builder.create()
        val editor = builder.show()
        delete.setOnClickListener {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.delWarn)
                .setIcon(R.drawable.delete)
                .setMessage(R.string.delete_warn)
                .setNegativeButton(resources.getString(R.string.neg)) { _, _ ->
                }
                .setPositiveButton(R.string.pos) { _, _ ->
                    viewModal.deleteNote(note)
                    updateList(allItems)
                    editor.cancel()
                    Toast.makeText(requireActivity(), R.string.deleted, Toast.LENGTH_LONG).show()
                }
                .show()
        }
        widget.setOnClickListener {
            val sharedPreferences = context?.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            sharedPreferences?.edit()?.putInt("note_id", note.id)?.apply()
            pushWidget()
            Toast.makeText(requireContext(), R.string.note_set_to_widget, Toast.LENGTH_SHORT).show()
            editor.cancel()
        }
        notify.setOnClickListener {
            startPersistentNotification(requireContext(), note)
            Toast.makeText(requireContext(), resources.getString(R.string.notification_sent), Toast.LENGTH_SHORT).show()
            editor.cancel()
        }
        share.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${note.noteTitle}\n${fromHtml(note.noteDescription)}")
            }
            this.startActivity(Intent.createChooser(intent, "Share via"))
        }
    }

    private fun pushWidget(){
        val intent = Intent(requireActivity(), NoteWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids: IntArray = AppWidgetManager.getInstance(requireActivity()).getAppWidgetIds(ComponentName(requireActivity(), NoteWidget::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        requireActivity().sendBroadcast(intent)
    }

    fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        fragmentTransaction.replace(R.id.place_holder, fragment)
        fragmentTransaction.commit ()
    }

    private fun updateList(items: LiveData<List<Note>>){
       items.observe(viewLifecycleOwner) { list ->
           list?.let {
               noteRVAdapter.updateList(it)
           }
       }
    }

    private fun startPersistentNotification(context: Context, note: Note) {
        val serviceIntent = Intent(context, Utils.PersistentService::class.java).apply {
            putExtra("noteId", note.id)
            putExtra(Utils.PersistentService.NOTE_ID_KEY, note.id)
            putExtra(Utils.PersistentService.NOTE_LABEL_ID, note.categoryId ?: 0)
            putExtra("noteTitle", note.noteTitle)
            putExtra("noteDescription", note.noteDescription)
            putExtra("timestamp", note.timeStamp)
            putExtra("categoryId", note.categoryId ?: 0)
            putExtra("security", note.security)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }

}