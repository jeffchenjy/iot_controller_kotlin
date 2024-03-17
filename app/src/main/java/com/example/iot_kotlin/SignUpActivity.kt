package com.example.iot_kotlin

import android.R.attr.name
import android.R.attr.password
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SignUpActivity : AppCompatActivity() {
    /* EditText */
    private lateinit var signup_username: EditText
    private lateinit var signup_email: EditText
    private lateinit var signup_password: EditText
    /* Button */
    private lateinit var signupButton: Button
    /* TextInputLayout */
    private lateinit var emailTextInputLayout : TextInputLayout
    private lateinit var usernameTextInputLayout : TextInputLayout
    private lateinit var passwordTextInputLayout : TextInputLayout
    /* TextView */
    private lateinit var loginRedirectText: TextView
    /* Firebase */
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_signup)
        /** Firebase **/
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users")
        findView()
        /**  Text Changed Listener **/
        passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        signup_email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    emailTextInputLayout.error  = null
                    signup_email.error = "Emaill can't be empty"
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && charCount > 0) {
                    emailTextInputLayout.error = getString(R.string.errorEmail_message)
                } else {
                    emailTextInputLayout.error  = null
                }
            }
        })
        signup_username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    usernameTextInputLayout.error  = null
                    signup_username.error = "Username can't be empty"
                } else if(charCount in 1..2) {
                    usernameTextInputLayout.error  = getString(R.string.errorUsername_message)
                } else {
                    usernameTextInputLayout.error  = null
                }
            }
        })
        signup_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    passwordTextInputLayout.error = null
                    signup_password.error  = "Password can't be empty"
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                } else if(charCount in 1..7) {
                    passwordTextInputLayout.error = getString(R.string.errorPassword_message)
                } else {
                    passwordTextInputLayout.error  = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
            }
        })
        /** Button **/
        signupButton.setOnClickListener(View.OnClickListener {
            val username = signup_username.text.toString()
            val email = signup_email.text.toString()
            val passwd = signup_password.text.toString()
            if(Patterns.EMAIL_ADDRESS.matcher(email).matches() && username.length >= 3 && passwd.length >= 8) {
                /** Show progress indicators **/
                val builder = AlertDialog.Builder(this@SignUpActivity)
                val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                builder.setView(dialogView)
                val dialog: AlertDialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                /** Authentication **/
                auth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 註冊成功
                        val currentUser = auth.currentUser
                        val helperClass = HelperClass(username, email, passwd)
                        if (currentUser != null) {
                            val uid = currentUser.uid
                            reference.child("UID").child(uid).setValue(helperClass)
                                .addOnSuccessListener {
                                    dialog.dismiss()
                                    val Login_intent = Intent()
                                    Login_intent.setClass(this@SignUpActivity, LoginActivity::class.java)
                                    startActivity(Login_intent)
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                                    finish()
                                    showToast(getString(R.string.signUp_success))
                                    Log.d("Database entry created successfully", "Database entry created successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.d("Failed to reference create", "${e.message}")
                                }
                        } else {
                            dialog.dismiss()
                            showToast("No user is currently in use")
                        }
                    } else {
                        dialog.dismiss()
                        showToast(getString(R.string.signUp_failed))
                    }
                }
            }
        })
        loginRedirectText.setOnClickListener {
            val Login_intent = Intent()
            Login_intent.setClass(this@SignUpActivity, LoginActivity::class.java)
            startActivity(Login_intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }

    }
    private fun findView() {
        signup_username = findViewById(R.id.signup_username)
        signup_email = findViewById(R.id.signup_email)
        signup_password = findViewById(R.id.signup_password)
        signupButton = findViewById(R.id.signup_button)
        emailTextInputLayout = findViewById(R.id.emailTextInputLayout)
        usernameTextInputLayout = findViewById(R.id.usernameTextInputLayout)
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout)
        loginRedirectText = findViewById(R.id.loginRedirectText)
    }
    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}