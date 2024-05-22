package com.jjewuz.justnotes.Todos

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.jjewuz.justnotes.R

class TodoAdapter(private val viewModelStoreOwner: ViewModelStoreOwner, var todos: List<Todo>, val todoClickInterface: TodoClickInterface, private val todoLongClickInterface: TodoLongClickInterface) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val completedCheckBox: CheckBox = itemView.findViewById(R.id.completedCheckBox)
        val timeText: TextView = itemView.findViewById(R.id.time_set)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todo = todos[position]
        holder.titleTextView.text = todo.text
        holder.completedCheckBox.isChecked = todo.isCompleted
        holder.timeText.text = todo.setTime

        if (todo.isCompleted){
            holder.titleTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.titleTextView.paintFlags = 0
        }


        val todoViewModel = ViewModelProvider(viewModelStoreOwner)[TodoViewModel::class.java]

        holder.completedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            todo.isCompleted = isChecked
            todoViewModel.update(todo)
        }

        holder.itemView.setOnClickListener{
            todo.isCompleted = !todo.isCompleted
            todoViewModel.update(todo)
        }

        holder.itemView.setOnLongClickListener {
            todoLongClickInterface.onTodoLongClick(todo)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return todos.size
    }
}

interface TodoClickInterface {
    fun onTodoClick(todo: Todo)
}

interface TodoLongClickInterface {
    fun onTodoLongClick(todo: Todo)
}
