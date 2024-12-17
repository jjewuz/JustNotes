package com.jjewuz.justnotes.Notes

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.jjewuz.justnotes.Category.CategoryDao
import com.jjewuz.justnotes.R
import com.jjewuz.justnotes.Utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class NoteRVAdapter(
    val context: Context,
    private val noteClickInterface: NoteClickInterface,
    private val noteLongClickInterface: NoteLongClickInterface,
    private val categoryDao: CategoryDao
) :
    RecyclerView.Adapter<NoteRVAdapter.ViewHolder>() {

    private lateinit var sharedPref: SharedPreferences

    private val allNotes = ArrayList<Note>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTV: TextView = itemView.findViewById(R.id.idTVNote)
        val descTV: TextView = itemView.findViewById(R.id.notedesc)
        val dateTV: TextView = itemView.findViewById(R.id.idTVDate)
        val categoryChip: Chip = itemView.findViewById(R.id.category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.note_rv_item,
            parent, false
        )

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        sharedPref = holder.descTV.context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isPreview = sharedPref.getBoolean("enabledPreview", false)

        holder.noteTV.text = allNotes[position].noteTitle
        val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
        val date = allNotes[position].timeStamp
        val currentDateAndTime: String = try {
            sdf.format(date.toLong())
        } catch (e: Exception){
            date
        }

        holder.dateTV.text = context.getString(R.string.lastedit) + currentDateAndTime
        val currCategory = allNotes[position].categoryId
        holder.categoryChip.visibility = View.GONE
        if(currCategory != 1) {
            CoroutineScope(Dispatchers.Main).launch {
                val categoryName = currCategory?.let { getCategoryName(it) }
                holder.categoryChip.visibility = View.VISIBLE
                holder.categoryChip.text = categoryName
            }

        }


        if (isPreview){
            holder.descTV.text = Utils.fromHtml(allNotes[position].noteDescription.take(400))
        } else{
            holder.descTV.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            noteClickInterface.onNoteClick(allNotes[position], position)
        }

        holder.itemView.setOnLongClickListener {
            noteLongClickInterface.onNoteLongClick(allNotes[position])
            return@setOnLongClickListener true
        }
    }

    private suspend fun getCategoryName(categoryId: Int): String? {
        return withContext(Dispatchers.IO) {
            categoryDao.getCategoryNameById(categoryId)
        }
    }

    override fun getItemCount(): Int {
        return allNotes.size
    }

    fun updateList(newList: List<Note>) {

        allNotes.clear()
        allNotes.addAll(newList)
        notifyDataSetChanged()
    }
}


interface NoteClickInterface {
    fun onNoteClick(note: Note, num: Int)
}

interface NoteLongClickInterface {
    fun onNoteLongClick(note: Note)
}




