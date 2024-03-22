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

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mHandler.sendEmptyMessageDelayed(GOTO_LoginFragment, 1000) //1秒跳轉
    }
    private val GOTO_LoginFragment = 0
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GOTO_LoginFragment -> {
                    val fragment = LoginFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in_right,  // enter
                            R.anim.fade_out,  // exit
                        )
                        .replace(R.id.fragment_container, fragment)
                        .commit()
                }
            }
        }
    }
}

