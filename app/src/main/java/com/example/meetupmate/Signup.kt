package com.example.meetupmate

import android.content.Intent
import android.os.Bundle
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

class Signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvLoginOnSignUpPage: TextView = findViewById(R.id.tvLoginOnSignUpPage)
        val spannableString = SpannableString(tvLoginOnSignUpPage.text)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@Signup, Login::class.java))
            }
        }

        val start = tvLoginOnSignUpPage.text.indexOf("Click Here!")
        val end = start + "Click Here!".length
        spannableString[start..end] = clickableSpan

        tvLoginOnSignUpPage.text = spannableString
        tvLoginOnSignUpPage.movementMethod = LinkMovementMethod.getInstance()

        val edtUsername: EditText = findViewById(R.id.edtUsername)
        val edtEMail: EditText = findViewById(R.id.edtEMail)
        val edtPassword: EditText = findViewById(R.id.edtPassword)

        // Sign Up button clicked
        val btnSignUp: Button = findViewById(R.id.btnSignUp)
        btnSignUp.setOnClickListener {

            if (edtUsername.text.toString().trim() != "" && edtEMail.text.toString().trim() != "" &&
                edtPassword.text.toString().trim() != "") {

                if (isValidEmail(edtEMail.text.toString().trim())) {

                    DatabaseManager.userExists(edtEMail.text.toString().trim()) { userExists->
                        if (userExists) {
                            showToast("E-mail Already In Use")
                            return@userExists
                        } else {
                            val newUser = User(
                                edtEMail.text.toString().trim(),
                                edtUsername.text.toString().trim(),
                                edtPassword.text.toString().trim(),
                                "")

                            DatabaseManager.addNewUser(newUser, this)
                            DatabaseManager.initCurrUser(newUser)

                            startActivity(Intent(this@Signup, MainActivity::class.java))
                        }
                    }
                } else {
                    showToast("Invalid E-mail")
                }
            } else {
                showToast("Fill Up All Details")
            }
        }
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        val regexValid = email.matches(emailRegex.toRegex())
        val androidValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        return regexValid and androidValid
    }

    fun showToast(toastText: String) {
        val toast = Toast(applicationContext)
        val view: View = layoutInflater.inflate(R.layout.custom_toast, findViewById(R.id.customToast))
        toast.view = view
        toast.duration = Toast.LENGTH_SHORT
        val customToastText: TextView = view.findViewById(R.id.customToastText)
        customToastText.text = toastText
        toast.show()
    }
}