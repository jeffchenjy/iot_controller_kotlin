package com.example.iot_kotlin

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfileActivity : AppCompatActivity() {
    private lateinit var reference: DatabaseReference
    /* EditText */
    private lateinit var editUsername: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    /* Button */
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    /* TextView */
    private lateinit var errorEmailTextView: TextView
    private lateinit var errorUsernameTextView: TextView
    private lateinit var errorPasswordTextView: TextView
    /* String */
    private lateinit var newUsername: String
    private lateinit var newEmail: String
    private lateinit var newPassword: String
    private lateinit var currentUserPassword: String
    private var currentUserUID: String? = null
    /*  About ToolBar */
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(bundle : Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_edit_profile)
        findView()
        setToolbar()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        /** 抓取 Firebase 的資料 */
        if (currentUser != null) {
            currentUserUID = currentUser.uid
            reference = FirebaseDatabase.getInstance().getReference("users/UID/$currentUserUID")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)
                        val password = snapshot.child("password").getValue(String::class.java)
                        currentUserPassword = password!!
                        editUsername.setText(username)
                        editEmail.setText(email)
                        //editPassword.setText(password)
                    } else {
                        // 處理資料不存在的情況
                        showToast("User data not found")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // 處理讀取資料失敗的情況
                    showToast("Error: ${databaseError.message}")
                }
            })
        }
        /**  Button Click Listener **/
        saveButton!!.setOnClickListener {
            if (currentUser != null) {
                /** Update Realtime Database **/
                // 獲取用戶輸入的資料
                newUsername = editUsername.text.toString()
                newEmail = editEmail.text.toString()
                newPassword = if(editPassword.text.toString() == "") {
                    currentUserPassword
                } else {
                    editPassword.text.toString()
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    errorEmailTextView.text = getString(R.string.errorEmail_message)
                    errorEmailTextView.visibility = View.VISIBLE
                } else errorEmailTextView.visibility = View.GONE

                if(newUsername.length < 3) {
                    errorUsernameTextView.text  = getString(R.string.errorUsername_message)
                    errorUsernameTextView.visibility = View.VISIBLE
                } else errorUsernameTextView.visibility = View.GONE

                if(newPassword.length < 8) {
                    errorPasswordTextView.text = getString(R.string.errorPassword_message)
                    errorPasswordTextView.visibility = View.VISIBLE
                } else errorPasswordTextView.visibility = View.GONE

                if(Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() && newUsername.length >= 3 && newPassword.length >= 8) {
                    errorEmailTextView.visibility = View.GONE
                    errorUsernameTextView.visibility = View.GONE
                    errorPasswordTextView.visibility = View.GONE
                    // 更新資料庫中的資料
                    val newData = mapOf<String, Any>(
                        "username" to newUsername,
                        "email" to newEmail,
                        "password" to newPassword
                    )
                    reference.updateChildren(newData)
                        .addOnSuccessListener {
                            //showToast("Data updated successfully")
                        }
                        .addOnFailureListener { e ->
                            showToast("Error updating data: ${e.message}")
                        }
                    currentUser?.let {
                        if(!newEmail.equals(currentUser.email)){
                            // 要求用戶重新驗證身份
                            val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentUserPassword)
                            currentUser.reauthenticate(credential)
                                .addOnCompleteListener { reauthTask ->
                                    if (reauthTask.isSuccessful) {
                                        // 重新驗證成功，更新電子郵件地址
                                        currentUser.verifyBeforeUpdateEmail(newEmail)
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
                                if (task.isSuccessful) {
                                    // 更新成功
                                    showToast("Password updated successfully")
                                } else {
                                    // 更新失敗，顯示錯誤消息
                                    Log.d("updatePassword", "Error updating password: ${task.exception?.message}")
                                }
                            }
                    }
                    val builder = AlertDialog.Builder(this@EditProfileActivity)
                    val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                    builder.setView(dialogView)
                    val dialog: AlertDialog = builder.create()
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()
                    Handler(Looper.myLooper()!!).postDelayed({
                        dialog.dismiss()
                        showToast("Data updated successfully")
                        val Profile_intent = Intent()
                        Profile_intent.setClass(this@EditProfileActivity, MainActivity::class.java)
                        Profile_intent.putExtra("fragmentToShow", "ProfileFragment")
                        startActivity(Profile_intent)
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        finish()
                    }, 1000)

                }
            }
        }
        deleteButton!!.setOnClickListener {
            MaterialAlertDialogBuilder(this,  R.style.CustomDialogTheme)
                .setIcon(R.drawable.ic_caution)
                .setTitle(resources.getString(R.string.delete))
                .setMessage(resources.getString(R.string.user_account_delete))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
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
                        val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentUserPassword)
                        user?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                user.delete().addOnCompleteListener { deleteTask ->
                                    if (deleteTask.isSuccessful) {
                                        if(currentUser != null){
                                            auth.signOut()
                                        }
                                        val Login_intent = Intent()
                                        Login_intent.setClass(this@EditProfileActivity, LoginActivity::class.java)
                                        startActivity(Login_intent)
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                                        finish()
                                    } else {
                                        Log.d("Error deleting user account:","${deleteTask.exception?.message}")
                                    }
                                }
                            } else {
                                Log.d("Error reauthenticating user:","${reauthTask.exception?.message}")
                            }
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(resources.getString(R.string.cancel)){ dialog, _ ->
                    // Respond to positive button press
                    dialog.dismiss()
                }
                .show()
        }
    }
    private fun findView(){
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        drawerLayout = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        editUsername = findViewById(R.id.editUsername)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        errorEmailTextView = findViewById(R.id.errorEmailTextView)
        errorUsernameTextView = findViewById(R.id.errorUsernameTextView)
        errorPasswordTextView = findViewById(R.id.errorPasswordTextView)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
    }
    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        /**set Navigation Icon **/
        toolbar!!.navigationIcon = getDrawable(R.drawable.ic_navigation_back)
        /**設置前方Icon與Title之距離為0 **/
        toolbar!!.contentInsetStartWithNavigation = 0
        /**設置Icon圖樣的點擊事件 **/
        toolbar!!.setNavigationOnClickListener(View.OnClickListener {
            val Profile_intent = Intent()
            Profile_intent.setClass(this@EditProfileActivity, MainActivity::class.java)
            Profile_intent.putExtra("fragmentToShow", "ProfileFragment")
            startActivity(Profile_intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        })
    }
    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val Profile_intent = Intent()
        Profile_intent.setClass(this@EditProfileActivity, MainActivity::class.java)
        Profile_intent.putExtra("fragmentToShow", "ProfileFragment")
        startActivity(Profile_intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
    }
}