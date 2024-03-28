package com.example.iot_kotlin

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    /* Firebase */
    private var currentUserUID: String? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    /* TextView */
    private lateinit var titleNickname: TextView
    private lateinit var profileUsername: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileDate: TextView
    private lateinit var profilePassword: TextView
    /* Button */
    private lateinit var editAccountButton: Button
    private lateinit var editProfileButton: Button
    private lateinit var signOutButton: Button
    /* String */
    private lateinit var password: String
    /* flag */
    private var nouserflag: Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        firebaseInit()
        /** Button OnClickListener **/
        buttonClickListener()
    }
    private fun findView(view: View){
        titleNickname = view.findViewById(R.id.titleNickname)
        profileUsername = view.findViewById(R.id.profileUsername)
        profileEmail = view.findViewById(R.id.profileEmail)
        profileDate = view.findViewById(R.id.profileDate)
        profilePassword = view.findViewById(R.id.profilePassword)
        editAccountButton = view.findViewById(R.id.editAccountButton)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        signOutButton = view.findViewById(R.id.signOutButton)
    }
    private fun firebaseInit() {
        /** Get firebase data **/
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        if (currentUser != null) {
            /** Show progress indicators **/
            val builder = AlertDialog.Builder(requireContext())
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
            builder.setView(dialogView)
            val dialog: AlertDialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            currentUserUID = currentUser!!.uid
            reference = FirebaseDatabase.getInstance().getReference("users/UID/$currentUserUID")
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        dialog.dismiss()
                        val username = snapshot.child("username").getValue(String::class.java)
                        val nickname = snapshot.child("nickname").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)
                        password = snapshot.child("password").getValue(String::class.java)!!
                        //profilePassword.text = password
                        val subText = password!!.substring(0, 2)
                        val hiddenText = "*".repeat(10)
                        currentUser?.metadata?.creationTimestamp?.let { creationTimestamp ->
                            val creationDate = Date(creationTimestamp)
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formattedDate = sdf.format(creationDate)
                            profileDate.text = formattedDate
                        }
                        titleNickname.text = nickname
                        profileUsername.text = username
                        profileEmail.text = email
                        profilePassword.text = subText+hiddenText
                    } else {
                        // 處理資料不存在的情況
                        showToast("User data not found")
                        dialog.dismiss()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // 處理讀取資料失敗的情況
                    showToast("User data load failed")
                    dialog.dismiss()
                }
            })
        } else {
            nouserflag = true
            titleNickname.text = "Guest"
            profileUsername.text = " "
            profileEmail.text = " "
            profileDate.text = " "
            profilePassword.text = " "
        }
    }
    private fun buttonClickListener() {
        editAccountButton.setOnClickListener(buttonOnClickListener())
        editProfileButton.setOnClickListener(buttonOnClickListener())
        signOutButton.setOnClickListener(buttonOnClickListener())
    }
    private fun buttonOnClickListener() : View.OnClickListener? {
        return View.OnClickListener {
            val view = it as? View
            val viewId = view?.id
            when(viewId) {
                R.id.editAccountButton -> {
                    if(!nouserflag!!) {
                        val fragment = EditAccountFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out_right  // popExit
                            )
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        showToast("Unable to modify without logging in")
                    }
                }
                R.id.editProfileButton -> {
                    if(!nouserflag!!) {
                        val fragment = EditProfileFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out_right  // popExit
                            )
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        showToast("Unable to modify without logging in")
                    }
                }
                R.id.signOutButton -> {
                    MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
                        .setIcon(R.drawable.ic_leave)
                        .setTitle(resources.getString(R.string.logout))
                        .setMessage(resources.getString(R.string.signout_message))
                        .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                            // Respond to positive button press
                            if(!nouserflag!!) {
                                auth.signOut()
                            }
                            dialog.dismiss()
                            val currentActivity = requireActivity()
                            val loginIntent = Intent(currentActivity, StartLogActivity::class.java)
                            loginIntent.putExtra("fragmentShow", "LoginFragment")
                            currentActivity.startActivity(loginIntent)
                            currentActivity.overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out)
                            currentActivity.finish()

                        }
                        .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}