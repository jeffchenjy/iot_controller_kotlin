package com.example.iot_kotlin

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CarControlActivity : AppCompatActivity()  {
    /*  About Activity  */
    private val context: Context? = this
    var carcontrolActivity: CarControlActivity? = this@CarControlActivity
    /*  About ToolBar */
    private var toolbar: Toolbar? = null
    /* About StringBuilder */
    private var CAR_STOP: StringBuilder? = null
    private var CAR_BACK: StringBuilder? = null
    private var CAR_FORWARD: StringBuilder? = null
    private var CAR_LEFT: StringBuilder? = null;
    private var CAR_RIGHT: StringBuilder? = null;
    private var CAR_A_Func: StringBuilder? = null
    private var CAR_B_Func: StringBuilder? = null
    private var CAR_X_Func: StringBuilder? = null
    private var CAR_Y_Func: StringBuilder? = null
    private var music_play_btn: StringBuilder? = null
    private var music_previous_btn: StringBuilder? = null
    private var music_next_btn: StringBuilder? = null
    private var shareData: SharedPreferences? = null
    /* String */
    private val music_pause : String = "O"
    private val music_volume_up : String = "+"
    private val music_volume_down : String = "-"
    private var directionCmd: String? = null
    private var music_controlCmd: String? = null
    private var remoteDeviceInfo: String? = null
    private var remoteMacAddress: String? = null
    private var songCmd: String? = null
    private val songArrayData = arrayOf("Song 1", "Song 2", "Song 3", "Song 4", "Song 5", "Song 6", "Song 7", "Song 8", "Song 9", "Song 10")
    /* Int and Boolean */
    private var music_volume_value: Int = 15
    private var music_flag= false
    private var cmd_flag= false
    private var control_flag= false
    /* Button */
    private var button_Up: Button? = null
    private var button_Down: Button? = null
    private var button_Left: Button? = null
    private var button_Right: Button? = null
    private var button_Stop: Button? = null
    private var button_A: Button? = null
    private var button_B: Button? = null
    private var button_X: Button? = null
    private var button_Y: Button? = null
    private var Link_Button: Button? = null
    /* Btn Handler and Runnable */
    val Btn_handler = Handler(Looper.myLooper()!!)
    val Btn_delay = 200 // 每0.2秒輸出一次
    var longPressRunnable: Runnable? = null
    /* Bluetooth */
    private var mBTAdapter: BluetoothAdapter? = null
    private var mChatService: BTChatService? = null
    /** The Handler that gets information back from the BluetoothChatService **/
    private val mHandler: Handler? = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            val i = message.what
            if (i == 2) {
                String(message.obj as ByteArray, 0, message.arg1)
            } else if (i != 4) {
                if (i != 5) {
                    return
                }
                Toast.makeText(context, message.data.getString(Constants.TOAST), Toast.LENGTH_SHORT)
                    .show()
            } else {
                val string = message.data.getString(Constants.DEVICE_NAME)
                val context = context
                Toast.makeText(context, "Connected to $string", Toast.LENGTH_SHORT).show()
                Link_Button!!.text = "DisLink"
            }
        }
    }
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_car_control)
        setToolbar()
        findView()
        shareData = getSharedPreferences("CarCMD", 0)
        stringBuilder()
        Button_setOnClickListener()
        Button_setOnLongClickListener()
        Button_setOnTouchListener()
        /*Get BT device ID and MAC*/
        remoteDeviceInfo = intent.getStringExtra("btData")
        /* BluetoothAdapter and BTChatService */
        mBTAdapter = BluetoothAdapter.getDefaultAdapter()
        mChatService = BTChatService(this, mHandler)
        val str = remoteDeviceInfo
        if (str != null) {
            remoteMacAddress = str.substring(str.length - 17)
            //BTChatService
            mChatService!!.connect(this.mBTAdapter!!.getRemoteDevice(remoteMacAddress), true)
        }
        Link_Button!!.setOnClickListener {
            if (Link_Button!!.text == "DisLink") {
                showToast("DisLink Bluetooth device...")
                mChatService!!.stop()
                Link_Button!!.text = "Link"
            } else if (Link_Button!!.text == "Link") {
                showToast("Link with Bluetooth device again...")
                if (remoteDeviceInfo != null) {
                    val carActivity = this@CarControlActivity
                    carActivity.remoteMacAddress =
                        carActivity.remoteDeviceInfo!!.substring(remoteDeviceInfo!!.length - 17)
                    //BTChatService
                    mChatService!!.connect(
                        mBTAdapter!!.getRemoteDevice(
                            remoteMacAddress
                        ), true
                    )
                    if (mChatService!!.link_use_str.equals("disconnect device")) {
                        Link_Button!!.text = "DisLink"
                    }
                }
            }
        }
    }
    private fun setToolbar() {
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.title = " "
        //supportActionBar!!.title = resources.getString(R.string.car_control_page)
        /**set Navigation Icon */
        toolbar!!.navigationIcon = getDrawable(R.drawable.ic_navigation_back)
        /**設置前方Icon與Title之距離為0 */
        toolbar!!.contentInsetStartWithNavigation = 0
        /**設置Icon圖樣的點擊事件 */
        toolbar!!.setNavigationOnClickListener(View.OnClickListener {
            val BTMain_intent = Intent()
            BTMain_intent.setClass(this@CarControlActivity, BTMainActivity::class.java)
            startActivity(BTMain_intent)
        })
    }
    private fun findView() {
        button_Up = findViewById<View>(R.id.button_up) as Button
        button_Down = findViewById<View>(R.id.button_down) as Button
        button_Left = findViewById<View>(R.id.button_left) as Button
        button_Right = findViewById<View>(R.id.button_right) as Button
        button_Stop = findViewById<View>(R.id.Stop_button) as Button
        button_A = findViewById<View>(R.id.button_a) as Button
        button_B = findViewById<View>(R.id.button_b) as Button
        button_X = findViewById<View>(R.id.button_x) as Button
        button_Y = findViewById<View>(R.id.button_y) as Button
        Link_Button = findViewById<View>(R.id.BT_Link_Button) as Button
    }
    private fun stringBuilder() {
        this.CAR_FORWARD = java.lang.StringBuilder()
        this.CAR_FORWARD!!.append(shareData!!.getString("Forward", "f"))
        this.CAR_BACK = java.lang.StringBuilder()
        this.CAR_BACK!!.append(shareData!!.getString("Back", "b"))
        this.CAR_LEFT = java.lang.StringBuilder()
        this.CAR_LEFT!!.append(shareData!!.getString("Left", "l"))
        this.CAR_RIGHT = java.lang.StringBuilder()
        this.CAR_RIGHT!!.append(shareData!!.getString("Right", "r"))
        this.CAR_STOP = java.lang.StringBuilder()
        this.CAR_STOP!!.append(shareData!!.getString("Stop", "p"))
        this.CAR_A_Func = java.lang.StringBuilder()
        this.CAR_A_Func!!.append(shareData!!.getString("A_button", "a"))
        this.CAR_B_Func = java.lang.StringBuilder()
        this.CAR_B_Func!!.append(shareData!!.getString("B_button", "d"))
        this.CAR_X_Func = java.lang.StringBuilder()
        this.CAR_X_Func!!.append(shareData!!.getString("X_button", "x"))
        this.CAR_Y_Func = java.lang.StringBuilder()
        this.CAR_Y_Func!!.append(shareData!!.getString("Y_button", "y"))

        this.music_play_btn = java.lang.StringBuilder()
        this.music_play_btn!!.append(shareData!!.getString("music_play_btn", "S"))
        this.music_previous_btn = java.lang.StringBuilder()
        this.music_previous_btn!!.append(shareData!!.getString("music_previous_btn", "P"))
        this.music_next_btn = java.lang.StringBuilder()
        this.music_next_btn!!.append(shareData!!.getString("music_next_btn", "N"))
    }
    private fun Button_setOnClickListener() {
        this.button_Up!!.setOnClickListener(ButtonClick())
        this.button_Down!!.setOnClickListener(ButtonClick())
        this.button_Left!!.setOnClickListener(ButtonClick())
        this.button_Right!!.setOnClickListener(ButtonClick())
        this.button_Stop!!.setOnClickListener(ButtonClick())
        this.button_A!!.setOnClickListener(ButtonClick())
        this.button_B!!.setOnClickListener(ButtonClick())
        this.button_X!!.setOnClickListener(ButtonClick())
        this.button_Y!!.setOnClickListener(ButtonClick())
    }
    private fun ButtonClick() : View.OnClickListener? {
        return View.OnClickListener {
            val view = it as? View
            val viewId = view?.id
            when(viewId) {
                R.id.button_up -> {
                    if (cmd_flag && !control_flag) {
                        directionCmd = this.CAR_FORWARD.toString()
                        val carActivity = this@CarControlActivity
                        carActivity.sendCMD(carActivity.directionCmd!!)
                    } else if (cmd_flag && control_flag) {
                        showToast("Please long click the direction button")
                    } else {
                        showToast("Please select mode first ! (Button A,B,X,Y)")
                    }
                }
                R.id.button_down -> {
                    if (cmd_flag && !control_flag) {
                        directionCmd = this.CAR_BACK.toString()
                        val carActivity = this@CarControlActivity
                        carActivity.sendCMD(carActivity.directionCmd!!)
                    } else if (cmd_flag && control_flag) {
                        showToast("Please long click the direction button")
                    } else {
                        showToast("Please select mode first ! (Button A,B,X,Y)")
                    }
                }
                R.id.button_left -> {
                    if (cmd_flag && !control_flag) {
                        directionCmd = this.CAR_LEFT.toString()
                        val carActivity = this@CarControlActivity
                        carActivity.sendCMD(carActivity.directionCmd!!)
                    } else if (cmd_flag && control_flag) {
                        showToast("Please long click the direction button")
                    } else {
                        showToast("Please select mode first ! (Button A,B,X,Y)")
                    }
                }
                R.id.button_right -> {
                    if (cmd_flag && !control_flag) {
                        directionCmd = this.CAR_RIGHT.toString()
                        val carActivity = this@CarControlActivity
                        carActivity.sendCMD(carActivity.directionCmd!!)
                    } else if (cmd_flag && control_flag) {
                        showToast("Please long click the direction button")
                    } else {
                        showToast("Please select mode first ! (Button A,B,X,Y)")
                    }
                }
                R.id.Stop_button -> {
                    if (cmd_flag && !control_flag) {
                        directionCmd = this.CAR_STOP.toString()
                        val carActivity = this@CarControlActivity
                        carActivity.sendCMD(carActivity.directionCmd!!)
                    }
                    else {
                        showToast("Please select mode first ! (Button A,B,X,Y)")
                    }
                }
                R.id.button_a -> {
                    directionCmd = this.CAR_A_Func.toString()
                    val carActivity = this@CarControlActivity
                    carActivity.sendCMD(carActivity.directionCmd!!)
                    cmd_flag = true
                    control_flag = false
                }
                R.id.button_b -> {
                    directionCmd = this.CAR_B_Func.toString()
                    val carActivity = this@CarControlActivity
                    carActivity.sendCMD(carActivity.directionCmd!!)
                    cmd_flag = true
                    control_flag = false
                }
                R.id.button_x -> {
                    directionCmd = this.CAR_X_Func.toString()
                    val carActivity = this@CarControlActivity
                    carActivity.sendCMD(carActivity.directionCmd!!)
                    cmd_flag = true
                    control_flag = false
                }
                R.id.button_y -> {
                    directionCmd = this.CAR_Y_Func.toString()
                    val carActivity = this@CarControlActivity
                    carActivity.sendCMD(carActivity.directionCmd!!)
                    cmd_flag = true
                    control_flag = true
                }
            }
        }
    }
    private fun Button_setOnLongClickListener() {
        this.button_Up!!.setOnLongClickListener(ButtonLongClick())
        this.button_Down!!.setOnLongClickListener(ButtonLongClick())
        this.button_Left!!.setOnLongClickListener(ButtonLongClick())
        this.button_Right!!.setOnLongClickListener(ButtonLongClick())
    }
    private fun ButtonLongClick() : View.OnLongClickListener? {
        return View.OnLongClickListener {
            val view = it as? View
            val viewId = view?.id
            when (viewId) {
                R.id.button_up -> {
                    if (cmd_flag) {
                        longPressRunnable = object : Runnable {
                            override fun run() {
                                directionCmd = carcontrolActivity!!.CAR_FORWARD.toString()
                                val carActivity = this@CarControlActivity
                                carActivity.sendCMD(carActivity.directionCmd!!)
                                // 設置下一次輸出
                                Btn_handler.postDelayed(this, Btn_delay.toLong())
                            }
                        }
                        Btn_handler.post(this.longPressRunnable!!)
                    }
                }
                R.id.button_down -> {
                    if (cmd_flag) {
                        longPressRunnable = object : Runnable {
                            override fun run() {
                                directionCmd = carcontrolActivity!!.CAR_BACK.toString()
                                val carActivity = this@CarControlActivity
                                carActivity.sendCMD(carActivity.directionCmd!!)
                                // 設置下一次輸出
                                Btn_handler.postDelayed(this, Btn_delay.toLong())
                            }
                        }
                        Btn_handler.post(this.longPressRunnable!!)
                    }
                }
                R.id.button_left -> {
                    if (cmd_flag) {
                        longPressRunnable = object : Runnable {
                            override fun run() {
                                directionCmd = carcontrolActivity!!.CAR_LEFT.toString()
                                val carActivity = this@CarControlActivity
                                carActivity.sendCMD(carActivity.directionCmd!!)
                                // 設置下一次輸出
                                Btn_handler.postDelayed(this, Btn_delay.toLong())
                            }
                        }
                        Btn_handler.post(this.longPressRunnable!!)
                    }
                }
                R.id.button_right -> {
                    if (cmd_flag) {
                        longPressRunnable = object : Runnable {
                            override fun run() {
                                directionCmd = carcontrolActivity!!.CAR_RIGHT.toString()
                                val carActivity = this@CarControlActivity
                                carActivity.sendCMD(carActivity.directionCmd!!)
                                // 設置下一次輸出
                                Btn_handler.postDelayed(this, Btn_delay.toLong())
                            }
                        }
                        Btn_handler.post(this.longPressRunnable!!)
                    }
                }
            }
            true
        }
    }

    private fun Button_setOnTouchListener() {
        button_Up!!.setOnTouchListener(ButtonOnTouch())
        button_Down!!.setOnTouchListener(ButtonOnTouch())
        button_Left!!.setOnTouchListener(ButtonOnTouch())
        button_Right!!.setOnTouchListener(ButtonOnTouch())
    }
    private fun ButtonOnTouch() :OnTouchListener? {
        return View.OnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if(cmd_flag && control_flag) {
                        if(v.id == R.id.button_down || v.id == R.id.button_left || v.id == R.id.button_right || v.id == R.id.button_up) {
                            // 放開按鈕時停止持續執行的操作
                            Btn_handler.removeCallbacksAndMessages(null)
                            directionCmd = carcontrolActivity!!.CAR_STOP.toString()
                            val carActivity = this@CarControlActivity
                            carActivity.sendCMD(carActivity.directionCmd!!)
                        }
                    }
                }
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.car_control_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.music_select -> selectMelody_Dialog()
            R.id.music_control -> Music_Control_Dialog()
            R.id.Direction_setup -> Direction_setup_Dialog()
            R.id.Car_CMD_List -> Car_CMD_List_Dialog()
            R.id.Instruction -> Instruction_Dialog()
        }
        return super.onOptionsItemSelected(menuItem)
    }
    private fun Music_Control_Dialog() {
        val inflate: View = layoutInflater.inflate(R.layout.music_control, null)
        var btn_music_previous  = inflate.findViewById<View>(R.id.music_previous) as ImageButton
        var btn_music_play = inflate.findViewById<View>(R.id.music_play) as ImageButton
        var btn_music_next = inflate.findViewById<View>(R.id.music_next) as ImageButton
        var btn_volume_minus = inflate.findViewById<View>(R.id.volume_minus) as ImageButton
        var btn_volume_add = inflate.findViewById<View>(R.id.volume_add) as ImageButton
        var progressBar = inflate.findViewById<View>(R.id.progressBar) as ProgressBar
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_music_menu)
            .setTitle(resources.getString(R.string.music_control))
            .setView(inflate)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
        progressBar.progress = music_volume_value
        if (music_flag) {
            val music_play_drawable = ContextCompat.getDrawable(this, R.drawable.ic_music_pause)
            btn_music_play.setImageDrawable(music_play_drawable)
        } else {
            val music_play_drawable = ContextCompat.getDrawable(this, R.drawable.ic_music_play)
            btn_music_play.setImageDrawable(music_play_drawable)
        }
        btn_music_previous!!.setOnClickListener(View.OnClickListener {
            music_controlCmd = this.music_previous_btn!!.toString()
            val carActivity = this@CarControlActivity
            carActivity.sendCMD(carActivity.music_controlCmd!!)
            val music_play_drawable = ContextCompat.getDrawable(this, R.drawable.ic_music_pause)
            btn_music_play.setImageDrawable(music_play_drawable)
            music_flag = true
        })
        btn_music_next!!.setOnClickListener(View.OnClickListener {
            music_controlCmd = this.music_next_btn!!.toString()
            val carActivity = this@CarControlActivity
            carActivity.sendCMD(carActivity.music_controlCmd!!)
            val music_play_drawable = ContextCompat.getDrawable(this, R.drawable.ic_music_pause)
            btn_music_play.setImageDrawable(music_play_drawable)
            music_flag = true
        })
        btn_music_play!!.setOnClickListener(View.OnClickListener {
            if (music_flag) {
                val carActivity = this@CarControlActivity
                carActivity.sendCMD(carActivity.music_pause)
                music_flag = false
                val music_play_drawable = ContextCompat.getDrawable(this, R.drawable.ic_music_play)
                btn_music_play.setImageDrawable(music_play_drawable)
            } else {
                music_controlCmd =
                    this.music_play_btn!!.toString()
                val carActivity = this@CarControlActivity
                carActivity.sendCMD(carActivity.music_controlCmd!!)
                music_flag = true
                val music_play_drawable = ContextCompat.getDrawable(this, R.drawable.ic_music_pause)
                btn_music_play.setImageDrawable(music_play_drawable)
            }
        })
        btn_volume_minus!!.setOnClickListener(View.OnClickListener {
            if (music_volume_value > 0) {
                music_volume_value--
                progressBar.progress = music_volume_value
                val carActivity = this@CarControlActivity
                carActivity.sendCMD(carActivity.music_volume_down)
                val volume_value = "Current volume is " + progressBar.progress
                showToast(volume_value)
            }
        })
        btn_volume_add!!.setOnClickListener(View.OnClickListener {
            if (music_volume_value < progressBar.max) {
                music_volume_value++
                progressBar.progress = music_volume_value
                val carActivity = this@CarControlActivity
                carActivity.sendCMD(carActivity.music_volume_up)
                val volume_value = "Current volume is " + progressBar.progress
                showToast(volume_value)
            }
        })
    }

    private fun Direction_setup_Dialog() {
        val inflate = layoutInflater.inflate(R.layout.car_direction_setup, null)
        val editText_forward = inflate.findViewById<View>(R.id.editText_forward) as EditText
        val editText_back = inflate.findViewById<View>(R.id.editText_back) as EditText
        val editText_left = inflate.findViewById<View>(R.id.editText_left) as EditText
        val editText_right = inflate.findViewById<View>(R.id.editText_right) as EditText
        val editText_a = inflate.findViewById<View>(R.id.editText_a) as EditText
        val editText_b = inflate.findViewById<View>(R.id.editText_b) as EditText
        val editText_x = inflate.findViewById<View>(R.id.editText_x) as EditText
        val editText_y = inflate.findViewById<View>(R.id.editText_y) as EditText
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_setting)
            .setTitle(resources.getString(R.string.direction_cmd))
            .setView(inflate)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                val obj = editText_forward.text.toString()
                if (obj.isNotEmpty()) {
                    this.CAR_FORWARD!!.setLength(0)
                    this.CAR_FORWARD!!.append(obj)
                    shareData!!.edit().putString("Forward", obj).commit()
                }
                val obj2 = editText_back.text.toString()
                if (obj2.isNotEmpty()) {
                    this.CAR_BACK!!.setLength(0)
                    this.CAR_BACK!!.append(obj2)
                    shareData!!.edit().putString("Back", obj2).commit()
                }
                val obj3 = editText_left.text.toString()
                if (obj3.isNotEmpty()) {
                    this.CAR_LEFT!!.setLength(0)
                    this.CAR_LEFT!!.append(obj3)
                    shareData!!.edit().putString("Left", obj3).commit()
                }
                val obj4 = editText_right.text.toString()
                if (obj4.isNotEmpty()) {
                    this.CAR_RIGHT!!.setLength(0)
                    this.CAR_RIGHT!!.append(obj4)
                    shareData!!.edit().putString("Right", obj4).commit()
                }
                val obj5 = editText_a.text.toString()
                if (obj5.isNotEmpty()) {
                    this.CAR_A_Func!!.setLength(0)
                    this.CAR_A_Func!!.append(obj5)
                    shareData!!.edit().putString("A_button", obj5).commit()
                }
                val obj6 = editText_b.text.toString()
                if (obj6.isNotEmpty()) {
                    this.CAR_B_Func!!.setLength(0)
                    this.CAR_B_Func!!.append(obj6)
                    shareData!!.edit().putString("B_button", obj6).commit()
                }
                val obj7 = editText_x.text.toString()
                if (obj7.isNotEmpty()) {
                    this.CAR_X_Func!!.setLength(0)
                    this.CAR_X_Func!!.append(obj7)
                    shareData!!.edit().putString("X_button", obj7).commit()
                }
                val obj8 = editText_y.text.toString()
                if (obj8.isNotEmpty()) {
                    this.CAR_Y_Func!!.setLength(0)
                    this.CAR_Y_Func!!.append(obj8)
                    shareData!!.edit().putString("Y_button", obj8).commit()
                }
                dialog.dismiss()
            }
            .setNegativeButton(
                resources.getString(R.string.cancel)
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    private fun Car_CMD_List_Dialog() {
        val sb = java.lang.StringBuilder()
        val string = shareData!!.getString("Forward", "f")
        sb.append("Forward  : $string\n")
        val string2 = shareData!!.getString("Back", "b")
        sb.append("Back        : $string2\n")
        val string3 = shareData!!.getString("Left", "l")
        sb.append("Left          : $string3\n")
        val string4 = shareData!!.getString("Right", "r")
        sb.append("Right        : $string4\n")
        val string5 = shareData!!.getString("Stop", "p")
        sb.append("Stop         : $string5\n")
        val string6 = shareData!!.getString("A_button", "a")
        sb.append("A               : $string6\n")
        val string7 = shareData!!.getString("B_button", "d")
        sb.append("B               : $string7\n")
        val string8 = shareData!!.getString("X_button", "x")
        sb.append("X               : $string8\n")
        val string9 = shareData!!.getString("Y_button", "y")
        sb.append("Y               : $string9\n\n")
        sb.append("Song 1    : 1 \n")
        sb.append("Song 2    : 2 \n")
        sb.append("Song 3    : 3 \n")
        sb.append("Song 4    : 4 \n")
        sb.append("Song 5    : 5 \n")
        sb.append("Song 6    : 6 \n")
        sb.append("Song 7    : 7 \n")
        sb.append("Song 8    : 8 \n")
        sb.append("Song 9    : 9 \n")
        sb.append("Song 10  : 0 \n\n")
        val string10 = shareData!!.getString("music_play_btn", "S")
        sb.append("Music play          : $string10\n")
        sb.append("Music pause       : $music_pause\n")
        val string11 = shareData!!.getString("music_previous_btn", "P")
        sb.append("Music previous  : $string11\n")
        val string12 = shareData!!.getString("music_next_btn", "N")
        sb.append("Music next          : $string12\n")
        sb.append("Music volume up       : $music_volume_up\n")
        sb.append("Music volume down : $music_volume_down\n")

        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_list)
            .setTitle(resources.getString(R.string.cmd_title))
            .setMessage(sb.toString())
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }
    private fun selectMelody_Dialog() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_car_music)
            .setTitle(resources.getString(R.string.music_title))
            .setItems(songArrayData) { dialog, i ->
                if (i == 0) {
                    songCmd = "1"
                } else if (i == 1) {
                    songCmd = "2"
                } else if (i == 2) {
                    songCmd = "3"
                } else if (i == 3) {
                    songCmd = "4"
                } else if (i == 4) {
                    songCmd = "5"
                } else if (i == 5) {
                    songCmd = "6"
                } else if (i == 6) {
                    songCmd = "7"
                } else if (i == 7) {
                    songCmd = "8"
                } else if (i == 8) {
                    songCmd = "9"
                } else if (i == 9) {
                    songCmd = "0"
                }
                val carActivity = this@CarControlActivity
                carActivity.sendCMD(carActivity.songCmd!!)
                music_flag = true
                dialog.dismiss()
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }
    private fun Instruction_Dialog() {
        MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.ic_help)
            .setTitle(resources.getString(R.string.operation_title))
            .setMessage(resources.getString(R.string.car_control_instruction))
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to positive button press
                dialog.dismiss()
            }
            .show()
    }
    private fun sendCMD(str: String) {
        if (mChatService!!.state !== 3 || str.length <= 0) {
            return
        }
        mChatService!!.write(str.toByteArray())
    }
    private fun showToast(msg: String) {
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        if (mChatService != null) {
            mChatService!!.stop()
            mChatService = null
        }
    }
}