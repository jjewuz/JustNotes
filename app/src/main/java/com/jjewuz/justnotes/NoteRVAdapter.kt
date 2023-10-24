package com.jjewuz.justnotes

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteRVAdapter(
    val context: Context,
    val noteClickInterface: NoteClickInterface,
    val noteLongClickInterface: NoteLongClickInterface
) :
    RecyclerView.Adapter<NoteRVAdapter.ViewHolder>() {

    lateinit var sharedPref: SharedPreferences

    private val allNotes = ArrayList<Note>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTV: TextView = itemView.findViewById(R.id.idTVNote)
        val descTV: TextView = itemView.findViewById(R.id.notedesc)
        val dateTV: TextView = itemView.findViewById(R.id.idTVDate)
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

        holder.noteTV.text = allNotes.get(position).noteTitle
        holder.dateTV.setText(context.getString(R.string.lastedit)+ allNotes.get(position).timeStamp)

        if (isPreview){
            holder.descTV.text = Utils.fromHtml(allNotes.get(position).noteDescription)
        } else{
            holder.descTV.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            noteClickInterface.onNoteClick(allNotes.get(position), position)
        }

        holder.itemView.setOnLongClickListener {
            noteLongClickInterface.onNoteLongClick(allNotes.get(position))
            return@setOnLongClickListener true
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




