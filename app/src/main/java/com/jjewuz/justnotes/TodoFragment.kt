package com.jjewuz.justnotes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class TodoFragment : Fragment(), TodoClickInterface, TodoLongClickInterface {

    private lateinit var todoViewModel: TodoViewModel
    private lateinit var noTasks: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_todo, container, false)

        noTasks = v.findViewById(R.id.notasks)

        val recyclerView: RecyclerView = v.findViewById(R.id.recyclerView)
        val adapter = TodoAdapter(requireActivity(), emptyList(), this, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        val standardBottomSheet = v.findViewById<FrameLayout>(R.id.standard_bottom_sheet)
        val standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = STATE_HIDDEN

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

        val addTodoButton: Button = v.findViewById(R.id.save)
        val addButton: FloatingActionButton = v.findViewById(R.id.addTodoButton)
        addButton.setOnClickListener {
            standardBottomSheetBehavior.state = STATE_COLLAPSED
        }

        addTodoButton.setOnClickListener {
            val todoText: TextInputEditText = v.findViewById(R.id.todoEditText)

            if ((todoText.length() <= 40) and (todoText.length() != 0)){
                val newTodo = Todo(
                    todoText.text.toString(),
                    false
                )

                todoViewModel.insert(newTodo)

                standardBottomSheetBehavior.state = STATE_HIDDEN

                todoText.text?.clear()
            }
        }

        return v
    }

    private fun deleteTodoItem(todo: Todo) {
        todoViewModel.delete(todo)
    }

    override fun onTodoClick(todo: Todo) {

    }

    override fun onTodoLongClick(todo: Todo) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.delWarnTodo)
            .setNegativeButton(resources.getString(R.string.neg)) { dialog, which ->
            }
            .setPositiveButton(R.string.pos) { dialog, which ->
                todoViewModel.delete(todo)
                Toast.makeText(requireActivity(), R.string.deleted, Toast.LENGTH_LONG).show()
            }
            .show()
    }

}