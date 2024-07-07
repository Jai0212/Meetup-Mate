package com.example.meetupmate

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Search : AppCompatActivity() {

    @SuppressLint("UseSupportActionBar")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tlSearchToolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.tlSearchToolbar)
        setSupportActionBar(tlSearchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val actvSearch: AutoCompleteTextView = findViewById(R.id.actvSearch)
        actvSearch.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val usersUsernames = mutableListOf<String>()

        DatabaseManager.getAllUsers { users ->
            for (user in users) {
                usersUsernames.add(user.username)
            }
        }

        val autoCompleteAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, usersUsernames)
        actvSearch.setAdapter(autoCompleteAdapter)
        actvSearch.threshold = 1

        actvSearch.setOnItemClickListener { adapterView, view, i, l ->
            val selectedUsername = autoCompleteAdapter.getItem(i)
            val intent = Intent(this@Search, Profile::class.java)
            intent.putExtra("username", selectedUsername)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@Search, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}