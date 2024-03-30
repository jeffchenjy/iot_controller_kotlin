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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView

class BTSearchFragment: Fragment() {
    /*  About ToolBar */
    private lateinit var toolbar: Toolbar
    /*  Anim ImageView  */
    private lateinit var imgAnim1: ImageView
    private lateinit var imgAnim2: ImageView
    private val AnimHandler = Handler(Looper.myLooper()!!)
    /* BlueTooth */
    private val Permission_REQUEST_Code = 100
    private var receiverFlag = false
    private var broadcastReceiver: BroadcastReceiver? = null
    private var mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var mSearch_btn: Button

    /*  RecyclerView */
    private lateinit var mScanRecyclerView: RecyclerView
    private lateinit var scanAdapter: RecyclerViewAdapter // 自定義的 Adapter
    private var btScanDeviceList = ArrayList<String>() // 藍牙設備列表

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bluetooth_search, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        setToolbar()
        this.runnableAnimScan.run()
        /** Bluetooth Check Permission **/
        val checkSelfPermission = ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_FINE_LOCATION")
        val checkSelfPermission2 = ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_COARSE_LOCATION")
        if (checkSelfPermission != 0 && checkSelfPermission2 != 0) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"), Permission_REQUEST_Code)
        }
        /** Initial the RecyclerView **/
        mScanRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        scanAdapter = RecyclerViewAdapter(btScanDeviceList) // 創建自定義的 Adapter
        mScanRecyclerView.adapter = scanAdapter
        /** Utilize ItemClickSupport to enable the RecyclerView to utilize click listener methods **/
        ItemClickSupport.addTo(mScanRecyclerView)
        mScanRecyclerView.onItemClick { recyclerView, position, _ ->
            val itemValue = (recyclerView.adapter as RecyclerViewAdapter).getItem(position)
            val name_substring = itemValue.substring(0, itemValue.length - 17)
            val mac_substring = itemValue.substring(itemValue.length - 17)
            MaterialAlertDialogBuilder(requireContext(),  R.style.CustomDialogTheme)
                .setIcon(R.drawable.ic_bt_search)
                .setTitle(resources.getString(R.string.bluetooth_connect))
                .setMessage(resources.getString(R.string.bt_search_dialog_start)+"("+name_substring+")"+resources.getString(R.string.bt_search_dialog_end))
                .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ ->
                    try {
                        if (itemValue.length >= 17) {
                            val remoteDevice = mBluetoothAdapter?.getRemoteDevice(mac_substring)
                            remoteDevice?.let {
                                val method = it.javaClass.getMethod("createBond")
                                method.invoke(it)
                            }
                        } else {
                            CustomSnackbar.showSnackbar(getView(), requireContext(), "Bluetooth MAC's length is not 17!")
                            //showToast("Bluetooth MAC's length is not 17!")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        CustomSnackbar.showSnackbar(getView(), requireContext(), "Error Catch!")
                        //showToast("Error Catch!")
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        /* search bt device */
        try {
            mBluetoothAdapter!!.startDiscovery()
            val intentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            receiverFlag = true
            broadcastReceiver = object : BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (action == BluetoothDevice.ACTION_FOUND) {
                        val device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                        val arrayAdapter = scanAdapter
                        if (device != null && !arrayAdapter.containsData(device.address)) {
                            arrayAdapter!!.addData(device.name + "\n" + device.address)
                        }
                    }
                }
            }
            requireContext().registerReceiver(broadcastReceiver, intentFilter)
            CustomSnackbar.showSnackbar(getView(), requireContext(), "Search BT device first.")
            //showToast("Search BT device first.")
        } catch (e: Exception) {
            CustomSnackbar.showSnackbar(getView(), requireContext(), "Can't get bluetooth device")
            //showToast("Can't get bluetooth device")
        }
        /** Search Button**/
        this.mSearch_btn.setOnClickListener(View.OnClickListener {
            try {
                if (receiverFlag) {
                    mBluetoothAdapter!!.cancelDiscovery()
                    btScanDeviceList.clear()
                    scanAdapter.clearData()
                    mScanRecyclerView.adapter = scanAdapter
                    mBluetoothAdapter!!.startDiscovery()
                    CustomSnackbar.showSnackbar(getView(), requireContext(), "Search BT device again.")
                    //showToast("Search BT device again.")
                }
            } catch (e: Exception) {
                CustomSnackbar.showSnackbar(getView(), requireContext(), "Can't get bluetooth device")
                //showToast("Can't get bluetooth device")
            }
        })
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        imgAnim1 = view.findViewById(R.id.imgAnim1)
        imgAnim2 = view.findViewById(R.id.imgAnim2)
        mScanRecyclerView = view.findViewById(R.id.Scan_RecyclerView)
        mSearch_btn = view.findViewById(R.id.search_btn)
    }

    private fun setToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.bluetooth_scan)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentManager.popBackStack()
            fragmentTransaction.commit()
        }
    }

    private val runnableAnimScan: Runnable = object : Runnable {
        override fun run() {
            imgAnim1.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000).withEndAction {
                imgAnim1.scaleX = 1f
                imgAnim1.scaleY = 1f
                imgAnim1.alpha = 1f
            }
            imgAnim2.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(700).withEndAction {
                imgAnim2.scaleX = 1f
                imgAnim2.scaleY = 1f
                imgAnim2.alpha = 1f
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
            CustomSnackbar.showSnackbar(getView(), requireContext(), "BT search is denied.")
            //showToast("BT search is denied.")
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        mBluetoothAdapter!!.cancelDiscovery()
        if (broadcastReceiver == null || !receiverFlag) {
            return
        }
        // Unregister the Bluetooth discovery receiver when the activity is destroyed
        requireContext().unregisterReceiver(broadcastReceiver)
        receiverFlag = false
        AnimHandler.removeCallbacks(runnableAnimScan)
        super.onDestroy()
    }
}