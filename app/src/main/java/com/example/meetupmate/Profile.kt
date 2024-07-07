package com.example.meetupmate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class Profile : AppCompatActivity() {

    private val GALLERY_REQUEST_CODE: Int = 100
    private lateinit var imgProfileImage: ShapeableImageView
    private lateinit var imageProfileUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tlProfile: Toolbar = findViewById(R.id.tlProfile)
        setSupportActionBar(tlProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        val imgAddFriend: ShapeableImageView = findViewById(R.id.imgAddFriend)
        val tvProfileName : TextView = findViewById(R.id.tvProfileName)
        imgProfileImage = findViewById(R.id.imgProfileImage)

        var selectedUser: User? = null

        val selectedUsername = intent.getStringExtra("username")

        DatabaseManager.getAllUsers { users ->
            for (user in users) {
                if (user.username == selectedUsername)
                {
                    selectedUser = user
                    selectedUser?.let {

                        Glide.with(imgProfileImage.context) // TODO might have to change to getProfileImage
                            .load(selectedUser!!.profileImage)
                            .placeholder(R.drawable.placeholder_image) // while loading
                            .error(R.drawable.placeholder_image)
                            .into(imgProfileImage)

                        DatabaseManager.isFriend(it) { isFriend ->
                            if (isFriend) {
                                imgAddFriend.setImageResource(R.drawable.remover_friend_icon)
                                imgAddFriend.visibility = VISIBLE

                                imgAddFriend.setOnClickListener {
                                    val toast = Toast(applicationContext)
                                    val view: View = layoutInflater.inflate(R.layout.custom_toast, findViewById(R.id.customToast))
                                    toast.view = view
                                    toast.duration = Toast.LENGTH_SHORT
                                    val customToastText: TextView = view.findViewById(R.id.customToastText)
                                    customToastText.text = "Removed as Friend"
                                    toast.show()

                                    DatabaseManager.removeFriend(selectedUser!!)
                                    imgAddFriend.setImageResource(R.drawable.add_friend_icon)

                                    finish()
//                                    overridePendingTransition(0, 0)
                                    startActivity(intent)
//                                    overridePendingTransition(0, 0)
                                }
                            } else {
                                if (selectedUsername == DatabaseManager.currUser.username) {
                                    imgAddFriend.setImageResource(R.drawable.edit_icon)
                                    imgAddFriend.visibility = VISIBLE

                                    imgAddFriend.setOnClickListener {
                                        val gallery = Intent(Intent.ACTION_PICK)
                                        gallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                        startActivityForResult(gallery, GALLERY_REQUEST_CODE)
                                    }
                                } else {
                                    imgAddFriend.setImageResource(R.drawable.add_friend_icon)
                                    imgAddFriend.visibility = VISIBLE

                                    imgAddFriend.setOnClickListener {

                                        val toast = Toast(applicationContext)
                                        val view: View = layoutInflater.inflate(R.layout.custom_toast, findViewById(R.id.customToast))
                                        toast.view = view
                                        toast.duration = Toast.LENGTH_SHORT
                                        val customToastText: TextView = view.findViewById(R.id.customToastText)
                                        customToastText.text = "Friend Added"
                                        toast.show()

                                        DatabaseManager.addFriend(selectedUser!!)
                                        imgAddFriend.setImageResource(R.drawable.remover_friend_icon)

                                        finish();
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                    }

                    updateRecyclerView(selectedUser!!)
                    break
                }
            }
        }

        tvProfileName.text = selectedUsername
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this@Profile, MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null) {
                    imageProfileUri = data.data!!
                    imgProfileImage.setImageURI(imageProfileUri)
                    DatabaseManager.updateProfileImage(imageProfileUri)

                    Glide.with(imgProfileImage.context)
                        .load(imageProfileUri.toString())
                        .placeholder(R.drawable.placeholder_image) // while loading
                        .error(R.drawable.placeholder_image)
                        .into(imgProfileImage)
                }
            }
        }
    }

    fun updateRecyclerView(selectedUser: User?) {
        val postsToDisplay = mutableListOf<Post>()
        var postsProcessed = 0

        DatabaseManager.getPostsForUser(selectedUser!!.email) { userPosts ->
            postsToDisplay.addAll(userPosts)

            DatabaseManager.getAllPosts { otherPosts ->
                for (post in otherPosts) {
                    DatabaseManager.hasJoinedPost(post) { hasJoined ->
                        postsProcessed++

                        if (hasJoined) {
                            postsToDisplay.add(post)
                        }

                        if (postsProcessed == otherPosts.size) {
                            DatabaseManager.isFriend(selectedUser!!) { isFriend ->
                                if (isFriend || selectedUser!!.email == DatabaseManager.currUser.email) {
                                    runOnUiThread {
                                        val rvProfilePosts: RecyclerView = findViewById(R.id.rvProfilePosts)
                                        rvProfilePosts.layoutManager = LinearLayoutManager(this)
                                        rvProfilePosts.adapter = RecyclerViewPostsAdapter(postsToDisplay, this)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (postsProcessed == 0) {
                DatabaseManager.isFriend(selectedUser!!) { isFriend ->
                    if (isFriend || selectedUser!!.email == DatabaseManager.currUser.email) {
                        val rvProfilePosts: RecyclerView = findViewById(R.id.rvProfilePosts)
                        rvProfilePosts.layoutManager = LinearLayoutManager(this)
                        rvProfilePosts.adapter = RecyclerViewPostsAdapter(postsToDisplay, this)
                    }
                }
            }
        }
    }
}