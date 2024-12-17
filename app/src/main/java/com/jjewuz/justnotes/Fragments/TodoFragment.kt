package com.jjewuz.justnotes.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.jjewuz.justnotes.Activities.ModalBottomSheet
import com.jjewuz.justnotes.Notifications.NotificationHelper
import com.jjewuz.justnotes.R
import com.jjewuz.justnotes.Todos.Todo
import com.jjewuz.justnotes.Todos.TodoAdapter
import com.jjewuz.justnotes.Todos.TodoClickInterface
import com.jjewuz.justnotes.Todos.TodoLongClickInterface
import com.jjewuz.justnotes.Todos.TodoViewModel
import com.jjewuz.justnotes.Utils.OnSwipeTouchListener
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
        requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        bottomAppBar = v.findViewById(R.id.bottomAppBar)
        noTasks = v.findViewById(R.id.notasks)

        (activity as AppCompatActivity).setSupportActionBar(v.findViewById(R.id.topAppBar))

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
                        replaceFragment(NotesFragment())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val recyclerView: RecyclerView = v.findViewById(R.id.recyclerView)
        val adapter = TodoAdapter(requireActivity(), emptyList(), this, this)
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        todoViewModel = ViewModelProvider(requireActivity())[TodoViewModel::class.java]
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
        ViewCompat.setOnApplyWindowInsetsListener(addButton) { vi, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = vi.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = insets.bottom + 20
            params.rightMargin = insets.right + 40
            vi.layoutParams = params
            WindowInsetsCompat.CONSUMED
        }



        addButton.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireActivity())
            builder.setTitle(R.string.add_reminder)
            builder.setIcon(R.drawable.reminders)
            val inf = requireActivity().layoutInflater.inflate(R.layout.todo_add, null)
            val todoText: TextInputEditText = inf.findViewById(R.id.todoEditText)
            val dataPickerButton: Chip = inf.findViewById(R.id.time_pick)
            var data = ""
            var dateTime = LocalDateTime.of(2024, 3, 17, 10, 0)
            dataPickerButton.setOnClickListener {
                val constraintsBuilder = CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now())
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.push_notification)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(constraintsBuilder.build())
                val picker = datePicker.build()
                picker.show(parentFragmentManager, "tag")
                picker.addOnPositiveButtonClickListener{
                    val date = picker.selection
                    val calendar = Calendar.getInstance()
                    if (date != null) {
                        calendar.timeInMillis = date
                        val timePicker = MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(LocalTime.now().hour)
                            .setMinute(LocalTime.now().minute)
                        val timePick = timePicker.build()
                        timePick.show(parentFragmentManager, "tag")
                        timePick.addOnPositiveButtonClickListener {
                            data =  "${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH)+1}.${calendar.get(Calendar.YEAR)} - ${timePick.hour}:${timePick.minute}"
                            dateTime = LocalDateTime.of(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1, // Months are 0-based in Calendar
                                calendar.get(Calendar.DAY_OF_MONTH),
                                timePick.hour,
                                timePick.minute
                            )
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm")
                            val formattedDate = dateTime.format(formatter)
                            dataPickerButton.text = formattedDate
                            data = formattedDate
                        }
                    }

                }
            }
            builder.setView(inf)
            builder.setPositiveButton(R.string.add) { _, _ ->
                if ((todoText.length() <= 100) and (todoText.length() != 0)) {
                    val newTodo = Todo(
                        todoText.text.toString(),
                        false,
                        data
                    )
                    NotificationHelper(requireContext()).createNotification("JustNotes", todoText.text.toString(), 101, dateTime)

                    todoViewModel.insert(newTodo)
                }
            }
            builder.create().show()
        }

        bottomAppBar.setNavigationOnClickListener {
            val modalBottomSheet = ModalBottomSheet()
            modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
        }

        context?.let {
            bottomAppBar.setOnTouchListener(object : OnSwipeTouchListener(it) {

                override fun onSwipeLeft() {
                    replaceFragment(NotesFragment())
                    super.onSwipeLeft()
                }

                override fun onSwipeRight() {
                    replaceFragment(NotesFragment())
                    super.onSwipeRight()
                }
            })
        }

        return v
    }


    override fun onTodoClick(todo: Todo) {
    }

    override fun onTodoLongClick(todo: Todo) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.delete_reminder)
            .setIcon(R.drawable.delete)
            .setNegativeButton(resources.getString(R.string.neg)) { _, _ ->
            }
            .setPositiveButton(R.string.pos) { _, _ ->
                todoViewModel.delete(todo)
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

}