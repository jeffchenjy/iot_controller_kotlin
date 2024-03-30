package com.example.iot_kotlin

import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Intent
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class WelcomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    /* Intent Activity */
    private lateinit var currentActivity: AppCompatActivity
    private lateinit var Main_intent: Intent
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        currentActivity = requireActivity() as AppCompatActivity
        currentUser = auth.currentUser
        mHandler.sendEmptyMessageDelayed(GOTO_LoginFragment, 800) //跳轉
    }
    private val GOTO_LoginFragment = 0
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GOTO_LoginFragment -> {
                    if (currentUser != null) {
                        startMainActivity()
                        return
                    } else {
                        val fragment = LoginFragment()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,  // enter
                                R.anim.fade_out,  // exit
                            )
                            .replace(R.id.fragment_container, fragment)
                            .hide(WelcomeFragment())
                            .commit()
                    }
                }
            }
        }
    }
    private fun startMainActivity() {
        Main_intent = Intent(currentActivity, MainActivity::class.java)
        currentActivity.startActivity(Main_intent)
        currentActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
        currentActivity.finish()
    }
}

