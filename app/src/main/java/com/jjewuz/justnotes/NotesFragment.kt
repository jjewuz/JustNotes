package com.jjewuz.justnotes

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.marginLeft
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesFragment : Fragment(), NoteClickInterface, NoteLongClickInterface {
    lateinit var viewModal: NoteViewModal
    lateinit var notesRV: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var addFAB: FloatingActionButton
    lateinit var nothing: TextView
    lateinit var viewIcon: MenuItem

    private lateinit var bottomAppBar: BottomAppBar

    private lateinit var labelGroup: ChipGroup
    private lateinit var label1: Chip
    private lateinit var label2: Chip
    private lateinit var label3: Chip

    lateinit var noteRVAdapter: NoteRVAdapter
    lateinit var allItems: LiveData<List<Note>>

    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        var reverse = sharedPref.getBoolean("reversed", false)
        var isGrid = sharedPref.getBoolean("grid", false)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar, menu)
                viewIcon = menu.findItem(R.id.view_change)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                viewIcon.setIcon(R.drawable.reminders)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.view_change -> {
                        replaceFragment(TodoFragment())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val v = inflater.inflate(R.layout.fragment_notes, container, false)

        bottomAppBar = v.findViewById(R.id.bottomAppBar)
        notesRV = v.findViewById(R.id.notes)
        progressBar = v.findViewById(R.id.progress_bar)
        addFAB = v.findViewById(R.id.idFAB)
        nothing = v.findViewById(R.id.nothing)
        labelGroup = v.findViewById(R.id.chipGroup)
        label1 = v.findViewById(R.id.label1)
        label2 = v.findViewById(R.id.label2)
        label3 = v.findViewById(R.id.label3)

        ViewCompat.setOnApplyWindowInsetsListener(addFAB) { vi, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = vi.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = insets.bottom + 20
            params.rightMargin = insets.right + 40
            vi.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }

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


        if (isGrid){
            val layoutManager = GridLayoutManager(requireActivity(), 2, VERTICAL, reverse)
            notesRV.layoutManager = layoutManager
        }else{
            val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, reverse)
            notesRV.layoutManager = layoutManager
        }

        noteRVAdapter = NoteRVAdapter(requireActivity(), this, this)

        notesRV.adapter = noteRVAdapter

        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[NoteViewModal::class.java]

        allItems = viewModal.getNotes()

        labelGroup.setOnCheckedStateChangeListener { group, checkedID ->
            when(group.checkedChipId) {
                R.id.important -> {
                    allItems = viewModal.getLabeled("important")
                }
                R.id.useful -> {
                    allItems = viewModal.getLabeled("useful")
                }
                R.id.hobby -> {
                    allItems = viewModal.getLabeled("hobby")
                }
                R.id.label1 -> {
                    allItems = viewModal.getLabeled("label1")
                }
                R.id.label2 -> {
                    allItems = viewModal.getLabeled("label2")
                }
                R.id.label3 -> {
                    allItems = viewModal.getLabeled("label3")
                }
                else -> allItems = viewModal.getNotes()
            }
            updateList(allItems)
        }

        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sorting -> {
                    reverse = !reverse
                    with (sharedPref.edit()){
                        putBoolean("reversed", reverse)
                        apply()
                    }
                    val fragmentManager = parentFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.place_holder, NotesFragment())
                    fragmentTransaction.commit ()
                    true
                }
                R.id.style -> {
                    isGrid = !isGrid
                    with (sharedPref.edit()){
                        putBoolean("grid", isGrid)
                        apply()
                    }
                    val fragmentManager = parentFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.place_holder, NotesFragment())
                    fragmentTransaction.commit ()
                    true
                }

                else -> false
            }

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

        bottomAppBar.setNavigationOnClickListener {
            val modalBottomSheet = ModalBottomSheet()
            modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
        }

        addFAB.setOnClickListener {
            val intent = Intent(requireActivity(), AddEditNoteActivity::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), addFAB, "transition_fab")
            startActivity(intent, options.toBundle())
        }

        context?.let {
            bottomAppBar?.setOnTouchListener(object : OnSwipeTouchListener(it) {

                override fun onSwipeLeft() {
                    replaceFragment(TodoFragment())
                    super.onSwipeLeft()
                }

                override fun onSwipeRight() {
                    replaceFragment(TodoFragment())
                    super.onSwipeRight()
                }
            })
        }

        return v
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
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
    }

    override fun onNoteClick(note: Note, num: Int) {
        openNote(note)
    }


    override fun onNoteLongClick(note: Note) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inf = requireActivity().layoutInflater.inflate(R.layout.note_options, null)
        val edit = inf.findViewById<MaterialCardView>(R.id.edit)
        val widget = inf.findViewById<MaterialCardView>(R.id.tohome)
        val delete = inf.findViewById<MaterialCardView>(R.id.delete)

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
                .setNegativeButton(resources.getString(R.string.neg)) { dialog, which ->
                }
                .setPositiveButton(R.string.pos) { dialog, which ->
                    viewModal.deleteNote(note)
                    updateList(allItems)
                    editor.cancel()
                    Toast.makeText(requireActivity(), R.string.deleted, Toast.LENGTH_LONG).show()
                }
                .show()
        }
        edit.setOnClickListener {
            openNote(note)
            editor.cancel()
        }
        widget.setOnClickListener {
            val sharedPreferences = context?.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            sharedPreferences?.edit()?.putInt("note_id", note.id)?.apply()
            pushWidget()
            Toast.makeText(requireContext(), R.string.note_set_to_widget, Toast.LENGTH_SHORT).show()
            editor.cancel()
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
       items.observe(viewLifecycleOwner, Observer { list ->
            list?.let {
                noteRVAdapter.updateList(it)
            }
        })
    }

}