package com.example.iot_kotlin

import android.app.ActivityManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Path
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

class HomeFragment : Fragment() {
    /*  Tasks ArrayList  */
    private var imageTasks: List<Runnable> = ArrayList()
    private var currentIndex = 0
    private val Iv_handler = Handler(Looper.myLooper()!!)
    private val Iv_delay = 500
    private var animationPaused: Boolean = false
    /*  About ToolBar */
    private var drawerLayout: DrawerLayout? = null
    private var navigation_view: NavigationView? = null
    private var itemIdToFind: Int? = null
    private var menu: Menu? = null
    private var menuItem: MenuItem? =null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    private var nightMode: Boolean? = false
    /* URL */
    private val twiter_url = "https://twitter.com/"
    private val github_url = "https://github.com/jeffchenjy/iot_controller_kotlin.git"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drawerLayout = view.findViewById(R.id.drawerLayout)
        navigation_view = view.findViewById(R.id.navigation_view)
        setToolbar(view)
        setNavigationItemSelectedListener()
        /** Mode Change **/
        menu = navigation_view?.menu
        itemIdToFind = R.id.action_change_mode
        menuItem = navigation_view?.menu?.findItem(itemIdToFind ?: 0)
        /* use sharedPreferences change themes*/
        sharedPreferences = requireActivity().getSharedPreferences("MODE", MODE_PRIVATE)

        changeTheme()
        imageAnimation()
    }
    private fun setToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.main_toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.iot_menu)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_main_navigation)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            drawerLayout?.let {
                if (it.isDrawerOpen(GravityCompat.START)) {
                    it.closeDrawer(GravityCompat.START)
                } else {
                    it.openDrawer(GravityCompat.START)
                }
            }
        }
    }
    private fun imageAnimation() {
        if (!animationPaused) {
            val path = Path()
            path.addCircle(0f, 0f, 350f, Path.Direction.CW)
            val runnableList = listOf(
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imagePhone), path, 200 / 30, 7) },
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imageGame), path, 200 / 30, 7) },
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imageTV), path, 200 / 30, 7) },
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imageHome), path, 200 / 30, 7) },
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imageWifi), path, 200 / 30, 7) },
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imageBT), path, 200 / 30, 7) },
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imageChip), path, 200 / 30, 7) },
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imagePerson), path, 200 / 30, 7) },
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imageCar), path, 200 / 30, 7) },
                Runnable { view?.findViewById<TextView>(R.id.textViewCloud)?.text = "IoT" }
            )
            imageTasks += runnableList
            executeNextTask()
        }
    }

    private fun executeNextTask() {
        try {
            val currentTask = imageTasks.getOrNull(currentIndex)
            currentTask?.run()
            currentIndex += 1
            if (currentIndex != imageTasks.size) {
                Iv_handler.postDelayed({ executeNextTask() }, Iv_delay.toLong())
            } else {
                Iv_handler.removeCallbacksAndMessages(null)
            }
        } catch (e: Exception) {
            Log.d("Task", e.toString())
            Iv_handler.removeCallbacksAndMessages(null)
        }

    }
    private fun changeTheme() {
        nightMode = sharedPreferences.getBoolean("nightMode", false)
        nightMode?.let { isNightMode ->
            if (isNightMode) {
                menuItem?.title = getString(R.string.light_mode)
                menuItem?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_light_mode)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                menuItem?.title = getString(R.string.dark_mode)
                menuItem?.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_dark_mode)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
    private fun setNavigationItemSelectedListener() {
        navigation_view?.setNavigationItemSelectedListener { item -> // 点击时收起菜单
            drawerLayout?.closeDrawer(GravityCompat.START)
            // 获取选项 id
            when (item.itemId) {
                R.id.action_home -> {
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
                    changeMode()
                }
                R.id.action_operation -> {
                    operationDialog()
                }
                R.id.action_about -> {
                    aboutDialog()
                }
                R.id.action_copyright -> {
                    copyrightDialog()
                }
                R.id.action_exit -> {
                    exit_app()
                }
            }
            false
        }
    }
    private fun changeMode() {
        try {
            animationPaused = true
            if (menuItem != null && requireContext() != null) {
                val title: CharSequence? = menuItem!!.title
                val context = requireContext()
                if (title != null) {
                    if (nightMode!!) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        edit = sharedPreferences.edit()
                        edit.putBoolean("nightMode", false)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        edit = sharedPreferences.edit()
                        edit.putBoolean("nightMode", true)
                    }
                    edit.apply()
                    if (title.toString() == context.getString(R.string.light_mode)) { // 使用正确的上下文调用 getString()
                        menuItem!!.title = context.getString(R.string.dark_mode)
                        menuItem!!.icon = ContextCompat.getDrawable(context, R.drawable.ic_dark_mode)
                    } else {
                        menuItem!!.title = context.getString(R.string.light_mode)
                        menuItem!!.icon = ContextCompat.getDrawable(context, R.drawable.ic_light_mode)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // 打印异常信息，便于调试
        } finally {
            animationPaused = false
        }
    }

    private fun operationDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_help)
            .setTitle(resources.getString(R.string.main_operation_title))
            .setMessage(resources.getString(R.string.main_operation))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun aboutDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_about)
            .setTitle(resources.getString(R.string.about_app))
            .setMessage(resources.getString(R.string.about))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }
    private fun copyrightDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_copyright)
            .setTitle(resources.getString(R.string.copyright_title))
            .setMessage(resources.getString(R.string.copyright))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
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
}