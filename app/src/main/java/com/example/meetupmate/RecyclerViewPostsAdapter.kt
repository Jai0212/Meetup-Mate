package com.example.meetupmate

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewPostsAdapter(private var mData: List<Post>, val context: Context) : RecyclerView.Adapter<RecyclerViewPostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_card_view, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]

        DatabaseManager.loadImageIntoImageView(item.image, holder.tvImage)

        holder.tvPostTitle.text = item.title
        holder.tvPostDateAndTime.text = item.dateAndTime
        holder.tvPostDescription.text = item.description

        if (DatabaseManager.currUser.email == item.creatorEmail) {
            holder.btnJoin.visibility = GONE
            holder.ivDelete.visibility = VISIBLE

            holder.ivDelete.setOnClickListener {
                DatabaseManager.deletePost(item)
                val updatedList = mData.toMutableList()
                updatedList.remove(item)
                mData = updatedList.toList()
                notifyItemRemoved(position)
            }
        }
        else {
            holder.ivDelete.visibility = GONE
            holder.btnJoin.visibility = VISIBLE

            DatabaseManager.hasJoinedPost(item) { hasJoined ->
                if (hasJoined) {
                    holder.btnJoin.text = "Leave"
                    holder.btnJoin.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EB4538"))
                }
                else {
                    holder.btnJoin.text = "Join"
                    holder.btnJoin.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFDB58"))
                }
            }

            holder.btnJoin.setOnClickListener {
                DatabaseManager.hasJoinedPost(item) { hasJoined ->
                    if (hasJoined) {
                        DatabaseManager.leavePost(item)
                        holder.btnJoin.text = "Join"
                        holder.btnJoin.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFDB58"))

                        if (context is Profile) {
                            val updatedList = mData.toMutableList()
                            updatedList.remove(item)
                            mData = updatedList.toList()
                            notifyItemRemoved(position)
                        }
                    }
                    else {
                        DatabaseManager.joinPost(item)
                        holder.btnJoin.text = "Leave"
                        holder.btnJoin.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EB4538"))
                    }
                }
            }
        }

        holder.btnWhoElseJoin.setOnClickListener {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.list_of_joined)

            val rvJoinedList: RecyclerView = dialog.findViewById(R.id.rvJoinedList)
            rvJoinedList.layoutManager = LinearLayoutManager(context)

            val joinedListUsername = mutableListOf<String>()

            DatabaseManager.allJoinedPost(item) { joinedList ->
                for (user in joinedList) {

                    DatabaseManager.getUser(user) {
                        if (it != null) {
                            joinedListUsername.add(it.username)

                            val adapter = JoinedListAdapter(joinedListUsername)
                            rvJoinedList.adapter = adapter
                        }
                    }
                }
            }

            val listOfJoinedOKBTN: Button = dialog.findViewById(R.id.listOfJoinedOKBTN)
            listOfJoinedOKBTN.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun update(posts: List<Post>) {
        mData = posts
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvImage: ImageView = itemView.findViewById(R.id.tvImage)
        val tvPostTitle: TextView = itemView.findViewById(R.id.tvPostTitle)
        val tvPostDateAndTime: TextView = itemView.findViewById(R.id.tvPostDateAndTime)
        val tvPostDescription: TextView = itemView.findViewById(R.id.tvPostDescription)
        val btnJoin: Button = itemView.findViewById(R.id.btnJoin)
        val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)
        val btnWhoElseJoin: Button = itemView.findViewById(R.id.btnWhoElseJoin)
    }
}
