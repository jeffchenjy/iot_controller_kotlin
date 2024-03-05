package com.example.iot_kotlin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_welcome)
        mHandler.sendEmptyMessageDelayed(GOTO_MAIN_ACTIVITY, 1000) //1秒跳轉
    }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GOTO_MAIN_ACTIVITY -> {
                    val intent = Intent()
                    intent.setClass(this@WelcomeActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else -> {}
            }
        }
    }
    companion object {
        private const val GOTO_MAIN_ACTIVITY = 0
    }
}