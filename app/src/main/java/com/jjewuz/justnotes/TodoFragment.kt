package com.jjewuz.justnotes

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.jjewuz.justnotes.Utils.hideKeyboard
import org.checkerframework.common.subtyping.qual.Bottom
import java.util.Calendar

class TodoFragment :Fragment(), TodoClickInterface, TodoLongClickInterface {

    private lateinit var todoViewModel: TodoViewModel
    private lateinit var noTasks: TextView
    private lateinit var bottomAppBar: BottomAppBar

    lateinit var viewIcon: MenuItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_todo, container, false)
        val sharedPref = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isDev = sharedPref.getBoolean("is_dev", false)
        bottomAppBar = v.findViewById(R.id.bottomAppBar)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar, menu)
                viewIcon = menu.findItem(R.id.view_change)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                viewIcon.setIcon(R.drawable.note)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.view_change -> {
                        val fragmentManager = parentFragmentManager
                        val fragmentTransaction = fragmentManager.beginTransaction()
                        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        fragmentTransaction.replace(R.id.place_holder, NotesFragment())
                        fragmentTransaction.commit ()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        noTasks = v.findViewById(R.id.notasks)

        val recyclerView: RecyclerView = v.findViewById(R.id.recyclerView)
        val adapter = TodoAdapter(requireActivity(), emptyList(), this, this)
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        todoViewModel = ViewModelProvider(requireActivity()).get(TodoViewModel::class.java)
        todoViewModel.allTodos.observe(viewLifecycleOwner) { todos ->
            adapter.todos = todos
            adapter.notifyDataSetChanged()
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                val count = adapter.itemCount
                if (count == 0){
                    recyclerView.visibility = View.GONE
                    noTasks.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    noTasks.visibility = View.GONE
                }
            }
        })
        val addButton: FloatingActionButton = v.findViewById(R.id.addTodoButton)
        addButton.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setTitle(R.string.add_reminder)
            builder.setIcon(R.drawable.reminders)
            val inf = requireActivity().layoutInflater.inflate(R.layout.todo_add, null)
            val todoText: TextInputEditText = inf.findViewById(R.id.todoEditText)
            val dataPickerButton: Chip = inf.findViewById(R.id.time_pick)
            if (!isDev)
                dataPickerButton.visibility = View.GONE
            dataPickerButton.setOnClickListener {
                val constraintsBuilder = CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now())
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.push_notification)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(constraintsBuilder.build())
                val picker = datePicker.build()
                picker.show(parentFragmentManager, "tag")
                picker.addOnPositiveButtonClickListener (){
                    val date = picker.selection
                    val calendar = Calendar.getInstance()
                    if (date != null) {
                        calendar.timeInMillis = date
                        val timePicker = MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(12)
                            .setMinute(10)
                        val timePick = timePicker.build()
                        timePick.show(parentFragmentManager, "tag")
                        timePick.addOnPositiveButtonClickListener {
                            dataPickerButton.text = "${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH)}.${calendar.get(Calendar.YEAR)} - ${timePick.hour}:${timePick.minute}"
                        }
                    }

                }
            }
            builder.setView(inf)
            builder.setPositiveButton(R.string.add) { dialog, id ->
                if ((todoText.length() <= 100) and (todoText.length() != 0)) {
                    val newTodo = Todo(
                        todoText.text.toString(),
                        false
                    )

                    todoViewModel.insert(newTodo)
                }
            }
            builder.create().show()
        }

        bottomAppBar.setNavigationOnClickListener {
            val modalBottomSheet = ModalBottomSheet()
            modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
        }

        return v
    }


    override fun onTodoClick(todo: Todo) {
    }

    override fun onTodoLongClick(todo: Todo) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.delete_reminder)
            .setIcon(R.drawable.delete)
            .setNegativeButton(resources.getString(R.string.neg)) { dialog, which ->
            }
            .setPositiveButton(R.string.pos) { dialog, which ->
                todoViewModel.delete(todo)
                Toast.makeText(requireActivity(), R.string.deleted, Toast.LENGTH_LONG).show()
            }
            .show()
    }

}