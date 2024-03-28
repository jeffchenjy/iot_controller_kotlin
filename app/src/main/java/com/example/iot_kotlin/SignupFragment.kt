package com.example.iot_kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupFragment: Fragment() {
    private var colorhandler = Handler(Looper.myLooper()!!)
    /* EditText */
    private lateinit var signup_username: EditText
    private lateinit var signup_email: EditText
    private lateinit var signup_nickname: EditText
    private lateinit var signup_password: EditText
    /* Button */
    private lateinit var signupButton: Button
    /* TextInputLayout */
    private lateinit var emailTextInputLayout : TextInputLayout
    private lateinit var usernameTextInputLayout : TextInputLayout
    private lateinit var nicknameTextInputLayout : TextInputLayout
    private lateinit var passwordTextInputLayout : TextInputLayout
    /* TextView */
    private lateinit var loginRedirectText: TextView
    /* Firebase */
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var currentActivity: AppCompatActivity
    private lateinit var Main_intent: Intent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        currentActivity = requireActivity() as AppCompatActivity
        /** Firebase **/
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users")
        TextChangedListener()
        textViewListener()
        /** Button **/
        signupButton.setOnClickListener(View.OnClickListener {
            val username = signup_username.text.toString()
            val nickname = signup_nickname.text.toString()
            val email = signup_email.text.toString()
            val passwd = signup_password.text.toString()
            if(Patterns.EMAIL_ADDRESS.matcher(email).matches() && username.length >= 3 && nickname.length>= 3 && passwd.length >= 8) {
                /** Show progress indicators **/
                val builder = AlertDialog.Builder(requireContext())
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
                        val initValue = resources.getString(R.string.unselect)
                        val helperClass = HelperClass(username, nickname, email, passwd, initValue, initValue, initValue)
                        if (currentUser != null) {
                            val uid = currentUser.uid
                            reference.child("UID").child(uid).setValue(helperClass)
                                .addOnSuccessListener {
                                    dialog.dismiss()
                                    showToast(getString(R.string.signUp_success))
                                    startMainActivity()
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
    }
    private fun findView(view: View) {
        signup_username = view.findViewById(R.id.signup_username)
        signup_nickname = view.findViewById(R.id.signup_nickname)
        signup_email = view.findViewById(R.id.signup_email)
        signup_password = view.findViewById(R.id.signup_password)
        signupButton = view.findViewById(R.id.signup_button)
        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayout)
        usernameTextInputLayout = view.findViewById(R.id.usernameTextInputLayout)
        nicknameTextInputLayout = view.findViewById(R.id.nicknameTextInputLayout)
        passwordTextInputLayout = view.findViewById(R.id.passwordTextInputLayout)
        loginRedirectText = view.findViewById(R.id.loginRedirectText)
    }
    private fun TextChangedListener() {
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
                    signup_email.error = getString(R.string.email_empty)
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
                    signup_username.error = getString(R.string.username_empty)
                } else if(charCount in 1..2) {
                    usernameTextInputLayout.error  = getString(R.string.errorUsername_message)
                } else {
                    usernameTextInputLayout.error  = null
                }
            }
        })
        signup_nickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    nicknameTextInputLayout.error  = null
                    signup_nickname.error = getString(R.string.nickname_empty)
                } else if(charCount in 1..2) {
                    nicknameTextInputLayout.error  = getString(R.string.errorUsername_message)
                } else {
                    nicknameTextInputLayout.error  = null
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
                    signup_password.error  = getString(R.string.password_empty)
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                } else if(charCount in 1..7) {
                    passwordTextInputLayout.error = getString(R.string.errorPassword_message)
                } else {
                    passwordTextInputLayout.error  = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
            }
        })
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun textViewListener() {
        loginRedirectText.setOnClickListener {
            ReturnLoginFragment()
        }
        loginRedirectText.setOnTouchListener{ _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下時的處理
                    loginRedirectText.setTextColor(Color.GRAY)
                    colorhandler.postDelayed({
                        loginRedirectText.setTextColor(Color.WHITE)
                    }, 800)
                }
                MotionEvent.ACTION_UP -> {
                    // 放開時的處理
                    colorhandler.removeCallbacksAndMessages(null)
                    loginRedirectText.setTextColor(Color.WHITE) // 恢復文字原本的顏色
                }
            }
            false
        }
    }
    private fun ReturnLoginFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentManager.popBackStack()
        fragmentTransaction.commit()
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