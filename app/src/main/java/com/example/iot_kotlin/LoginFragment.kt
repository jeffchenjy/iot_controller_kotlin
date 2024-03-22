package com.example.iot_kotlin

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    /* EditText */
    private lateinit var login_email: EditText
    private lateinit var login_password: EditText
    /* TextInputLayout */
    private lateinit var emailTextInputLayout : TextInputLayout
    private lateinit var passwordTextInputLayout : TextInputLayout
    /* TextView */
    private lateinit var signupRedirectText: TextView
    private lateinit var free_login: TextView
    private lateinit var forgotPassword: TextView
    /* Button */
    private lateinit var loginButton: Button
    /* Intent Activity */
    private lateinit var currentActivity: AppCompatActivity
    private lateinit var Main_intent: Intent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        setupViews()
        TextChangedListener()
        // 確認用戶是否已經登入 Firebase
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startMainActivity()
            return
        }
        /** Button **/
        loginButton.setOnClickListener{
            val email = login_email.text.toString()
            val passwd = login_password.text.toString()
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (passwd.isNotEmpty()) {
                    /** Show progress indicators **/
                    val builder = AlertDialog.Builder(requireContext())
                    val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                    builder.setView(dialogView)
                    val dialog: AlertDialog = builder.create()
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()
                    auth.signInWithEmailAndPassword(email, passwd)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                dialog.dismiss()
                                // 登錄成功
                                startMainActivity()
                            } else {
                                // 登錄失敗
                                dialog.dismiss()
                                showToast(getString(R.string.login_failed))
                            }
                        }
                } else {
                    login_password.error  = "Password can't be empty"
                }
            } else {
                // 電子郵件無效
                login_email.error = "Invalid email address"
            }
        }
        signupRedirectText.setOnClickListener{
            val fragment = SignupFragment()
            currentActivity.supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out_right  // popExit
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        free_login.setOnClickListener {
            if(currentUser != null) {
                auth.signOut()
            }
            startMainActivity()
        }
        forgotPassword.setOnClickListener(View.OnClickListener {
            val builder = AlertDialog.Builder(requireContext())
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
    private fun findView(view: View) {
        login_email = view.findViewById(R.id.login_email)
        login_password = view.findViewById(R.id.login_password)
        loginButton = view.findViewById(R.id.login_button)
        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayout)
        passwordTextInputLayout = view.findViewById(R.id.passwordTextInputLayout)
        signupRedirectText = view.findViewById(R.id.signUpRedirectText)
        free_login = view.findViewById(R.id.free_login)
        forgotPassword = view.findViewById(R.id.forgot_password)
    }
    private fun setupViews() {
        auth = FirebaseAuth.getInstance()
        currentActivity = requireActivity() as AppCompatActivity
    }
    private fun TextChangedListener() {
        /**  Text Changed Listener **/
        passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        login_email.addTextChangedListener(object : TextWatcher {
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
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && charCount > 0) {
                    emailTextInputLayout.error = getString(R.string.errorEmail_message)
                } else {
                    emailTextInputLayout.error  = null
                }
            }
        })
        login_password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                } else {
                    passwordTextInputLayout.error  = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
            }
        })
    }
    private fun startMainActivity() {
        Main_intent = Intent(currentActivity, MainActivity::class.java)
        currentActivity.startActivity(Main_intent)
        currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        currentActivity.finish()
    }
    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}