package com.jjewuz.justnotes

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.get
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

    override fun onNoteClick(note: Note, num: Int) {
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


    override fun onNoteLongClick(note: Note) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.delWarn)
            .setIcon(R.drawable.delete)
            .setMessage(R.string.delete_warn)
            .setNegativeButton(resources.getString(R.string.neg)) { dialog, which ->
            }
            .setPositiveButton(R.string.pos) { dialog, which ->
                viewModal.deleteNote(note)
                updateList(allItems)
                Toast.makeText(requireActivity(), R.string.deleted, Toast.LENGTH_LONG).show()
            }
            .show()
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