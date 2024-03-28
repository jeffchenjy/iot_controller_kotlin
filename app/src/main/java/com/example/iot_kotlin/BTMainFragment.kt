package com.example.iot_kotlin

import android.Manifest
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.navigation.NavigationView
import pl.droidsonroids.gif.GifDrawable

class BTMainFragment: Fragment() {
    /*  Button  */
    private var mDiscoverable_btn: Button? = null
    private var mPaired_btn: Button? = null
    private var bt_switch_btn: MaterialSwitch? = null
    /*  ImageView  */
    private var mBluetoothIv: ImageView? = null
    private var mBluetooth_off_Iv: ImageView? = null
    private var imgAnim1: ImageView? = null
    private var imgAnim2: ImageView? = null
    private val AnimHandler = Handler(Looper.myLooper()!!)
    /*  TextView  */
    private var mStatusBluetoothTv: TextView? = null
    private var mPair_title: TextView? = null
    /*  About ToolBar */
    private var toolbar: Toolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var navigation_view: NavigationView? = null
    /* BlueTooth */
    private val Request_enable_BT = 0
    private val Permission_REQUEST_Code = 100
    /*  RecyclerView */
    private lateinit var mDevice_list: RecyclerView
    private lateinit var PairedAdapter: RecyclerViewAdapter // 自定義的 Adapter
    private var  btPairedDeviceList = ArrayList<String>() // 藍牙設備列表
    private var itemData: String? = null
    /* Bluetooth */
    private var mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    /* Use ActivityResultLauncher (starActivityForResult has been Deprecated)*/
    private var activityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            Log.e("Activity result", "OK")
            // There are no request codes
            val data = result.data
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth_main, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        setToolbar()
        setClickListener()
        setNavigationItemSelectedListener()
        /** Init **/
        mDevice_list.visibility = View.INVISIBLE
        mPair_title!!.visibility = View.INVISIBLE

