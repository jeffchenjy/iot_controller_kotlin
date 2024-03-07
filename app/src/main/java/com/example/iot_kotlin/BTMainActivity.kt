package com.example.iot_kotlin

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.navigation.NavigationView
import pl.droidsonroids.gif.GifDrawable

class BTMainActivity : AppCompatActivity() {
    /*  About Activity  */
    private val context: Context? = this
    var btMainActivity: BTMainActivity? = this
    /*  Button  */
    private var mDiscoverable_btn:Button? = null
    private var mPaired_btn:Button? = null
    private var bt_switch_btn: MaterialSwitch? = null
    /*  ImageView  */
    private var mBluetoothIv: ImageView? = null
    private var mBluetooth_off_Iv: ImageView? = null
    private var imgAnim1: ImageView? = null
    private var imgAnim2: ImageView? = null
    private val AnimHandler = Handler(Looper.myLooper()!!)
    /*  TextView  */
    private var mStatusBluetoothTv: TextView? = null
    private var mPair_title:TextView? = null
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
        if (result.resultCode == RESULT_OK) {
            Log.e("Activity result", "OK")
            // There are no request codes
            val data = result.data
        }
    }
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_bt_main)
        setToolbar()
        findView()
        setClickListener()
        setNavigationItemSelectedListener()
        /** Init **/
        this.mDevice_list!!.visibility = View.INVISIBLE
        this.mPair_title!!.visibility = View.INVISIBLE

        /** Initial the RecyclerView **/
        mDevice_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        PairedAdapter = RecyclerViewAdapter(btPairedDeviceList) // 創建自定義的 Adapter
        mDevice_list!!.adapter = PairedAdapter
        /** Utilize ItemClickSupport to enable the RecyclerView to utilize click listener methods **/
        ItemClickSupport.addTo(mDevice_list)
        mDevice_list.onItemClick { recyclerView, position, v ->
            itemData = (recyclerView.adapter as RecyclerViewAdapter).getItem(position)
            val mainActivity1 = Intent()
            mainActivity1.putExtra("btData", itemData)
            mainActivity1.setClass(this@BTMainActivity, CarControlActivity::class.java)
            startActivity(mainActivity1)
        }
        /** Animation initial**/
        try {
            val gifDrawable = GifDrawable(assets, "bluetooth_on.gif")
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
                val thumbDrawable = ContextCompat.getDrawable(this, R.drawable.ic_switch_check)
                bt_switch_btn!!.thumbIconDrawable = thumbDrawable
                if (!mBluetoothAdapter!!.isEnabled) {
                    mPair_title!!.visibility = View.INVISIBLE
                    mDevice_list!!.visibility = View.INVISIBLE
                    showToast("Turning On Bluetooth...")
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        mBluetoothAdapter!!.enable()
                    }
                } else {
                    showToast("Bluetooth is already on")
                }
            } else {
                // Switch 按鈕被關閉
                bt_switch_btn!!.thumbIconDrawable = null
                mPair_title!!.visibility = View.INVISIBLE
                mDevice_list!!.visibility = View.INVISIBLE
                if (mBluetoothAdapter!!.isEnabled) {
                    mBluetoothAdapter!!.disable()
                    showToast("Turning Bluetooth Off")
                } else {
                    showToast("Bluetooth is already off")
                }
            }
        }
    }
    private fun setToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = resources.getString(R.string.bluetooth)
        drawerLayout = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        navigation_view = findViewById<View>(R.id.navigation_view) as NavigationView
        /**set Navigation Icon **/
        toolbar!!.navigationIcon = getDrawable(R.drawable.ic_navigation_back)
        /**設置前方Icon與Title之距離為0 **/
        toolbar!!.contentInsetStartWithNavigation = 0
        /**設置Icon圖樣的點擊事件 **/
        toolbar!!.setNavigationOnClickListener(View.OnClickListener {
            val Main_intent = Intent()
            Main_intent.setClass(this@BTMainActivity, MainActivity::class.java)
            startActivity(Main_intent)
            //overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,R.anim.slide_in_left, R.anim.slide_out_right, R.color.transition_color)//API 34
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        })
    }
    private fun findView() {
        mStatusBluetoothTv = findViewById(R.id.statusBluetoothTv)
        mPair_title = findViewById(R.id.pair_title)
        mBluetoothIv = findViewById(R.id.bluetooth_Iv)
        mBluetooth_off_Iv = findViewById(R.id.bluetooth_off_Iv)
        imgAnim1 = findViewById(R.id.imgAnim1)
        imgAnim2 = findViewById(R.id.imgAnim2)
        mDiscoverable_btn = findViewById(R.id.discoverable_btn)
        mPaired_btn = findViewById(R.id.paired_btn)
        bt_switch_btn = findViewById(R.id.bt_switch_btn)
        mDevice_list = findViewById(R.id.device_RecyclerView)
    }
    private fun setClickListener() {
        mDiscoverable_btn!!.setOnClickListener(ButtonClick())
        mPaired_btn!!.setOnClickListener(ButtonClick())
    }
    private val runnableAnim: Runnable = object : Runnable {
        override fun run() {
            if (btMainActivity!!.mBluetoothAdapter!!.isEnabled) {
                mBluetoothIv!!.visibility = View.VISIBLE
                mBluetooth_off_Iv!!.visibility = View.INVISIBLE
                if (!bt_switch_btn!!.isChecked) {
                    bt_switch_btn!!.isChecked = true
                    bt_switch_btn!!.thumbIconDrawable = getDrawable(R.drawable.ic_switch_check)
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
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { }
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
                        this.PairedAdapter!!.clearData()
                        this.mDevice_list!!.adapter = this.PairedAdapter
                        if (mBluetoothAdapter!!.isEnabled) {
                            /*Set mPair_title and mDevice_list VISIBLE*/
                            mPair_title!!.visibility = View.VISIBLE
                            mDevice_list!!.visibility = View.VISIBLE
                            var devices: Set<BluetoothDevice> = mBluetoothAdapter!!.bondedDevices
                            var arrayAdapter = this.PairedAdapter
                            for (device: BluetoothDevice in devices) {
                                arrayAdapter!!.addData(device.name+"\n"+device.address)
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Permission_REQUEST_Code || grantResults.isEmpty()) {
            // Check if the permission was granted
            return
        }
        if (grantResults[0] == -1 && grantResults[1] == -1) {
            showToast("BT scan is denied.")
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.bt_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.bt_nav -> bt_main_nav()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun setNavigationItemSelectedListener() {
        navigation_view!!.setNavigationItemSelectedListener { item -> // 點選時收起選單
            drawerLayout!!.closeDrawer(GravityCompat.END)
            // 取得選項id
            when (item.itemId) {
                R.id.action_home -> {
                    val Main_intent = Intent()
                    Main_intent.setClass(this@BTMainActivity, MainActivity::class.java)
                    startActivity(Main_intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
                R.id.bluetooth_search -> {
                    mPair_title!!.visibility = View.INVISIBLE
                    mDevice_list!!.visibility = View.INVISIBLE
                    if (mBluetoothAdapter!!.isEnabled) {
                        val BTScan_intent = Intent()
                        BTScan_intent.setClass(this@BTMainActivity, BTscanActivity::class.java)
                        startActivity(BTScan_intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    } else {
                        showToast("Please turn on Bluetooth to search devices")
                    }
                }
                R.id.bt_Operation -> {
                    operation_Dialog()
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
    private fun operation_Dialog() {
        MaterialAlertDialogBuilder(this,  R.style.CustomDialogTheme)
            .setIcon(R.drawable.ic_help)
            .setTitle(resources.getString(R.string.operation_title))
            .setMessage(resources.getString(R.string.bt_operation))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }

    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}