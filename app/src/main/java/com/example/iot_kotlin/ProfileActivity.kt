package com.example.iot_kotlin

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    /* NavigationBarView */
    private lateinit var bottom_navigation: NavigationBarView
    /* TextView */
    private lateinit var titleUsername: TextView
    private lateinit var profileUsername: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profilePassword: TextView
    /* Button */
    private lateinit var editButton: Button
    private lateinit var signOutButton: Button
    /* String */
    private lateinit var password: String
    /* flag */
    private var nouserflag: Boolean? = false
    override fun onCreate(bundle : Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_profile)
        findView()
        setNavigationBarViewItemSelectedListener()
        /** Show User Info **/
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val currentUserUID = currentUser.uid
            reference = FirebaseDatabase.getInstance().getReference("users/UID/$currentUserUID")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)
                        password = snapshot.child("password").getValue(String::class.java)!!
                        titleUsername.text = username
                        profileUsername.text = username
                        profileEmail.text = email
                        //profilePassword.text = password
                        val subText = password!!.substring(0, 2)
                        val hiddenText = "*".repeat(10)
                        profilePassword.text = subText+hiddenText
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
        } else {
            nouserflag = true
            titleUsername.text = "Guest"
            profileUsername.text = "Guest"
            profileEmail.text = "Guest"
            profilePassword.text = "Guest"
        }
        /** Button OnClickListener **/
        editButton!!.setOnClickListener{
            if(!nouserflag!!) {
                val builder = AlertDialog.Builder(this@ProfileActivity)
                val dialogView: View = layoutInflater.inflate(R.layout.dialog_password_verify, null)
                val codeBox = dialogView.findViewById<EditText>(R.id.codeBox)
                builder.setView(dialogView)
                val dialog: AlertDialog = builder.create()
                dialogView.findViewById<View>(R.id.btnCheck).setOnClickListener(View.OnClickListener {
                    val userCode = codeBox.text.toString()
                    if (userCode.equals(password)) {
                        dialog.dismiss()
                        val edit_intent = Intent()
                        edit_intent.setClass(this@ProfileActivity, EditProfileActivity::class.java)
                        startActivity(edit_intent)
                        finish()
                    } else {
                        showToast("Password error")
                    }
                })
                dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
                if (dialog.window != null) {
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                }
                dialog.show()
            } else {
                showToast("Unable to modify without logging in")
            }
        }
        signOutButton!!.setOnClickListener {
            MaterialAlertDialogBuilder(this,  R.style.CustomDialogTheme)
                .setIcon(R.drawable.ic_leave)
                .setTitle(resources.getString(R.string.logout))
                .setMessage(resources.getString(R.string.signout_message))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                    // Respond to positive button press
                    if(!nouserflag!!) {
                        auth.signOut()
                    }
                    val Login_intent = Intent()
                    Login_intent.setClass(this@ProfileActivity, LoginActivity::class.java)
                    startActivity(Login_intent)
                    finish()
                    dialog.dismiss()
                }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }
    }
    private fun findView(){
        bottom_navigation = findViewById(R.id.bottom_navigation)
        titleUsername = findViewById(R.id.titleUsername)
        profileUsername = findViewById(R.id.profileUsername)
        profileEmail = findViewById(R.id.profileEmail)
        profilePassword = findViewById(R.id.profilePassword)
        editButton = findViewById(R.id.editButton)
        signOutButton = findViewById(R.id.signOutButton)
    }
    private fun setNavigationBarViewItemSelectedListener() {
        val menu = bottom_navigation.menu
        menu.findItem(R.id.nbar_home).isChecked = false
        menu.findItem(R.id.nbar_info).isChecked = true
        // 通知 BottomNavigationView 重新繪製界面
        bottom_navigation.invalidate()
        bottom_navigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nbar_home -> {
                    val Profile_intent = Intent()
                    Profile_intent.setClass(this@ProfileActivity, MainActivity::class.java)
                    startActivity(Profile_intent)
                    finish()
                    true
                }
                R.id.nbar_info -> {
                    // Respond to navigation item 2 click
                    showToast("Already at account page")
                    true
                }
                else -> false
            }
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val Profile_intent = Intent()
        Profile_intent.setClass(this@ProfileActivity, MainActivity::class.java)
        startActivity(Profile_intent)
        finish()
    }
}