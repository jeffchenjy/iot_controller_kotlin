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
    /*  Button  */
    private var bt_btn: Button? = null
    private var wifi_btn:Button? = null
    private var exit_btn:Button? = null
    /*  ImageView  */
    private var imagePhone:ImageView? = null
    private var imageGame:ImageView? = null
    private var imageTV:ImageView? = null
    private var imageHome:ImageView? = null
    private var imageWifi:ImageView? = null
    private var imageBT:ImageView? = null
    private var imageChip:ImageView? = null
    private var imagePerson:ImageView? = null
    private var imageCar:ImageView? = null
    /*  TextView  */
    private var textViewCloud: TextView? = null
    /*  Tasks ArrayList  */
    private var imageTasks: List<Runnable> = ArrayList()
    private var currentIndex = 0
    private val Iv_handler = Handler(Looper.myLooper()!!)
    private val Iv_delay = 500
    /*  About Activity  */
    var mainActivity: MainActivity? = this@MainActivity
    private val context: Context? = this
    /*  About ToolBar */
    private var main_Toolbar: Toolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigation_view: NavigationView? = null
    private lateinit var bottom_navigation: NavigationBarView
    private var itemIdToFind: Int? = null
    private var menu: Menu? = null
    private var menuItem: MenuItem? =null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    private var nightMode: Boolean? = false
    /* URL */
    private val twiter_url = "https://twitter.com/"
    private val github_url = "https://github.com/jeffchenjy/iot_controller_kotlin.git"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findView()
        setToolbar()
        Image_Animation()
        setNavigationBarViewItemSelectedListener()
        setNavigationItemSelectedListener()
        /** Mode Change **/
        menu = navigation_view?.menu
        // 指定要查找的菜單項目的 ID
        itemIdToFind = R.id.action_change_mode
        // 使用 findItem() 方法來查找指定 ID 的菜單項目
        menuItem = menu?.findItem(itemIdToFind ?: 0)
        /* use sharedPreferences change themes*/
        sharedPreferences = getSharedPreferences("MODE", MODE_PRIVATE)
        nightMode = sharedPreferences?.getBoolean("nightMode", false)
        nightMode?.let { isNightMode ->
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                menuItem?.title = getString(R.string.light_mode)
                menuItem?.icon = ContextCompat.getDrawable(this, R.drawable.ic_light_mode)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                menuItem?.title = getString(R.string.dark_mode)
                menuItem?.icon = ContextCompat.getDrawable(this, R.drawable.ic_dark_mode)
            }
        }
        /** Button **/
        bt_btn!!.setOnClickListener(View.OnClickListener {
            val BTMain_intent = Intent()
            BTMain_intent.setClass(this@MainActivity, BTMainActivity::class.java)
            startActivity(BTMain_intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        })
        wifi_btn!!.setOnClickListener {
            val WFMain_intent = Intent()
            WFMain_intent.setClass(this@MainActivity, WFMainActivity::class.java)
            startActivity(WFMain_intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        exit_btn!!.setOnClickListener {
            MaterialAlertDialogBuilder(this,  R.style.CustomDialogTheme)
                .setIcon(R.drawable.ic_leave)
                .setTitle(resources.getString(R.string.leave_title))
                .setMessage(resources.getString(R.string.leave))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                    // Respond to positive button press
                    val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
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
    }
    private fun setToolbar() {
        setSupportActionBar(main_Toolbar)
        /** !!操作符表示一個非空斷言（non-null assertion）。
         * 它告訴編譯器，即使某個變數的類型是可空的，我也確定這個變數在此時不會為null。
         * 如果在運行時該變數的值為null，那麼就會拋出NullPointerException異常。 **/
        supportActionBar!!.title = resources.getString(R.string.iot_menu)
        /**set Navigation Icon */
        main_Toolbar!!.navigationIcon = getDrawable(R.drawable.ic_main_navigation)
        /**設置前方Icon與Title之距離為0 */
        main_Toolbar!!.contentInsetStartWithNavigation = 0
        /**設置Icon圖樣的點擊事件 */
        main_Toolbar!!.setNavigationOnClickListener(View.OnClickListener {
            if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
                drawerLayout!!.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout!!.openDrawer(GravityCompat.START)
            }
        })
    }
    private fun findView() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigation_view = findViewById(R.id.navigation_drawer)
        main_Toolbar = findViewById(R.id.main_toolbar)
        bottom_navigation = findViewById(R.id.bottom_navigation)
        imagePhone = findViewById(R.id.imagePhone)
        imageGame = findViewById(R.id.imageGame)
        imageTV = findViewById(R.id.imageTV)
        imageHome = findViewById(R.id.imageHome)
        imageWifi = findViewById(R.id.imageWifi)
        imageBT = findViewById(R.id.imageBT)
        imageChip = findViewById(R.id.imageChip)
        imagePerson = findViewById(R.id.imagePerson)
        imageCar = findViewById(R.id.imageCar)
        textViewCloud = findViewById(R.id.textViewCloud)
        bt_btn = findViewById(R.id.bt_btn)
        wifi_btn = findViewById(R.id.wifi_btn)
        exit_btn = findViewById(R.id.exit_btn)
    }
    private fun Image_Animation() {
        val path = Path()
        path.addCircle(0f, 0f, 350f, Path.Direction.CW)
        val RunnableList = listOf(
            Runnable { ViewPathAnimator.animate(imagePhone, path, 200 / 30, 7) },
            Runnable { ViewPathAnimator.animate(imageGame, path, 200 / 30, 7) },
            Runnable { ViewPathAnimator.animate(imageTV, path, 200 / 30, 7) },
            Runnable { ViewPathAnimator.animate(imageHome, path, 200 / 30, 7) },
            Runnable { ViewPathAnimator.animate(imageWifi, path, 200 / 30, 7) },
            Runnable { ViewPathAnimator.animate(imageBT, path, 200 / 30, 7) },
            Runnable { ViewPathAnimator.animate(imageChip, path, 200 / 30, 7) },
            Runnable { ViewPathAnimator.animate(imagePerson, path, 200 / 30, 7) },
            Runnable { ViewPathAnimator.animate(imageCar, path, 200 / 30, 7) },
            Runnable { textViewCloud!!.text = "IoT" })
        imageTasks += RunnableList
        executeNextTask()
    }
    private fun executeNextTask() {
        var currentTask = imageTasks[currentIndex] // 取得目前要執行的任務
        currentTask.run() // Runnable執行任務
        currentIndex += 1 // 計算下一個任務的索引
        if (currentIndex != imageTasks.size) {             // 延遲一段時間後執行下一個任務
            Iv_handler.postDelayed({ executeNextTask() }, Iv_delay.toLong())
        } else {
            Iv_handler.removeCallbacksAndMessages(null)
        }
    }
    private fun setNavigationBarViewItemSelectedListener() {
        bottom_navigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nbar_home -> {
                    // Respond to navigation item 1 click
                    showToast("Already at home page")
                    true
                }
                R.id.nbar_info -> {
                    val Profile_intent = Intent()
                    Profile_intent.setClass(this@MainActivity, ProfileActivity::class.java)
                    startActivity(Profile_intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    /*
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }*/

    private fun setNavigationItemSelectedListener() {
        navigation_view!!.setNavigationItemSelectedListener { item -> // 點選時收起選單
            drawerLayout!!.closeDrawer(GravityCompat.START)
            // 取得選項id
            when (item.itemId) {
                R.id.action_home -> {
                    showToast("Already at HomePage")
                }
                R.id.action_twitter -> {
                    val uri = Uri.parse(twiter_url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                R.id.action_github -> {
                    val uri = Uri.parse(github_url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                R.id.action_change_mode -> {
                    Change_Mode()
                }
                R.id.action_operation -> {
                    operation_Dialog()
                }
                R.id.action_about -> {
                    about_Dialog()
                }
                R.id.action_copyright -> {
                    copyright_Dialog()
                }
            }
            false
        }
    }
    private fun Change_Mode() {
        try {        // 檢查是否找到了菜單項目
            if (menuItem != null) {
                val title: CharSequence? = menuItem!!.title
                if(nightMode!!) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    edit = sharedPreferences!!.edit()
                    edit!!.putBoolean("nightMode", false)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    edit = sharedPreferences!!.edit()
                    edit!!.putBoolean("nightMode", true)
                }
                edit!!.apply()
                if (title.toString() == getString(R.string.light_mode)) {
                    menuItem!!.title = getString(R.string.dark_mode)
                    menuItem!!.icon = getDrawable(R.drawable.ic_dark_mode)
                } else {
                    menuItem!!.title = getString(R.string.light_mode)
                    menuItem!!.icon = getDrawable(R.drawable.ic_light_mode)
                }
            }
        } catch (e: Exception) {
            showToast(e.toString())
        }
    }
    private fun operation_Dialog() {
        MaterialAlertDialogBuilder(this,  R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_help)
            .setTitle(resources.getString(R.string.main_operation_title))
            .setMessage(resources.getString(R.string.main_operation))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }

    private fun about_Dialog() {
        MaterialAlertDialogBuilder(this,  R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_about)
            .setTitle(resources.getString(R.string.about_app))
            .setMessage(resources.getString(R.string.about))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }

    private fun copyright_Dialog() {
        MaterialAlertDialogBuilder(this,  R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_copyright)
            .setTitle(resources.getString(R.string.copyright_title))
            .setMessage(resources.getString(R.string.copyright))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.appTasks
        for (task in tasks) {
            task.finishAndRemoveTask()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Iv_handler.removeCallbacksAndMessages(null)
    }
    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}