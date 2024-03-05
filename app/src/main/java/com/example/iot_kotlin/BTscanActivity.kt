package com.example.iot_kotlin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat

class BTscanActivity : AppCompatActivity() {
    /*  About Activity  */
    private val context: Context? = this
    var btscanActivity: BTscanActivity? = this
    /*  About ToolBar */
    private var toolbar: Toolbar? = null
    /*  Anim ImageView  */
    private var imgAnim1: ImageView? = null
    private  var imgAnim2:ImageView? = null
    private val AnimHandler = Handler(Looper.myLooper()!!)
    /* BlueTooth */
    private val Permission_REQUEST_Code = 100
    private var receiverFlag = false
    private var broadcastReceiver: BroadcastReceiver? = null
    private var scanAdapter: ArrayAdapter<String>? = null
    private var btScanDeviceList: List<String>? = null
    private var mScan_ListView: ListView? = null
    private var mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var mSearch_btn: Button? = null

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_bt_scan)
        setToolbar()
        imgAnim1 = findViewById<View>(R.id.imgAnim1) as ImageView
        imgAnim2 = findViewById<View>(R.id.imgAnim2) as ImageView
        this.runnableAnim.run()
        /** Bluetooth Check Permission **/
        val checkSelfPermission = ActivityCompat.checkSelfPermission(context!!, "android.permission.ACCESS_FINE_LOCATION")
        val checkSelfPermission2 = ActivityCompat.checkSelfPermission(context!!, "android.permission.ACCESS_COARSE_LOCATION")
        if (checkSelfPermission != 0 && checkSelfPermission2 != 0) {
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"), Permission_REQUEST_Code)
        }

        /** ListView **/
        mScan_ListView = findViewById<View>(R.id.Scan_ListView) as ListView
        btScanDeviceList = ArrayList<String>()
        scanAdapter = ArrayAdapter<String>(this.context, R.layout.list_view_style, btScanDeviceList!!)
        mScan_ListView!!.adapter = scanAdapter as ListAdapter?

        /** Bluetooth **/
        mBluetoothAdapter!!.startDiscovery()
        val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        receiverFlag = true
        broadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == "android.bluetooth.device.action.FOUND") {
                    val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                    val arrayAdapter = scanAdapter
                    if (device != null) {
                        arrayAdapter!!.add(device.name + "\n" + device.address)
                    }
                }
            }
        }
        registerReceiver(broadcastReceiver, intentFilter)
        showToast("Search BT device first.")
        /** Search Button**/
        mSearch_btn = findViewById<View>(R.id.search_btn) as Button
        this.mSearch_btn!!.setOnClickListener(View.OnClickListener {
            if (receiverFlag) {
                mBluetoothAdapter!!.cancelDiscovery()
                btScanDeviceList = emptyList()
                scanAdapter!!.clear()
                mScan_ListView!!.adapter = scanAdapter
                mBluetoothAdapter!!.startDiscovery()
                showToast("Search BT device again.")
            }
        })
        /** ListView ClickListener **/
        mScan_ListView!!.onItemClickListener =
            OnItemClickListener { adapterView, _, position, _ ->
                mBluetoothAdapter!!.cancelDiscovery()
                val obj = adapterView.getItemAtPosition(position).toString()
                try {
                    val substring = obj.substring(obj.length - 17)
                    val remoteDevice = mBluetoothAdapter!!.getRemoteDevice(substring)
                    val method = remoteDevice.javaClass.getMethod("createBond", null)
                    method.invoke(remoteDevice, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }
    private fun setToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = resources.getString(R.string.bluetooth_scan)
        /**set Navigation Icon */
        toolbar!!.navigationIcon = getDrawable(R.drawable.ic_navigation_back)
        /**設置前方Icon與Title之距離為0 */
        toolbar!!.contentInsetStartWithNavigation = 0
        /**設置Icon圖樣的點擊事件 */
        toolbar!!.setNavigationOnClickListener(View.OnClickListener {
            val BTMain_intent = Intent()
            BTMain_intent.setClass(this@BTscanActivity, BTMainActivity::class.java)
            startActivity(BTMain_intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        })
    }
    private val runnableAnim: Runnable = object : Runnable {
        override fun run() {
            imgAnim1!!.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000).withEndAction {
                imgAnim1!!.scaleX = 1f
                imgAnim1!!.scaleY = 1f
                imgAnim1!!.alpha = 1f
            }
            imgAnim2!!.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(700).withEndAction {
                imgAnim2!!.scaleX = 1f
                imgAnim2!!.scaleY = 1f
                imgAnim2!!.alpha = 1f
            }
            AnimHandler.postDelayed(this, 1500)
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
            showToast("BT search is denied.")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        mBluetoothAdapter!!.cancelDiscovery()
        if (broadcastReceiver == null || !receiverFlag) {
            return
        }
        // Unregister the Bluetooth discovery receiver when the activity is destroyed
        unregisterReceiver(broadcastReceiver)
        receiverFlag = false
        AnimHandler.removeCallbacks(runnableAnim)
    }

    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}