        /** Initial the RecyclerView **/
        mDevice_list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        PairedAdapter = RecyclerViewAdapter(btPairedDeviceList) // 創建自定義的 Adapter
        mDevice_list!!.adapter = PairedAdapter
        /** Utilize ItemClickSupport to enable the RecyclerView to utilize click listener methods **/
        ItemClickSupport.addTo(mDevice_list)
        mDevice_list.onItemClick { recyclerView, position, _ ->
            itemData = (recyclerView.adapter as RecyclerViewAdapter).getItem(position)
            val currentActivity = requireActivity()
            val mainActivity1 = Intent(currentActivity, CarControlActivity::class.java)
            mainActivity1.putExtra("btData", itemData)
            currentActivity.startActivity(mainActivity1)
            currentActivity.finish()
        }
        /** Animation initial**/
        try {
            val gifDrawable = context?.assets?.let { GifDrawable(it, "bluetooth_on.gif") }
            mBluetoothIv!!.setImageDrawable(gifDrawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mBluetoothIv!!.visibility = View.INVISIBLE
        mBluetooth_off_Iv!!.visibility = View.INVISIBLE
        runnableAnim.run()
        /** BluetoothAdapter **/
        if (mBluetoothAdapter == null) mStatusBluetoothTv!!.text = "Bluetoothは利用できません"
        else mStatusBluetoothTv!!.text = "Bluetoothが利用可能です"
        /** Switch Button **/
        bt_switch_btn!!.setOnCheckedChangeListener { buttonView, isChecked ->
            // 當 Switch 按鈕的狀態發生變化時，會調用此方法
            if (isChecked) {
                // Switch 按鈕被打開
                val thumbDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_check)
                bt_switch_btn!!.thumbIconDrawable = thumbDrawable
                try{
                    if (!mBluetoothAdapter!!.isEnabled) {
                        mPair_title!!.visibility = View.INVISIBLE
                        mDevice_list!!.visibility = View.INVISIBLE
                        showToast("Turning On Bluetooth...")
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            mBluetoothAdapter!!.enable()
                        }
                    } else {
                        showToast("Bluetooth is already on")
                    }
                } catch (e: Exception) {
                }
            } else {
                // Switch 按鈕被關閉
                bt_switch_btn!!.thumbIconDrawable = null
                mPair_title!!.visibility = View.INVISIBLE
                mDevice_list!!.visibility = View.INVISIBLE
                try {
                    if (mBluetoothAdapter!!.isEnabled) {
                        mBluetoothAdapter!!.disable()
                        showToast("Turning Bluetooth Off")
                    } else {
                        showToast("Bluetooth is already off")
                    }
                } catch (e: Exception) {

                }
            }
        }
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        drawerLayout = view.findViewById(R.id.drawerLayout)
        navigation_view = view.findViewById(R.id.navigation_view)
        mStatusBluetoothTv = view.findViewById(R.id.statusBluetoothTv)
        mPair_title = view.findViewById(R.id.pair_title)
        mBluetoothIv = view.findViewById(R.id.bluetooth_Iv)
        mBluetooth_off_Iv = view.findViewById(R.id.bluetooth_off_Iv)
        imgAnim1 = view.findViewById(R.id.imgAnim1)
        imgAnim2 = view.findViewById(R.id.imgAnim2)
        mDiscoverable_btn = view.findViewById(R.id.discoverable_btn)
        mPaired_btn = view.findViewById(R.id.paired_btn)
        bt_switch_btn = view.findViewById(R.id.bt_switch_btn)
        mDevice_list = view.findViewById(R.id.device_RecyclerView)
    }
    private fun setToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title  = resources.getString(R.string.bluetooth)
    }
    private fun setClickListener() {
        mDiscoverable_btn!!.setOnClickListener(ButtonClick())
        mPaired_btn!!.setOnClickListener(ButtonClick())
    }
    private fun ButtonClick(): View.OnClickListener? {
        return View.OnClickListener {
            val view = it as? View
            val viewId = view?.id
            when (viewId){
                R.id.discoverable_btn -> {
                    // Discover bluetooth btn click
                    mPair_title!!.visibility = View.INVISIBLE
                    mDevice_list!!.visibility = View.INVISIBLE
                    if (mBluetoothAdapter!!.isEnabled) {
                        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { }
                        if (!mBluetoothAdapter!!.isDiscovering) {
                            val discoverintent =
                                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                            activityResultLauncher.launch(discoverintent)
                        }
                    } else {
                        showToast("Please turn on Bluetooth first")
                    }
                }
                R.id.paired_btn -> {    // Get Paired devices button click
                    try {
                        /*Clear DeviceList*/
                        this.btPairedDeviceList.clear()
                        this.PairedAdapter.clearData()
                        this.mDevice_list.adapter = this.PairedAdapter
                        if (mBluetoothAdapter!!.isEnabled) {
                            /*Set mPair_title and mDevice_list VISIBLE*/
                            mPair_title!!.visibility = View.VISIBLE
                            mDevice_list.visibility = View.VISIBLE
                            var devices: Set<BluetoothDevice> = mBluetoothAdapter!!.bondedDevices
                            var arrayAdapter = this.PairedAdapter
                            for (device: BluetoothDevice in devices) {
                                arrayAdapter.addData(device.name+"\n"+device.address)
                            }
                        } else {
                            //bluetooth is off so can't get paired devices
                            showToast("Please turn on Bluetooth to get paired devices")
                        }
                    } catch (e: Exception) {
                        showToast("Can't get Bluetooth paired device!")
                    }
                }
            }
        }
    }
    private val runnableAnim: Runnable = object : Runnable {
        override fun run() {
                if (mBluetoothAdapter!!.isEnabled) {
                    mBluetoothIv!!.visibility = View.VISIBLE
                    mBluetooth_off_Iv!!.visibility = View.INVISIBLE
                    if (!bt_switch_btn!!.isChecked) {
                        bt_switch_btn!!.isChecked = true
                        bt_switch_btn!!.thumbIconDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_switch_check)
                    }
                    /** Animation **/
                    imgAnim1!!.animate().scaleX(2f).scaleY(2f).alpha(0f).setDuration(610)
                        .withEndAction {
                            imgAnim1!!.scaleX = 1f
                            imgAnim1!!.scaleY = 1f
                            imgAnim1!!.alpha = 1f
                        }
                } else {
                    mBluetoothIv!!.visibility = View.INVISIBLE
                    mBluetooth_off_Iv!!.visibility = View.VISIBLE
                    if (bt_switch_btn!!.isChecked) {
                        bt_switch_btn!!.isChecked = false
                        bt_switch_btn!!.thumbIconDrawable = null
                    }
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
                }
                AnimHandler.postDelayed(this, 1650)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Permission_REQUEST_Code && grantResults.isNotEmpty()) {
            val permissionDenied = grantResults.any { it == PackageManager.PERMISSION_DENIED }
            if (permissionDenied) {
                showToast("BT scan is denied.")
            } else {
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bt_main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bt_nav -> bt_main_nav()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setNavigationItemSelectedListener() {
        navigation_view?.setNavigationItemSelectedListener { item ->
            drawerLayout?.closeDrawer(GravityCompat.END)
            when (item.itemId) {
                R.id.bluetooth_search -> {
                    mPair_title?.visibility = View.INVISIBLE
                    mDevice_list?.visibility = View.INVISIBLE
                    if (mBluetoothAdapter?.isEnabled == true) {
                        startBluetoothSearchFragment()
                    } else {
                        showToast("Please turn on Bluetooth to search devices")
                    }
                }
                R.id.bt_Operation -> {
                    operation_Dialog()
                }
                R.id.action_exit -> {
                    exit_app()
                }
            }
            false
        }
    }

    private fun bt_main_nav() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.END)) {
            drawerLayout!!.closeDrawer(GravityCompat.END)
        } else {
            drawerLayout!!.openDrawer(GravityCompat.END)
        }
    }

    private fun startBluetoothSearchFragment() {
        val fragment = BTSearchFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out_right  // popExit
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun operation_Dialog() {
        MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_help)
            .setTitle(resources.getString(R.string.operation_title))
            .setMessage(resources.getString(R.string.bt_operation))
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
    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        AnimHandler.removeCallbacks(runnableAnim)
        super.onDestroy()
    }
}