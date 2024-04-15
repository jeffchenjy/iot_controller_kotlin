package com.example.iot_kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private var colorhandler = Handler(Looper.myLooper()!!)
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
    /* parameter of changeTheme */
    private lateinit var sharedPreferences: SharedPreferences
    private var isNightMode: Boolean? = false
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
        changeTheme()
        setupFirebaseInit()
        TextChangedListener()
        /** TextView Listener **/
        textViewClickListener()
        textViewTouchListener()
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
                                CustomSnackbar.showSnackbar(getView(), requireContext(), getString(R.string.login_failed))
                                //showToast(getString(R.string.login_failed))
                            }
                        }
                } else {
                    login_password.error  = getString(R.string.password_empty)
                }
            } else {
                // 電子郵件無效
                login_email.error = getString(R.string.invalid_email_address)
            }
        }
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
    private fun setupFirebaseInit() {
        auth = FirebaseAuth.getInstance()
        currentActivity = requireActivity() as AppCompatActivity
        currentUser = auth.currentUser
        if (currentUser != null) {
            startMainActivity()
            return
        }
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
    private fun textViewClickListener() {
        signupRedirectText.setOnClickListener(textViewOnClickListener())
        free_login.setOnClickListener(textViewOnClickListener())
        forgotPassword.setOnClickListener(textViewOnClickListener())
    }
    private fun textViewOnClickListener() : View.OnClickListener?  {
        return View.OnClickListener {
            val view = it as? View
            val viewId = view?.id
            when(viewId) {
                R.id.signUpRedirectText -> {
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
                R.id.free_login -> {
                    if(currentUser != null) {
                        auth.signOut()
                    }
                    startMainActivity()
                }
                R.id.forgot_password -> {
                    val builder = AlertDialog.Builder(requireContext())
                    val dialogView: View = layoutInflater.inflate(R.layout.dialog_passwd_forgot, null)
                    val emailBox = dialogView.findViewById<EditText>(R.id.emailBox)
                    builder.setView(dialogView)
                    val dialog: AlertDialog = builder.create()
                    dialogView.findViewById<View>(R.id.btnReset).setOnClickListener(View.OnClickListener {
                        val userEmail = emailBox.text.toString()
                        if (TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail)
                                .matches()
                        ) {
                            CustomSnackbar.showSnackbar(getView(), requireContext(), "Please enter your registered email address")
                            //showToast("Please enter your registered email address")
                            return@OnClickListener
                        }
                        auth.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                CustomSnackbar.showSnackbar(getView(), requireContext(), "Please check your email")
                                //showToast("Please check your email")
                                dialog.dismiss()
                            } else {
                                CustomSnackbar.showSnackbar(getView(), requireContext(), "Unable to send, failed")
                                //showToast("Unable to send, failed")
                            }
                        }
                    })
                    dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
                    if (dialog.window != null) {
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                    }
                    dialog.show()
                }
            }
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun textViewTouchListener() {
        signupRedirectText.setOnTouchListener(textViewOnTouchListener())
        free_login.setOnTouchListener(textViewOnTouchListener())
        forgotPassword.setOnTouchListener(textViewOnTouchListener())
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun textViewOnTouchListener(): View.OnTouchListener? {
        return View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    when(v.id) {
                        R.id.signUpRedirectText -> {
                            signupRedirectText.setTextColor(Color.GRAY)
                        }
                        R.id.free_login -> {
                            free_login.setTextColor(Color.GRAY)
                        }
                        R.id.forgot_password -> {
                            forgotPassword.setTextColor(Color.GRAY)
                        }
                    }
                    // 延遲 2 秒後恢復原本的顏色
                    colorhandler.postDelayed({
                        when(v.id) {
                            R.id.signUpRedirectText -> {
                                signupRedirectText.setTextColor(Color.WHITE)
                            }
                            R.id.free_login -> {
                                free_login.setTextColor(Color.WHITE)
                            }
                            R.id.forgot_password -> {
                                forgotPassword.setTextColor(Color.WHITE)
                            }
                        }
                    }, 600) // 0.6 秒後執行
                }
                MotionEvent.ACTION_UP -> {
                    // 取消延遲執行，防止在 2 秒內放開時顏色恢復的操作執行
                    colorhandler.removeCallbacksAndMessages(null)
                    when(v.id) {
                        R.id.signUpRedirectText -> {
                            signupRedirectText.setTextColor(Color.WHITE)
                        }
                        R.id.free_login -> {
                            free_login.setTextColor(Color.WHITE)
                        }
                        R.id.forgot_password -> {
                            forgotPassword.setTextColor(Color.WHITE)
                        }
                    }
                }
            }
            false
        }
    }
    private fun startMainActivity() {
        Main_intent = Intent(currentActivity, MainActivity::class.java)
        Main_intent.putExtra("checkUserFlag", "1")
        currentActivity.startActivity(Main_intent)
        currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        currentActivity.finish()
    }
    private fun changeTheme() {
        /* use sharedPreferences change themes*/
        sharedPreferences = requireActivity().getSharedPreferences("MODE", Context.MODE_PRIVATE)
        isNightMode = sharedPreferences.getBoolean("nightMode", false)
        isNightMode?.let { isNightMode ->
            if (isNightMode) {
                if(AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            } else {
                if(AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}