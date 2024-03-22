package com.example.iot_kotlin

import android.app.ActivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class StartLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startlog)
        val fragmentShow = intent.getStringExtra("fragmentShow")
        when (fragmentShow) {
            "LoginFragment" -> replaceFragment(LoginFragment())
            else -> replaceFragment(WelcomeFragment())
        }
        intent.removeExtra("fragmentShow")

    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
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