package com.example.iot_kotlin

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
   private lateinit var bottom_navigation: NavigationBarView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation = findViewById(R.id.bottom_navigation)


        val fragmentToShow = intent.getStringExtra("fragmentToShow")
        when (fragmentToShow) {
            "BTMainFragment" -> {
                bottom_navigation.selectedItemId = R.id.nbar_bt
                replaceFragment(BTMainFragment())
            }
            "WFMainFragment" -> {
                bottom_navigation.selectedItemId = R.id.nbar_wifi
                replaceFragment(WFMainFragment())

            }
            else -> {
                replaceFragment(HomeFragment())
            }
        }
        intent.removeExtra("fragmentToShow")


        bottom_navigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nbar_home -> {
                    replaceFragment(HomeFragment())
                }
                R.id.nbar_bt -> {
                    val fragmentManager = supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    val btSearchFragment = fragmentManager.findFragmentByTag("btSearchFragment")
                    if(btSearchFragment != null) {
                        fragmentManager.popBackStack("btSearchFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        transaction.commit()
                    } else {
                        replaceFragment(BTMainFragment())
                    }
                }
                R.id.nbar_wifi -> {
                    replaceFragment(WFMainFragment())
                }
                R.id.nbar_info -> {
                    val fragmentManager = supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    val editFragment = fragmentManager.findFragmentByTag("editFragment")
                    if(editFragment != null) {
                        fragmentManager.popBackStack("editFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        transaction.commit()
                    } else {
                        replaceFragment(ProfileFragment())
                    }
                }
            }
            true // 返回布林值表示事件是否已處理
        }

    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        // 替換為新的 Fragment
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else {
            MaterialAlertDialogBuilder(this, R.style.CustomDialogTheme)
                .setIcon(R.drawable.ic_leave)
                .setTitle(resources.getString(R.string.leave_title))
                .setMessage(resources.getString(R.string.leave))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
                    val tasks = activityManager.appTasks
                    for (task in tasks) {
                        task.finishAndRemoveTask()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}