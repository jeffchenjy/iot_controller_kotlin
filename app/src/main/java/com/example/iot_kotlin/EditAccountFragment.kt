package com.example.iot_kotlin

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
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditAccountFragment : Fragment() {
    /*  About ToolBar */
    private lateinit var toolbar: Toolbar
    /* EditText */
    private lateinit var editUsername: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    /* TextInputLayout */
    private lateinit var emailTextInputLayout : TextInputLayout
    private lateinit var usernameTextInputLayout : TextInputLayout
    private lateinit var passwordTextInputLayout : TextInputLayout
    /* Button */
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var savebuttonCardView: CardView
    /* String */
    private lateinit var newUsername: String
    private lateinit var newEmail: String
    private lateinit var newPassword: String
    private lateinit var currentUserPassword: String
    private lateinit var currentUserName: String
    private lateinit var currentUserEmail: String
    /* Firebase */
    private var currentUserUID: String? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    /* checkEditText Loop */
    private val handler = Handler(Looper.myLooper()!!)
    private val checkEditTexts = Runnable {
        if((editEmail.text.toString() == currentUserEmail || editEmail.text.isEmpty()) &&
            (editUsername.text.toString() == currentUserName || editUsername.text.isEmpty())
            && editPassword.text.isEmpty()) {
            saveButton.isEnabled = false
            savebuttonCardView.visibility = View.VISIBLE
        } else {
            saveButton.isEnabled = true
            savebuttonCardView.visibility = View.GONE
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_account, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        setToolbar()
        textChangedListener()
        firebaseInit()
        /**  Button Click Listener **/
        buttonClickListener()
    }
    private fun findView(view: View){
        toolbar = view.findViewById(R.id.toolbar)
        editUsername = view.findViewById(R.id.editUsername)
        editEmail = view.findViewById(R.id.editEmail)
        editPassword = view.findViewById(R.id.editPassword)
        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayout)
        usernameTextInputLayout = view.findViewById(R.id.usernameTextInputLayout)
        passwordTextInputLayout = view.findViewById(R.id.passwordTextInputLayout)
        saveButton = view.findViewById(R.id.saveButton)
        deleteButton = view.findViewById(R.id.deleteButton)
        savebuttonCardView = view.findViewById(R.id.savebuttonCardView)
    }
    private fun setToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.edit_account)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            returnProfileFragment()
        }
    }
    private fun firebaseInit() {
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        /** Show progress indicators **/
        val builder = AlertDialog.Builder(requireContext())
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
        builder.setView(dialogView)
        val dialog: AlertDialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        /** 抓取 Firebase 的資料 */
        if (currentUser != null) {
            currentUserUID = currentUser!!.uid
            reference = FirebaseDatabase.getInstance().getReference("users/UID/$currentUserUID")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        dialog.dismiss()
                        val username = snapshot.child("username").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)
                        val password = snapshot.child("password").getValue(String::class.java)
                        currentUserEmail = email!!
                        currentUserName = username!!
                        currentUserPassword = password!!
                        editUsername.setText(username)
                        editEmail.setText(email)
                        //editPassword.setText(password)
                        savebuttonCardView.visibility = View.VISIBLE
                        saveButton.isEnabled = false
                    } else {
                        // 處理資料不存在的情況
                        dialog.dismiss()
                        showToast("User data not found")
                        currentUserEmail = " "
                        currentUserName = " "
                        currentUserPassword = " "
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // 處理讀取資料失敗的情況
                    dialog.dismiss()
                    showToast("Error: ${databaseError.message}")
                }
            })
        }
    }
    private fun textChangedListener() {
        /**  Text Changed Listener **/
        passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
        editEmail.addTextChangedListener(object : TextWatcher {
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
                    editEmail.error = getString(R.string.email_empty)
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && charCount > 0) {
                    emailTextInputLayout.error = getString(R.string.errorEmail_message)
                } else if(email == (currentUserEmail)) {
                    emailTextInputLayout.error = null
                } else {
                    emailTextInputLayout.error  = null
                }
                handler.post(checkEditTexts)
            }
        })
        editUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val username = s.toString()
                if (s.isNullOrEmpty()) {
                    usernameTextInputLayout.error  = null
                    editUsername.error = getString(R.string.username_empty)
                } else if(charCount in 1..2) {
                    usernameTextInputLayout.error  = getString(R.string.errorUsername_message)
                } else if(username == currentUserName) {
                    usernameTextInputLayout.error  = null
                } else {
                    usernameTextInputLayout.error  = null
                }
                handler.post(checkEditTexts)
            }
        })
        editPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                if (s.isNullOrEmpty()) {
                    passwordTextInputLayout.error = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                } else if(charCount in 1..7) {
                    passwordTextInputLayout.error = getString(R.string.errorPassword_message)
                } else {
                    passwordTextInputLayout.error  = null
                    passwordTextInputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
                handler.post(checkEditTexts)
            }
        })
    }
    fun containsSpecialCharacter(input: String): Boolean {
        val regex = Regex("[^A-Za-z0-9 ]") // 匹配除了字母、數字和空格之外的所有字符
        return regex.containsMatchIn(input)
    }
    private fun buttonClickListener() {
        saveButton.setOnClickListener(buttonOnClickListener())
        deleteButton.setOnClickListener(buttonOnClickListener())
    }
    private fun buttonOnClickListener() : View.OnClickListener? {
        return View.OnClickListener {
            val view = it as? View
            val viewId = view?.id
            when(viewId) {
                R.id.saveButton -> {
                    handler.removeCallbacksAndMessages(null)
                    if (currentUser != null) {
                        /** Update Realtime Database **/
                        // 獲取用戶輸入的資料
                        newUsername = editUsername.text.toString()
                        newEmail = editEmail.text.toString()
                        newPassword = if(editPassword.text.isEmpty()) {
                            currentUserPassword
                        } else {
                            editPassword.text.toString()
                        }
                        if(Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() && newUsername.length >= 3 && newPassword.length >= 8) {
                            /** Check Password **/
                            val builder = AlertDialog.Builder(requireContext())
                            val dialogView: View = layoutInflater.inflate(R.layout.dialog_passwd_verify, null)
                            val codeBox = dialogView.findViewById<EditText>(R.id.codeBox)
                            builder.setView(dialogView)
                            val dialog: AlertDialog = builder.create()
                            dialogView.findViewById<View>(R.id.btnCheck).setOnClickListener(View.OnClickListener {
                                val userCode = codeBox.text.toString()
                                if (userCode.equals(currentUserPassword)) {
                                    dialog.dismiss()
                                    /** Show progress indicators **/
                                    val builder = AlertDialog.Builder(requireContext())
                                    val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                                    builder.setView(dialogView)
                                    val dialog: AlertDialog = builder.create()
                                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                                    dialog.show()
                                    // 準備更新資料庫中的資料
                                    val newData = mapOf<String, Any>(
                                        "username" to newUsername,
                                        "email" to newEmail,
                                        "password" to newPassword
                                    )
                                    reference.updateChildren(newData)
                                        .addOnSuccessListener {
                                            showToast(getString(R.string.data_update_success))
                                        }
                                        .addOnFailureListener { _ ->
                                            showToast(getString(R.string.data_update_failed))
                                        }
                                    currentUser?.let {
                                        if(newEmail != currentUser!!.email){
                                            // 要求用戶重新驗證身份
                                            val credential = EmailAuthProvider.getCredential(currentUser!!.email!!, currentUserPassword)
                                            currentUser!!.reauthenticate(credential)
                                                .addOnCompleteListener { reauthTask ->
                                                    if (reauthTask.isSuccessful) {
                                                        // 重新驗證成功，更新電子郵件地址
                                                        currentUser!!.verifyBeforeUpdateEmail(newEmail)
                                                            .addOnCompleteListener { updateEmailTask ->
                                                                if (updateEmailTask.isSuccessful) {
                                                                    // 電子郵件地址更新成功
                                                                    showToast("Please check your email")
                                                                }
                                                            }
                                                    }
                                                }
                                        }
                                        // 更新密碼
                                        it.updatePassword(newPassword)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {    // 更新成功
                                                    showToast(getString(R.string.Password_update_message))
                                                } else {                    // 更新失敗，顯示錯誤消息
                                                    showToast(getString(R.string.Password_update_failed))
                                                    //Log.d("updatePassword", "Error updating password: ${task.exception?.message}")
                                                }
                                            }
                                        Handler(Looper.myLooper()!!).postDelayed({
                                            dialog.dismiss()
                                            returnProfileFragment()
                                        }, 1500)
                                    }
                                } else {
                                    showToast(getString(R.string.password_error))
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
                R.id.deleteButton -> {
                    handler.removeCallbacksAndMessages(null)
                    MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
                        .setIcon(R.drawable.ic_caution)
                        .setTitle(resources.getString(R.string.delete))
                        .setMessage(resources.getString(R.string.user_account_delete))
                        .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                            dialog.dismiss()
                            /** Check Password **/
                            val builder = AlertDialog.Builder(requireContext())
                            val dialogView: View = layoutInflater.inflate(R.layout.dialog_passwd_verify, null)
                            val codeBox = dialogView.findViewById<EditText>(R.id.codeBox)
                            builder.setView(dialogView)
                            val dialog: AlertDialog = builder.create()
                            dialogView.findViewById<View>(R.id.btnCheck).setOnClickListener(View.OnClickListener {
                                val userCode = codeBox.text.toString()
                                if (userCode.equals(currentUserPassword)) {
                                    dialog.dismiss()
                                    currentUser?.let { user ->
                                        reference = FirebaseDatabase.getInstance().getReference("users/UID/$currentUserUID")
                                        reference.removeValue()
                                            .addOnSuccessListener {
                                                // 刪除成功
                                                Log.d("User data deleted successfully", "User data deleted successfully")
                                            }
                                            .addOnFailureListener { exception ->
                                                // 刪除失敗，顯示錯誤消息
                                                Log.d("Error deleting user data", "${exception.message}")
                                            }
                                        val credential = EmailAuthProvider.getCredential(currentUser!!.email!!, currentUserPassword)
                                        user?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                                            if (reauthTask.isSuccessful) {
                                                user.delete().addOnCompleteListener { deleteTask ->
                                                    if (deleteTask.isSuccessful) {
                                                        if(currentUser != null){
                                                            auth.signOut()
                                                        }
                                                        val currentActivity = requireActivity()
                                                        val loginIntent = Intent(currentActivity, StartLogActivity::class.java)
                                                        loginIntent.putExtra("fragmentShow", "LoginFragment")
                                                        currentActivity.startActivity(loginIntent)
                                                        currentActivity.overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out)
                                                        currentActivity.finish()
                                                    } else {
                                                        Log.d("Error deleting user account:","${deleteTask.exception?.message}")
                                                    }
                                                }
                                            } else {
                                                Log.d("Error reauthenticating user:","${reauthTask.exception?.message}")
                                            }
                                        }
                                    }
                                } else {
                                    showToast(getString(R.string.password_error))
                                }
                            })
                            dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
                            if (dialog.window != null) {
                                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                            }
                            dialog.show()
                        }
                        .setNegativeButton(resources.getString(R.string.cancel)){ dialog, _ ->
                            // Respond to positive button press
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }
    private fun returnProfileFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentManager.popBackStack()
        fragmentTransaction.commit()
    }
    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}