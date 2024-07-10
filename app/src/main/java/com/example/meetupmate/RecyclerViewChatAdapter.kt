package com.example.meetupmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewChatAdapter(private var mData: MutableList<Message>, private val post: Post) : RecyclerView.Adapter<RecyclerViewChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_view_left, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]

        DatabaseManager.loadImageIntoImageView(item.sender.profileImage, holder.ivChatMessageImageLeft)
        holder.tvChatMessageMessageLeft.text = item.message
        holder.tvChatMessageDateLeft.text = "${item.timestamp.date}, ${item.timestamp.time}"

        // If current user sends message, message appears on right
        if (item.sender.email == DatabaseManager.currUser.email) {
            val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.chat_view_right, holder.clChatViewLeft, false)

            val tvChatMessageMessageRight = view.findViewById<TextView>(R.id.tvChatMessageMessageRight)
            val tvChatMessageDateRight = view.findViewById<TextView>(R.id.tvChatMessageDateRight)
            val ivChatMessageImageRight = view.findViewById<ImageView>(R.id.ivChatMessageImageRight)

            tvChatMessageMessageRight.text = item.message
            tvChatMessageDateRight.text = "${item.timestamp.date}, ${item.timestamp.time}"
            DatabaseManager.loadImageIntoImageView(item.sender.profileImage, ivChatMessageImageRight)

            holder.clChatViewLeft.removeAllViews()
            holder.clChatViewLeft.addView(view)
        }
        else {
            holder.cvChatMessageWholeLeft.visibility = View.VISIBLE
            holder.tvChatMessageMessageLeft.visibility = View.VISIBLE
            holder.tvChatMessageDateLeft.visibility = View.VISIBLE
            holder.ivChatMessageImageLeft.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun updateMessages(newMessages: MutableList<Message>) {
        mData = newMessages
        notifyDataSetChanged()
    }

    fun addMessage(message: Message) {
        mData.add(message)
        notifyItemInserted(mData.size - 1)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clChatViewLeft: ConstraintLayout = itemView.findViewById(R.id.clChatViewLeft)
        val cvChatMessageWholeLeft: CardView = itemView.findViewById(R.id.cvChatMessageWholeLeft)
        val tvChatMessageMessageLeft: TextView = itemView.findViewById(R.id.tvChatMessageMessageLeft)
        val tvChatMessageDateLeft: TextView = itemView.findViewById(R.id.tvChatMessageDateLeft)
        val ivChatMessageImageLeft: ImageView = itemView.findViewById(R.id.ivChatMessageImageLeft)
    }
}