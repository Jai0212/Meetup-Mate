package com.example.meetupmate

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar

class Chat : AppCompatActivity() {
    private lateinit var adapter: RecyclerViewChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // If logo clicked, go to main page
        val logoChat: ImageView = findViewById(R.id.logoChat)
        logoChat.setOnClickListener {
            startActivity(Intent(this@Chat, MainActivity::class.java))
        }

        // To get back arrow
        val tlChat: androidx.appcompat.widget.Toolbar = findViewById(R.id.tlChat)
        setSupportActionBar(tlChat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val postTitle = intent.getStringExtra("postTitle")
        val postImage = intent.getStringExtra("postImage")

        val tvChatName: TextView = findViewById(R.id.tvChatName)
        val ivChatImage: ImageView = findViewById(R.id.ivChatImage)

        tvChatName.text = postTitle
        DatabaseManager.loadImageIntoImageView(postImage!!, ivChatImage)

        val edtChatMessageInput: EditText = findViewById(R.id.edtChatMessageInput)
        val ivChatSend: ImageView = findViewById(R.id.ivChatSend)

        DatabaseManager.getPost(postImage) { post ->
            DatabaseManager.getMessages(post) { messages ->

                val rvChat: RecyclerView = findViewById(R.id.rvChat)
                rvChat.layoutManager = LinearLayoutManager(this)
                adapter = RecyclerViewChatAdapter(mutableListOf(), post)
                rvChat.adapter = adapter

                val mutableMessages = messages?.toMutableList()
                mutableMessages?.let {
                    adapter.updateMessages(it)
                    adapter.notifyDataSetChanged()
                }

                ivChatSend.setOnClickListener {
                    val messageText = edtChatMessageInput.text.toString().trim()

                    if (messageText.isNotEmpty()) {
                        val newMessage = Message(
                            DatabaseManager.currUser,
                            messageText,
                            getCurrentDateTime()
                        )

                        edtChatMessageInput.setText("")
                        // To lower down keyboard
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(edtChatMessageInput.windowToken, 0)

                        DatabaseManager.addMessage(newMessage, post)

                        adapter.addMessage(newMessage)
                        adapter.notifyDataSetChanged()
                    } else {
                        val toast = Toast(applicationContext)
                        val view: View = layoutInflater.inflate(R.layout.custom_toast, findViewById(R.id.customToast))
                        toast.view = view
                        toast.duration = Toast.LENGTH_SHORT
                        val customToastText: TextView = view.findViewById(R.id.customToastText)
                        customToastText.text = "Enter a Message"
                        toast.show()
                    }
                }

                // If group title clicked
                tvChatName.setOnClickListener {
                    val dialog = Dialog(this)
                    dialog.setContentView(R.layout.post_card_view)

                    val btnDialogPostOKBTN: Button = dialog.findViewById(R.id.btnDialogPostOKBTN)
                    val btnJoin: Button = dialog.findViewById(R.id.btnJoin)
                    val ivDelete: ImageView = dialog.findViewById(R.id.ivDelete)
                    val tvImage: ImageView = dialog.findViewById(R.id.tvImage)
                    val tvPostTitle: TextView = dialog.findViewById(R.id.tvPostTitle)
                    val tvPostDateAndTime: TextView = dialog.findViewById(R.id.tvPostDateAndTime)
                    val tvPostDescription: TextView = dialog.findViewById(R.id.tvPostDescription)
                    val btnWhoElseJoin: Button = dialog.findViewById(R.id.btnWhoElseJoin)
                    val postCardView: LinearLayout = dialog.findViewById(R.id.postCardView)

                    postCardView.layoutParams.width = resources.getDimensionPixelSize(R.dimen.dialogBoxWidth)
                    postCardView.layoutParams = postCardView.layoutParams

                    btnJoin.visibility = GONE
                    ivDelete.visibility = GONE
                    btnDialogPostOKBTN.visibility = VISIBLE
                    DatabaseManager.loadImageIntoImageView(postImage, tvImage)
                    tvPostTitle.text = postTitle
                    tvPostDateAndTime.text = post.dateAndTime
                    tvPostDescription.text = post.description

                    btnWhoElseJoin.setOnClickListener {
                        val whoElseJoinDialog = Dialog(this)
                        whoElseJoinDialog.setContentView(R.layout.list_of_joined)

                        val rvJoinedList: RecyclerView = whoElseJoinDialog.findViewById(R.id.rvJoinedList)
                        rvJoinedList.layoutManager = LinearLayoutManager(this)

                        val joinedListUsername = mutableListOf<String>()

                        DatabaseManager.allJoinedPost(post) { joinedList ->
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

                        val listOfJoinedOKBTN: Button = whoElseJoinDialog.findViewById(R.id.listOfJoinedOKBTN)
                        listOfJoinedOKBTN.setOnClickListener {
                            whoElseJoinDialog.dismiss()
                        }

                        whoElseJoinDialog.show()
                    }

                    btnDialogPostOKBTN.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@Chat, ChatMain::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getCurrentDateTime(): Timestamp {
        val calendar = Calendar.getInstance()

        val dateFormatter = SimpleDateFormat("dd-MM")
        val timeFormatter = SimpleDateFormat("HH:mm")

        val date: String = dateFormatter.format(calendar.time)
        val time: String = timeFormatter.format(calendar.time)

        return Timestamp(date, time)
    }
}
