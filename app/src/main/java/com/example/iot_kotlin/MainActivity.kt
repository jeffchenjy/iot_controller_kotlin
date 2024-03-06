package com.example.iot_kotlin

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

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

    /* URL */
    val twiter_url = "https://twitter.com/"
    val github_url = "https://github.com/jeffchenjy/iot_controller_kotlin.git"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setToolbar()
        findView()
        Image_Animation()
        setNavigationItemSelectedListener()
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
            MaterialAlertDialogBuilder(this)
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
        drawerLayout = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        navigation_view = findViewById<View>(R.id.navigation_view) as NavigationView
        main_Toolbar = findViewById<View>(R.id.main_toolbar) as Toolbar
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
        imagePhone = findViewById<View>(R.id.imagePhone) as ImageView
        imageGame = findViewById<View>(R.id.imageGame) as ImageView
        imageTV = findViewById<View>(R.id.imageTV) as ImageView
        imageHome = findViewById<View>(R.id.imageHome) as ImageView
        imageWifi = findViewById<View>(R.id.imageWifi) as ImageView
        imageBT = findViewById<View>(R.id.imageBT) as ImageView
        imageChip = findViewById<View>(R.id.imageChip) as ImageView
        imagePerson = findViewById<View>(R.id.imagePerson) as ImageView
        imageCar = findViewById<View>(R.id.imageCar) as ImageView
        textViewCloud = findViewById<View>(R.id.textViewCloud) as TextView
        bt_btn = findViewById<View>(R.id.bt_btn) as Button
        wifi_btn = findViewById<View>(R.id.wifi_btn) as Button
        exit_btn = findViewById<View>(R.id.exit_btn) as Button
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

    private fun setNavigationItemSelectedListener() {
        navigation_view!!.setNavigationItemSelectedListener { item -> // 點選時收起選單
            drawerLayout!!.closeDrawer(GravityCompat.START)
            // 取得選項id
            when (item.itemId) {
                R.id.action_home -> {
                    val Main_intent = Intent()
                    Main_intent.setClass(this@MainActivity, MainActivity::class.java)
                    startActivity(Main_intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
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
    private fun operation_Dialog() {
        MaterialAlertDialogBuilder(this)
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
        MaterialAlertDialogBuilder(this)
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
        MaterialAlertDialogBuilder(this)
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

}