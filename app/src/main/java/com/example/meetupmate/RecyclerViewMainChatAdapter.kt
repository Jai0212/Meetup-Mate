package com.example.meetupmate

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewMainChatAdapter(private val mData: List<Post>, private val context: Context) : RecyclerView.Adapter<RecyclerViewMainChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_chat_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]

        DatabaseManager.loadImageIntoImageView(item.image, holder.ivMainChatImage)
        holder.tvMainChatName.text = item.title

        // When a particular chat is selected
        holder.llMainChat.setOnClickListener {
            val intent = Intent(context, Chat::class.java)
            intent.putExtra("postTitle", item.title)
            intent.putExtra("postImage", item.image)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llMainChat: LinearLayout = itemView.findViewById(R.id.llMainChat)
        val ivMainChatImage: ImageView = itemView.findViewById(R.id.ivMainChatImage)
        val tvMainChatName: TextView = itemView.findViewById(R.id.tvMainChatName)
    }
}