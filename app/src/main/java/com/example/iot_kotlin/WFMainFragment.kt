package com.example.iot_kotlin

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.iot_kotlin.Constants.REQUEST_LOCATION_PERMISSION
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import okhttp3.OkHttpClient
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest


class WFMainFragment: Fragment() {
    /*  ImageView  */
    private var imgAnim1: ImageView? = null
    private var imgAnim2: ImageView? = null
    private var wifi_Iv: ImageView? = null
    private var wifi_off_Iv: ImageView? = null
    /* Button and TextView*/
    private var CCI_button: Button? = null
    private var wifi_ssid_Tv: TextView? = null

    private val client = OkHttpClient()
    private val AnimHandler = Handler(Looper.myLooper()!!)
    private var shareData: SharedPreferences? = null
    /*  About ToolBar */
    private var toolbar: Toolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigation_view: NavigationView? = null
    /*cmd url*/
    private var url_title = "http://"
    private var url: StringBuilder? = null
    private var cmd_url: String? = null
    /*video url*/
    private var v_url_title = "http://"
    private var v_url: StringBuilder? = null
    private var video_url: String? = null
    private val Permission_REQUEST_Code = 100
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wifi_main, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        setToolbar()
        button_setOnClickListener()
        setNavigationItemSelectedListener()
        shareData = requireContext().getSharedPreferences("URL", 0)
        /*CMD URL*/
        this.url = java.lang.StringBuilder()
        this.url!!.append(shareData!!.getString("Url", "172.20.10.8:8000/car/"))
        cmd_url = url_title + this.url
        /*VIDEO URL*/
        this.v_url = java.lang.StringBuilder()
        this.v_url!!.append(shareData!!.getString("video_Url", "172.20.10.8:8080/?action=stream"))
        video_url = v_url_title + this.v_url
        /*Wifi*/
        requestLocationPermission()
        WifiUtils.connectWifi(requireContext())
        if (WifiUtils.isWifiEnabled(requireContext())) {
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
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        drawerLayout = view.findViewById(R.id.drawerLayout)
        navigation_view = view.findViewById(R.id.navigation_view)
        wifi_ssid_Tv = view.findViewById(R.id.wifi_ssid_Tv)
        CCI_button = view.findViewById(R.id.CCI_button)
        imgAnim1 = view.findViewById(R.id.imgAnim1)
        imgAnim2 = view.findViewById(R.id.imgAnim2)
        wifi_Iv = view.findViewById(R.id.wifi_Iv)
        wifi_off_Iv = view.findViewById(R.id.wifi_off_Iv)
    }
    private fun setToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title  = resources.getString(R.string.wifi)
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
                    val currentActivity = requireActivity()
                    val WFCarControl_intent = Intent(currentActivity, WFCarControlActivity::class.java)
                    WFCarControl_intent.putExtra("CmdUrl", cmd_url)
                    WFCarControl_intent.putExtra("VideoUrl", video_url)
                    currentActivity.startActivity(WFCarControl_intent)
                    currentActivity.finish()
                }
            }
        }
    }
    private val runnableAnim: Runnable = object : Runnable {
        override fun run() {
            if (WifiUtils.isWifiEnabled(requireContext())) {
                wifi_Iv!!.visibility = View.VISIBLE
                wifi_off_Iv!!.visibility = View.INVISIBLE
                wifi_ssid_Tv!!.text = WifiUtils.WifiSSID(requireContext())
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
                wifi_ssid_Tv!!.text = "No Wi-Fi"
            }
            AnimHandler.postDelayed(this, 1500)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.wf_main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.wifi_nav -> wf_main_nav()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setNavigationItemSelectedListener() {
        navigation_view?.setNavigationItemSelectedListener { item ->
            drawerLayout?.closeDrawer(GravityCompat.END)
            when (item.itemId) {
                R.id.current_url -> {
                    Current_Url_Dialog()
                }
                R.id.action_url -> {
                    Url_Setup_Dialog()
                }
                R.id.current_video_url -> {
                    Current_Video_Url_Dialog()
                }
                R.id.action_video_url -> {
                    Video_Url_Setup_Dialog()
                }
                R.id.wf_Operation -> {
                    Operation_Dialog()
                }
                R.id.action_exit -> {
                    exit_app()
                }
            }
            false
        }
    }

    private fun wf_main_nav() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.END)) {
            drawerLayout!!.closeDrawer(GravityCompat.END)
        } else {
            drawerLayout!!.openDrawer(GravityCompat.END)
        }
    }
    private fun Operation_Dialog() {
        MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
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
        MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
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
        MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
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
        val inflate: View = layoutInflater.inflate(R.layout.dialog_url_setup, null)
        val editText_url = inflate.findViewById<View>(R.id.editText_url) as EditText
        val http_text = inflate.findViewById<View>(R.id.http_text) as TextView
        val radioGroup_http = inflate.findViewById<View>(R.id.radioGroup_http) as RadioGroup

        radioGroup_http.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton_http -> http_text.text = "http://"
                R.id.radioButton_https -> http_text.text = "https://"
            }
        }
        MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_setting)
            .setTitle(resources.getString(R.string.url_setting))
            .setView(inflate)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                // Respond to positive button press
                val obj = editText_url.text.toString()
                if (obj.length != 0) {
                    url!!.setLength(0)
                    url!!.append(obj)
                    shareData!!.edit().putString("Url", obj).commit()
                }
                url_title = http_text.text.toString()
                cmd_url = url_title + url
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun Video_Url_Setup_Dialog() {
        val inflate: View = layoutInflater.inflate(R.layout.dialog_url_setup, null)
        val editText_url = inflate.findViewById<View>(R.id.editText_url) as EditText
        val http_text = inflate.findViewById<View>(R.id.http_text) as TextView
        val radioGroup_http = inflate.findViewById<View>(R.id.radioGroup_http) as RadioGroup
        radioGroup_http.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton_http -> http_text.text = "http://"
                R.id.radioButton_https -> http_text.text = "https://"
            }
        }
        MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_video_setting)
            .setTitle(resources.getString(R.string.video_url_setting))
            .setView(inflate)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                val obj = editText_url.text.toString()
                if (obj.length != 0) {
                    v_url!!.setLength(0)
                    v_url!!.append(obj)
                    shareData!!.edit().putString("video_Url", obj).commit()
                }
                v_url_title = http_text.text.toString()
                video_url = v_url_title + v_url
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
    private fun exit_app() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_leave)
            .setTitle(resources.getString(R.string.leave_title))
            .setMessage(resources.getString(R.string.leave))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = activityManager.appTasks
                for (task in tasks) {
                    task.finishAndRemoveTask()
                }
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    /* check Location Permission */
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private fun requestLocationPermission() {
        val perms = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (EasyPermissions.hasPermissions(requireContext(), *perms)) {
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"), Permission_REQUEST_Code)
        }
    }
    override fun onDestroy() {
        AnimHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}