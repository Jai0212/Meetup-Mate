package com.example.meetupmate

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // If logo clicked, go to main page
        val logoMainChat: ImageView = findViewById(R.id.logoMainChat)
        logoMainChat.setOnClickListener {
            startActivity(Intent(this@ChatMain, MainActivity::class.java))
        }

        // To get back arrow
        val tlMainChat: androidx.appcompat.widget.Toolbar = findViewById(R.id.tlMainChat)
        setSupportActionBar(tlMainChat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val chatsPosts = mutableListOf<Post>()
        var postsProcessed = 0

        DatabaseManager.getPostsForUser { userPosts ->
            chatsPosts.addAll(userPosts)

            DatabaseManager.getAllPosts { otherPosts ->
                for (post in otherPosts) {
                    DatabaseManager.hasJoinedPost(post) { hasJoined ->
                        postsProcessed++

                        if (hasJoined) {
                            chatsPosts.add(post)
                        }

                        if (postsProcessed == otherPosts.size) {
                            runOnUiThread {
                                val rvMainChat: RecyclerView = findViewById(R.id.rvMainChat)
                                rvMainChat.layoutManager = LinearLayoutManager(this)
                                rvMainChat.adapter = RecyclerViewMainChatAdapter(chatsPosts, this)
                            }
                        }
                    }
                }
            }

            if (postsProcessed == 0) {
                val rvMainChat: RecyclerView = findViewById(R.id.rvMainChat)
                rvMainChat.layoutManager = LinearLayoutManager(this)
                rvMainChat.adapter = RecyclerViewMainChatAdapter(chatsPosts, this)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@ChatMain, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}