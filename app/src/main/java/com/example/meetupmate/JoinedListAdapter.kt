package com.example.meetupmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JoinedListAdapter(private val joinedList: List<String>) : RecyclerView.Adapter<JoinedListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.joined_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = joinedList[position]
        holder.tvJoinedItem.text = name
    }

    override fun getItemCount(): Int {
        return joinedList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJoinedItem: TextView = itemView.findViewById(R.id.tvJoinedItem)
    }
}
