package com.example.meetupmate

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val GALLERY_REQUEST_CODE: Int = 100
    private lateinit var addPostImage: ImageView
    private lateinit var imageUri: Uri

    @SuppressLint("UseSupportActionBar")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imgSearchIcon: ImageView = findViewById(R.id.imgSearchIcon)

        imgSearchIcon.setOnClickListener {
            startActivity(Intent(this@MainActivity, Search::class.java))
        }

        val toolbar: Toolbar = findViewById(R.id.tlToolbar)
        setActionBar(toolbar)

        val imgProfileIcon: ImageView = findViewById(R.id.imgProfileIcon)

//        imgProfileIcon.setImageURI(Uri.parse(DatabaseManager.currUser.profileImage))
//        DatabaseManager.loadImageIntoImageView(DatabaseManager.currUser.profileImage, imgProfileIcon)
        DatabaseManager.getProfileImage {
            if (it != null) {
                DatabaseManager.loadImageIntoImageView(it, imgProfileIcon)
            }
        }

        val imgLogo: ImageView = findViewById(R.id.imgLogo)
        imgLogo.setOnClickListener {
            startActivity(Intent(this@MainActivity, MainActivity::class.java))
        }

        imgProfileIcon.setOnClickListener { view ->
            showProfileMenu(view)
        }
    }

    private fun showProfileMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.profile_menu, popup.menu)
        popup.setOnMenuItemClickListener { item -> handleMenuItemClick(item) }
        popup.show()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mViewProfile -> {
                val intent = Intent(this@MainActivity, Profile::class.java)
                intent.putExtra("username", DatabaseManager.currUser.username)
                startActivity(intent)
                true
            }
            R.id.mAddMeetup -> {

                val dialog = Dialog(this@MainActivity)
                dialog.setContentView(R.layout.add_post)

                addPostImage = dialog.findViewById(R.id.addPostImage)
                val addPostTitle: EditText = dialog.findViewById(R.id.addPostTitle)
                val addPostDate: EditText = dialog.findViewById(R.id.addPostDate)
                val addPostTime: EditText = dialog.findViewById(R.id.addPostTime)
                val addPostDescription: EditText = dialog.findViewById(R.id.addPostDescription)
                val addPostCancelBTN: Button = dialog.findViewById(R.id.addPostCancelBTN)
                val addPostAddBTN: Button = dialog.findViewById(R.id.addPostAddBTN)

                addPostImage.setOnClickListener {
                    val gallery = Intent(Intent.ACTION_PICK)
                    gallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(gallery, GALLERY_REQUEST_CODE)
                }

                addPostAddBTN.setOnClickListener {
                    if (addPostTitle.text.toString() != "" && addPostDate.text.toString() != "" &&
                        addPostTime.text.toString() != "" && addPostDescription.text.toString() != "" &&
                        addPostImage.drawable != resources.getDrawable(R.drawable.upload_area)) {

                        DatabaseManager.addPost(
                            imageUri,
                            addPostTitle.text.toString(),
                            addPostDate.text.toString(),
                            addPostTime.text.toString(),
                            addPostDescription.text.toString())

                        dialog.dismiss()

                        val toast = Toast(applicationContext)
                        val view: View = layoutInflater.inflate(R.layout.custom_toast, findViewById(R.id.customToast))
                        toast.view = view
                        toast.duration = Toast.LENGTH_SHORT
                        val customToastText: TextView = view.findViewById(R.id.customToastText)
                        customToastText.text = "Post Added"
                        toast.show()
                    }
                    else {
                        val toast = Toast(applicationContext)
                        val view: View = layoutInflater.inflate(R.layout.custom_toast, findViewById(R.id.customToast))
                        toast.view = view
                        toast.duration = Toast.LENGTH_SHORT
                        val customToastText: TextView = view.findViewById(R.id.customToastText)
                        customToastText.text = "Fill Up All Details"
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                    }
                }

                addPostCancelBTN.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
                true
            }
            R.id.mLogout -> {
                DatabaseManager.initCurrUser(User(email = "", username = "", password = "", profileImage = ""))
                startActivity(Intent(this@MainActivity, Login::class.java))
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                if (data != null) {
                    imageUri = data.data!!
                    addPostImage.setImageURI(imageUri)
                }
            }
        }
    }
}