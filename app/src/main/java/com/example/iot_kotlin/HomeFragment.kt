package com.example.iot_kotlin

import android.app.ActivityManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.ColorDrawable
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
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    /* firebase User*/
    private var currentUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
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
    private var isNightMode: Boolean? = false
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
        changeThemeInit()
        imageAnimation()
        firebaseUserCheck()
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
                Runnable { ViewPathAnimator.animate(view?.findViewById(R.id.imageDatabase), path, 200 / 30, 7) },
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
    private fun changeThemeInit() {
        /* use sharedPreferences change themes*/
        sharedPreferences = requireActivity().getSharedPreferences("MODE", MODE_PRIVATE)
        isNightMode = sharedPreferences.getBoolean("nightMode", false)
    }
    private fun setNavigationItemSelectedListener() {
        navigation_view?.setNavigationItemSelectedListener { item ->
            drawerLayout?.closeDrawer(GravityCompat.START)
            when (item.itemId) {
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
    private fun firebaseUserCheck() {
        val getCheckUserFlag = requireActivity().intent.getStringExtra("checkUserFlag")
        requireActivity().intent.removeExtra("checkUserFlag")
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        if(currentUser != null && getCheckUserFlag != null) {
            /** Show message **/
            val builder = AlertDialog.Builder(requireContext())
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_message_show, null)
            val showMessage = dialogView.findViewById<TextView>(R.id.textView_showMessage)
            builder.setView(dialogView)
            val dialog: AlertDialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            showMessage.text = getString(R.string.loginsuccess)
            dialog.show()
            Handler(Looper.myLooper()!!).postDelayed({
                dialog.dismiss()
            }, 800)
        }
    }
    private fun changeMode() {
        try {
            animationPaused = true
            if (isNightMode!!) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                edit = sharedPreferences.edit()
                edit.putBoolean("nightMode", false)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                edit = sharedPreferences.edit()
                edit.putBoolean("nightMode", true)
            }
            edit.apply()
        } catch (e: Exception) {
            e.printStackTrace()
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
                Iv_handler.removeCallbacksAndMessages(null)
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