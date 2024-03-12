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


        var fragmentToShow = intent.getStringExtra("fragmentToShow")
        if (fragmentToShow != null) {
            if(fragmentToShow == "BTMainFragment") {
                bottom_navigation.selectedItemId = R.id.nbar_bt
                replaceFragment(BTMainFragment())
            }
            if(fragmentToShow == "WFMainFragment") {
                bottom_navigation.selectedItemId = R.id.nbar_wifi
                replaceFragment(WFMainFragment())
            }
            if(fragmentToShow == "ProfileFragment") {
                bottom_navigation.selectedItemId = R.id.nbar_info
                replaceFragment(ProfileFragment())
            }
        } else {
            replaceFragment(HomeFragment())
        }
        intent.removeExtra("fragmentToShow")



        bottom_navigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nbar_home -> {
                    replaceFragment(HomeFragment())
                }
                R.id.nbar_bt -> {
                    replaceFragment(BTMainFragment())
                }
                R.id.nbar_wifi -> {
                    replaceFragment(WFMainFragment())
                }
                R.id.nbar_info -> {
                    replaceFragment(ProfileFragment())
                }
            }
            true // 返回布林值表示事件是否已處理
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
    override fun onBackPressed() {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.appTasks
        for (task in tasks) {
            task.finishAndRemoveTask()
        }
        super.onBackPressed()
    }

}