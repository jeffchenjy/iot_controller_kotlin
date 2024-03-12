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
import android.text.TextUtils
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
    /* TextView */
    private lateinit var errorEmailTextView: TextView
    private lateinit var errorUsernameTextView: TextView
    private lateinit var errorPasswordTextView: TextView
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

        signupButton.setOnClickListener(View.OnClickListener {
            val username = signup_username.text.toString()
            val email = signup_email.text.toString()
            val passwd = signup_password.text.toString()
            if (username.isEmpty()) {
                signup_username.error = "Username can't be empty"
            }
            if (email.isEmpty()) {
                signup_email.error = "Emaill can't be empty"
            }
            if (passwd.isEmpty()) {
                signup_password.error = "Password can't be empty"
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorEmailTextView.text = getString(R.string.errorEmail_message)
                errorEmailTextView.visibility = View.VISIBLE
            } else errorEmailTextView.visibility = View.GONE

            if(username.length < 3) {
                errorUsernameTextView.text  = getString(R.string.errorUsername_message)
                errorUsernameTextView.visibility = View.VISIBLE
            } else errorUsernameTextView.visibility = View.GONE

            if(passwd.length < 8) {
                errorPasswordTextView.text = getString(R.string.errorPassword_message)
                errorPasswordTextView.visibility = View.VISIBLE
            } else errorPasswordTextView.visibility = View.GONE

            if(Patterns.EMAIL_ADDRESS.matcher(email).matches() && username.length >= 3 && passwd.length >= 8) {
                errorEmailTextView.visibility = View.GONE
                errorUsernameTextView.visibility = View.GONE
                errorPasswordTextView.visibility = View.GONE
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
                            showToast("Current user is null")
                        }
                    } else {
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
        errorEmailTextView = findViewById(R.id.errorEmailTextView)
        errorUsernameTextView = findViewById(R.id.errorUsernameTextView)
        errorPasswordTextView = findViewById(R.id.errorPasswordTextView)
        loginRedirectText = findViewById(R.id.loginRedirectText)
    }
    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}