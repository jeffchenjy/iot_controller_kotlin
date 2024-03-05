package com.example.iot_kotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import okhttp3.OkHttpClient


class WFMainActivity : AppCompatActivity() {
    /*  ImageView  */
    private var imgAnim1: ImageView? = null
    private var imgAnim2:ImageView? = null
    private var wifi_Iv:ImageView? = null
    private var wifi_off_Iv:ImageView? = null
    /* Button and TextView*/
    private var CCI_button: Button? = null
    private var wifi_ssid_Tv: TextView? = null
    /*  About Activity  */
    private val context: Context? = this
    var wfMainActivity: WFMainActivity? = this@WFMainActivity

    private val client = OkHttpClient()
    private val AnimHandler = Handler(Looper.myLooper()!!)
    private var shareData: SharedPreferences? = null
    /*  About ToolBar */
    private var toolbar: Toolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigation_view: NavigationView? = null
    /*cmd url*/
    private var url_title = "https://"
    private var url: StringBuilder? = null
    private var cmd_url: String? = null
    /*video url*/
    private var v_url_title = "https://"
    private var v_url: StringBuilder? = null
    private var video_url: String? = null


    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_wf_main)
        setToolbar()
        findView()
        button_setOnClickListener()
        setNavigationItemSelectedListener()
        shareData = getSharedPreferences("URL", 0)
        /*CMD URL*/
        this.url = java.lang.StringBuilder()
        this.url!!.append(shareData!!.getString("Url", "hook.eu2.make.com/7fzzzajfbkl9j15hp93au3tqrlw5ighm?color=color = "))
        cmd_url = url_title + this.url
        /*VIDEO URL*/
        this.v_url = java.lang.StringBuilder()
        this.v_url!!.append(shareData!!.getString("video_Url", "storage.googleapis.com/exoplayer-test-media-0/play.mp3"))
        video_url = v_url_title + this.v_url

        /*Wifi*/
        WifiUtils.connectWifi(this)
        if (WifiUtils.isWifiEnabled(context)) {
            wifi_Iv!!.visibility = View.VISIBLE
            imgAnim1!!.visibility = View.VISIBLE
            imgAnim2!!.visibility = View.VISIBLE
            wifi_off_Iv!!.visibility = View.INVISIBLE
        }
        else {
            wifi_Iv!!.visibility = View.INVISIBLE
            imgAnim1!!.visibility = View.INVISIBLE
            imgAnim2!!.visibility = View.INVISIBLE
            wifi_off_Iv!!.visibility = View.VISIBLE
        }
        runnableAnim.run()
    }
    private fun setToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = resources.getString(R.string.wifi)
        drawerLayout = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        navigation_view = findViewById<View>(R.id.navigation_view) as NavigationView
        /**set Navigation Icon */
        toolbar!!.navigationIcon = getDrawable(R.drawable.ic_navigation_back)
        /**設置前方Icon與Title之距離為0 */
        toolbar!!.contentInsetStartWithNavigation = 0
        /**設置Icon圖樣的點擊事件 */
        toolbar!!.setNavigationOnClickListener(View.OnClickListener { v: View? ->
            val Main_intent = Intent()
            Main_intent.setClass(this@WFMainActivity, MainActivity::class.java)
            startActivity(Main_intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        })
    }
    private fun findView() {
        wifi_ssid_Tv = findViewById<View>(R.id.wifi_ssid_Tv) as TextView
        CCI_button = findViewById<View>(R.id.CCI_button) as Button
        imgAnim1 = findViewById<View>(R.id.imgAnim1) as ImageView
        imgAnim2 = findViewById<View>(R.id.imgAnim2) as ImageView
        wifi_Iv = findViewById<View>(R.id.wifi_Iv) as ImageView
        wifi_off_Iv = findViewById<View>(R.id.wifi_off_Iv) as ImageView
    }
    private fun button_setOnClickListener() {
        CCI_button!!.setOnClickListener(ButtonClick())
    }
    private fun ButtonClick() : View.OnClickListener? {
        return View.OnClickListener {
            val view = it as? View
            val viewId = view?.id
            when(viewId) {
                R.id.CCI_button -> {
                    val WFCarControl_intent = Intent()
                    WFCarControl_intent.putExtra("CmdUrl", cmd_url)
                    WFCarControl_intent.putExtra("VideoUrl", video_url)
                    WFCarControl_intent.setClass(this@WFMainActivity, WFCarControlActivity::class.java)
                    startActivity(WFCarControl_intent)
                }
            }
        }
    }
    private val runnableAnim: Runnable = object : Runnable {
        override fun run() {
            if (WifiUtils.isWifiEnabled(context)) {
                wifi_Iv!!.visibility = View.VISIBLE
                wifi_off_Iv!!.visibility = View.INVISIBLE
                wifi_ssid_Tv!!.text = WifiUtils.WifiSSID(context)
                /** Animation **/
                wifi_Iv!!.setImageResource(R.drawable.ic_iot_wifi)
                imgAnim1!!.animate().scaleX(2f).scaleY(2f).alpha(0f).setDuration(1000)
                    .withEndAction {
                        wifi_Iv!!.setImageResource(R.drawable.ic_iot_wifi_2bar)
                        imgAnim1!!.scaleX = 1f
                        imgAnim1!!.scaleY = 1f
                        imgAnim1!!.alpha = 1f
                    }
                imgAnim2!!.animate().scaleX(2f).scaleY(2f).alpha(0f).setDuration(700)
                    .withEndAction {
                        wifi_Iv!!.setImageResource(R.drawable.ic_iot_wifi_1bar)
                        imgAnim2!!.scaleX = 1f
                        imgAnim2!!.scaleY = 1f
                        imgAnim2!!.alpha = 1f
                    }
            } else {
                wifi_Iv!!.visibility = View.INVISIBLE
                wifi_off_Iv!!.visibility = View.VISIBLE
                imgAnim1!!.animate().scaleX(1f).scaleY(1f).alpha(0f).setDuration(1000)
                    .withEndAction {
                        imgAnim1!!.scaleX = 1f
                        imgAnim1!!.scaleY = 1f
                        imgAnim1!!.alpha = 1f
                    }
                imgAnim2!!.animate().scaleX(1f).scaleY(1f).alpha(0f).setDuration(700)
                    .withEndAction {
                        imgAnim2!!.scaleX = 1f
                        imgAnim2!!.scaleY = 1f
                        imgAnim2!!.alpha = 1f
                    }
                wifi_ssid_Tv!!.text = "No Wifi"
            }
            AnimHandler.postDelayed(this, 1500)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.wf_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.wifi_nav -> wf_main_nav()
        }
        return super.onOptionsItemSelected(menuItem)
    }
    private fun setNavigationItemSelectedListener() {
        navigation_view!!.setNavigationItemSelectedListener { item -> // 點選時收起選單
            drawerLayout!!.closeDrawer(GravityCompat.END)
            // 取得選項id
            val id = item.itemId
            if (id == R.id.action_home) {
                val Main_intent = Intent()
                Main_intent.setClass(this@WFMainActivity, MainActivity::class.java)
                startActivity(Main_intent)
            } else if (id == R.id.current_url) {
                Current_Url_Dialog()
            } else if (id == R.id.action_url) {
                Url_Setup_Dialog()
            } else if (id == R.id.current_video_url) {
                Current_Video_Url_Dialog()
            } else if (id == R.id.action_video_url) {
                Video_Url_Setup_Dialog()
            } else if (id == R.id.wf_Operation) {
                Operation_Dialog()
            }
            false
        }
    }
    private fun Operation_Dialog() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_help)
            .setTitle(resources.getString(R.string.operation_title))
            .setMessage(resources.getString(R.string.wf_operation))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }

    private fun Current_Url_Dialog() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_http)
            .setTitle(resources.getString(R.string.current_url))
            .setMessage(cmd_url)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }

    private fun Current_Video_Url_Dialog() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_video)
            .setTitle(resources.getString(R.string.current_video_url))
            .setMessage(video_url)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }

    private fun Url_Setup_Dialog() {
        val inflate: View = layoutInflater.inflate(R.layout.url_setup, null)
        val editText_url = inflate.findViewById<View>(R.id.editText_url) as EditText
        val http_text = inflate.findViewById<View>(R.id.http_text) as TextView
        val radioGroup_http = inflate.findViewById<View>(R.id.radioGroup_http) as RadioGroup

        radioGroup_http.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton_http -> http_text.text = "http://"
                R.id.radioButton_https -> http_text.text = "https://"
            }
        }
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_setting)
            .setTitle(resources.getString(R.string.url_setting))
            .setView(inflate)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                val obj = editText_url.text.toString()
                if (obj.length != 0) {
                    wfMainActivity!!.url!!.setLength(0)
                    wfMainActivity!!.url!!.append(obj)
                    shareData!!.edit().putString("Url", obj).commit()
                }
                url_title = http_text.text.toString()
                cmd_url = url_title + wfMainActivity!!.url
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun Video_Url_Setup_Dialog() {
        val inflate: View = layoutInflater.inflate(R.layout.url_setup, null)
        val editText_url = inflate.findViewById<View>(R.id.editText_url) as EditText
        val http_text = inflate.findViewById<View>(R.id.http_text) as TextView
        val radioGroup_http = inflate.findViewById<View>(R.id.radioGroup_http) as RadioGroup
        radioGroup_http.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton_http -> http_text.text = "http://"
                R.id.radioButton_https -> http_text.text = "https://"
            }
        }
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_video_setting)
            .setTitle(resources.getString(R.string.video_url_setting))
            .setView(inflate)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                val obj = editText_url.text.toString()
                if (obj.length != 0) {
                    wfMainActivity!!.v_url!!.setLength(0)
                    wfMainActivity!!.v_url!!.append(obj)
                    shareData!!.edit().putString("video_Url", obj).commit()
                }
                v_url_title = http_text.text.toString()
                video_url = v_url_title + wfMainActivity!!.v_url
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
    private fun wf_main_nav() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.END)) {
            drawerLayout!!.closeDrawer(GravityCompat.END)
        } else {
            drawerLayout!!.openDrawer(GravityCompat.END)
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        AnimHandler.removeCallbacksAndMessages(null)
    }
}