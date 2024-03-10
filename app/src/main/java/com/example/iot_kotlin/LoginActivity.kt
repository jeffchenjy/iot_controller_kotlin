package com.example.iot_kotlin

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity()  {

    private lateinit var auth: FirebaseAuth
    /* EditText */
    private lateinit var login_email: EditText
    private lateinit var login_password: EditText
    /* TextView */
    private lateinit var signupRedirectText: TextView
    private lateinit var free_login: TextView
    private lateinit var forgotPassword: TextView
    /* Button */
    private lateinit var loginButton: Button

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        findView()
        val currentUser = auth.currentUser
        if(currentUser != null) {
            val Main_intent = Intent()
            Main_intent.setClass(this@LoginActivity, MainActivity::class.java)
            startActivity(Main_intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
        loginButton.setOnClickListener(View.OnClickListener {
            val email = login_email.text.toString()
            val passwd = login_password.text.toString()
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (passwd.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, passwd)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // 登錄成功
                                val Main_intent = Intent()
                                Main_intent.setClass(this@LoginActivity, MainActivity::class.java)
                                startActivity(Main_intent)
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                finish()
                            } else {
                                // 登錄失敗
                                showToast(getString(R.string.login_failed))
                            }
                        }
                } else {
                    // 密碼為空
                    login_password.error = "Password can't be empty"
                }
            } else {
                // 電子郵件無效
                login_email.error = "Invalid email address"
            }
        })
        signupRedirectText.setOnClickListener{
            val Login_intent = Intent()
            Login_intent.setClass(this@LoginActivity, SignUpActivity::class.java)
            startActivity(Login_intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
        free_login.setOnClickListener {
            val currentUser = auth.currentUser
            if(currentUser != null) {
                auth.signOut()
            }
            val Main_intent = Intent()
            Main_intent.setClass(this@LoginActivity, MainActivity::class.java)
            startActivity(Main_intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        forgotPassword.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(this@LoginActivity)
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val emailBox = dialogView.findViewById<EditText>(R.id.emailBox)
            builder.setView(dialogView)
            val dialog: AlertDialog = builder.create()
            dialogView.findViewById<View>(R.id.btnReset).setOnClickListener(View.OnClickListener {
                val userEmail = emailBox.text.toString()
                if (TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail)
                        .matches()
                ) {
                    showToast("Please enter your registered email address")
                    return@OnClickListener
                }
                auth.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showToast("Please check your email")
                        dialog.dismiss()
                    } else {
                        showToast("Unable to send, failed")
                    }
                }
            })
            dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        })

    }
    private fun findView() {
        login_email = findViewById(R.id.login_email)
        login_password = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupRedirectText = findViewById(R.id.signUpRedirectText)
        free_login = findViewById(R.id.free_login)
        forgotPassword = findViewById(R.id.forgot_password)
    }
    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}