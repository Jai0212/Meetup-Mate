package com.example.meetupmate

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract.Data
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvSignupOnLoginPage: TextView = findViewById(R.id.tvSignupOnLoginPage)
        val spannableString = SpannableString(tvSignupOnLoginPage.text)

        val clickableSpan = object : ClickableSpan() {
             override fun onClick(widget: View) {
                 startActivity(Intent(this@Login, Signup::class.java))
            }
        }

        val start = tvSignupOnLoginPage.text.indexOf("Click Here!")
        val end = start + "Click Here!".length
        spannableString[start..end] = clickableSpan

        tvSignupOnLoginPage.text = spannableString
        tvSignupOnLoginPage.movementMethod = LinkMovementMethod.getInstance()

        val edtEMail: EditText = findViewById(R.id.edtEMail)
        val edtPassword: EditText = findViewById(R.id.edtPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)

        // Login button clicked
        btnLogin.setOnClickListener {
            if (edtEMail.text.toString().trim() == "") {
                showInvalidDetailsToast()
            }
            DatabaseManager.userExists(edtEMail.text.toString().trim().lowercase()) { exists ->
                if (exists) {
                    DatabaseManager.getUser(edtEMail.text.toString().trim().lowercase()) { user ->
                        if (user != null) {
                            if (user.password == edtPassword.text.toString() && edtEMail.text.toString() != "")
                            {
                                DatabaseManager.initCurrUser(user)

                                val pref = getSharedPreferences("isLoggedIn", MODE_PRIVATE)
                                val editor: SharedPreferences.Editor = pref.edit()
                                editor.putBoolean("flag", true)
                                editor.putString("email", user.email)
                                editor.apply()

                                val intent = Intent(this@Login, MainActivity::class.java)
                                // removes login from backstack
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            else {
                                showInvalidDetailsToast()
                            }
                        }
                        else {
                            showInvalidDetailsToast()
                        }
                    }
                }
                else {
                    showInvalidDetailsToast()
                }
            }
        }
    }

    private fun showInvalidDetailsToast() {
        val toast = Toast(applicationContext)
        val view: View = layoutInflater.inflate(R.layout.custom_toast, findViewById(R.id.customToast))
        toast.view = view
        toast.duration = Toast.LENGTH_SHORT
        val customToastText: TextView = view.findViewById(R.id.customToastText)
        customToastText.text = "Invalid Details"
        toast.show()
    }
}