package com.jjewuz.justnotes.Utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.jjewuz.justnotes.R

data class Option(val icon: Int, val title: String, val action: String)


class GridAdapter(
    private val context: Context,
    private val options: List<Option>,
    private val onItemClick: (Option) -> Unit
) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {

    private var maxHeight = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.option_card)
        val icon: ImageView = view.findViewById(R.id.icon)
        val title: TextView = view.findViewById(R.id.option_title)
        val action: TextView = view.findViewById(R.id.option_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.option_button, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.icon.setImageResource(option.icon)
        holder.title.text = option.title
        holder.action.text = option.action

        holder.card.setOnClickListener {
            onItemClick(option)
        }

        holder.card.post {
            val height = holder.card.height
            if (height > maxHeight) {
                maxHeight = height
                notifyDataSetChanged()
            }
            holder.card.layoutParams.height = maxHeight
        }
    }

    override fun getItemCount() = options.size
}
