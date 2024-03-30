package com.example.iot_kotlin

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Calendar


class EditProfileFragment: Fragment() {
    /*  About ToolBar */
    private lateinit var toolbar: Toolbar
    /* EditText */
    private lateinit var nicknameTextInputLayout: TextInputLayout
    private lateinit var editNickname: EditText
    /* TextView */
    private lateinit var genderTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var bornYearTextView: TextView
    private lateinit var bornDateTextView: TextView
    /* Button */
    private lateinit var saveProfileButton: Button
    private lateinit var saveProfileButtonCardView: CardView
    /* LinearLayout */
    private lateinit var genderLinearLayout: LinearLayout
    private lateinit var countryLinearLayout: LinearLayout
    private lateinit var bornLinearLayout: LinearLayout
    /* String */
    private lateinit var currentNickname: String
    private lateinit var currentGender: String
    private lateinit var currentCountry: String
    private lateinit var currentBorndate: String
    private lateinit var currentBornYear: String
    private lateinit var currentBornDate: String
    /* ImageView */
    private lateinit var uploadImage: CircleImageView
    private lateinit var uploadIcon: ImageView
    /* Firebase */
    private var currentUserUID: String? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imgReference: StorageReference
    private var imageUri: Uri? = null
    private lateinit var imgURL: String
    /* List */
    private val countryList = listOf<String>(
        "未選択", "アイスランド", "アイルランド", "アメリカ", "イギリス", "イタリア",
        "ウクライナ", "オーストラリア", "オーストリア", "カナダ", "サウジアラビア",
        "ジョージア", "シンガポール", "スイス", "韓国", "中国", "台湾",
        "日本", "ニュージーランド", "フィンランド", "南アフリカ", "ロシア",
        "其他"
    )
    /* checkProfileChange Loop */
    private val handler = Handler(Looper.myLooper()!!)
    private val checkProfileChange = Runnable {
        if((editNickname.text.toString() == currentNickname || editNickname.text.isEmpty()) &&
            genderTextView.text.toString() == currentGender &&
            countryTextView.text.toString() == currentCountry &&
            bornYearTextView.text.toString() == currentBornYear &&
            bornDateTextView.text.toString() == currentBornDate &&
            imageUri == null ) {
            saveProfileButton.isEnabled = false
            saveProfileButtonCardView.visibility = View.VISIBLE
        } else {
            saveProfileButton.isEnabled = true
            saveProfileButtonCardView.visibility = View.GONE
        }
    }
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageUri = data?.data
            uploadImage.setImageURI(imageUri)
        } else {
            Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show()
        }
        handler.post(checkProfileChange)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        setToolbar()
        textChangedListener()
        firebaseInit()
        layoutClickListener()
        buttonClickListener()
        imageClickListener()
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        nicknameTextInputLayout = view.findViewById(R.id.nicknameTextInputLayout)
        editNickname = view.findViewById(R.id.editNickname)
        genderTextView = view.findViewById(R.id.genderTextView)
        countryTextView = view.findViewById(R.id.countryTextView)
        bornYearTextView = view.findViewById(R.id.bornYearTextView)
        bornDateTextView = view.findViewById(R.id.bornDateTextView)
        saveProfileButton = view.findViewById(R.id.saveProfileButton)
        saveProfileButtonCardView = view.findViewById(R.id.saveProfileButtonCardView)
        uploadImage = view.findViewById(R.id.uploadImage)
        uploadIcon = view.findViewById(R.id.uploadIcon)
        genderLinearLayout = view.findViewById(R.id.genderLinearLayout)
        countryLinearLayout = view.findViewById(R.id.countryLinearLayout)
        bornLinearLayout = view.findViewById(R.id.bornLinearLayout)
    }
    private fun setToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.edit_profile)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            returnProfileFragment()
        }
    }
    private fun firebaseInit() {
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        /** Show progress indicators **/
        val builder = AlertDialog.Builder(requireContext())
        val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
        builder.setView(dialogView)
        val dialog: AlertDialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        /** 抓取 Firebase 的資料 */
        if (currentUser != null) {
            currentUserUID = currentUser!!.uid
            reference = FirebaseDatabase.getInstance().getReference("users/UID/$currentUserUID") //獲取當前使用者的資料路徑
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        dialog.dismiss()
                        val nickname = snapshot.child("nickname").getValue(String::class.java)
                        val gender = snapshot.child("gender").getValue(String::class.java)
                        val country = snapshot.child("country").getValue(String::class.java)
                        val borndate = snapshot.child("borndate").getValue(String::class.java)
                        if(snapshot.child("avatar").exists()) {
                            val avatar = snapshot.child("avatar").getValue(String::class.java)
                            /* Image Show */
                            avatar?.let { imageUrl ->
                                Glide.with(requireContext())
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_person_circle_bg) // 設置占位符，當圖片加載時顯示
                                    .error(R.drawable.ic_person_circle_bg) // 設置加載錯誤時顯示的圖片
                                    .into(uploadImage)
                            }
                        }
                        /* Data Initial */
                        val initValue = resources.getString(R.string.unselect)
                        currentNickname = nickname!!
                        currentGender = gender!!
                        currentCountry = country!!
                        currentBorndate = borndate!!
                        /** Information Show **/
                        editNickname.setText(nickname)
                        genderTextView.text = gender
                        countryTextView.text = country
                        if(currentBorndate == initValue) {
                            currentBornYear = currentBorndate
                            currentBornDate = currentBorndate
                        } else {
                            currentBornYear = currentBorndate.substring(0, minOf(currentBorndate.length, 4))
                            currentBornDate = currentBorndate.substring(5, currentBorndate.length)
                        }
                        bornYearTextView.text = currentBornYear
                        bornDateTextView.text = currentBornDate

                        saveProfileButtonCardView.visibility = View.VISIBLE
                        saveProfileButton.isEnabled = false
                    } else {
                        // 處理資料不存在的情況
                        dialog.dismiss()
                        CustomSnackbar.showSnackbar(getView(), requireContext(), "User data not found")
                        //showToast("User data not found")
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // 處理讀取資料失敗的情況
                    dialog.dismiss()
                    CustomSnackbar.showSnackbar(getView(), requireContext(), "Error: ${databaseError.message}")
                    //showToast("Error: ${databaseError.message}")
                }
            })
        }
    }
    private fun textChangedListener() {
        /**  Text Changed Listener **/
        editNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val nickname = s.toString()
                when {
                    s.isNullOrEmpty() -> {
                        nicknameTextInputLayout.error = null
                        editNickname.error = getString(R.string.nickname_empty)
                    }
                    charCount in 1..2 -> {
                        nicknameTextInputLayout.error = getString(R.string.errorUsername_message)
                    }
                    nickname == currentNickname -> {
                        nicknameTextInputLayout.error = null
                    }
                    else -> {
                        nicknameTextInputLayout.error = null
                    }
                }
                handler.post(checkProfileChange)
            }
        })
    }
    private fun imageClickListener() {
        uploadImage.setOnClickListener(imageOnClick())
        uploadIcon.setOnClickListener(imageOnClick())
    }
    private fun imageOnClick(): View.OnClickListener? {
        return View.OnClickListener {
            val view = it as? View
            when(view?.id) {
                R.id.uploadImage -> {
                    imageClickAction()
                }
                R.id.uploadIcon -> {
                    imageClickAction()
                }
            }
        }
    }
    private fun imageClickAction() {
        val photoPicker = Intent()
        photoPicker.action = Intent.ACTION_GET_CONTENT
        photoPicker.type = "image/*"
        activityResultLauncher.launch(photoPicker)
    }
    private fun buttonClickListener() {
        saveProfileButton.setOnClickListener{
            handler.removeCallbacksAndMessages(null)
            firebaseUpdate()
        }
    }
    private fun firebaseUpdate() {
        if (currentUser != null) {
            if(editNickname.text.isNotEmpty() && editNickname.text.toString().length >= 3) {
                /** Show progress indicators **/
                val builder = AlertDialog.Builder(requireContext())
                val dialogView: View = layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                builder.setView(dialogView)
                val dialog: AlertDialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                /** Prepare Data **/
                val newNickname = editNickname.text.toString()
                val newGender = genderTextView.text.toString()
                val newCountry = countryTextView.text.toString()
                val newBorndate = if(bornYearTextView.text.toString() == getString(R.string.unselect)) {
                    bornYearTextView.text.toString()
                } else {
                    bornYearTextView.text.toString()+"-"+bornDateTextView.text.toString()
                }
                if(imageUri != null) {
                    firebaseUpdateImg(imageUri!!)
                }
                val newData = mapOf<String, Any>(
                    "nickname" to newNickname,
                    "gender" to newGender,
                    "country" to newCountry,
                    "borndate" to newBorndate
                )
                /** Update User Data **/
                reference.updateChildren(newData)
                    .addOnSuccessListener {
                        CustomSnackbar.showSnackbar(getView(), requireContext(), getString(R.string.data_update_success))
                        //showToast(getString(R.string.data_update_success))
                    }
                    .addOnFailureListener { _ ->
                        CustomSnackbar.showSnackbar(getView(), requireContext(), getString(R.string.data_update_failed))
                        //showToast(getString(R.string.data_update_failed))
                    }
                Handler(Looper.myLooper()!!).postDelayed({
                    dialog.dismiss()
                    returnProfileFragment()
                }, 1500)
            } else {
                CustomSnackbar.showSnackbar(getView(), requireContext(), getString(R.string.nickname_error))
                //showToast(getString(R.string.nickname_error))
            }
        }
    }

    private fun firebaseUpdateImg(uri: Uri) {
        storageReference = FirebaseStorage.getInstance().getReference("$currentUserUID")
        imgReference = storageReference.child("headImage.${getFileExtension(uri)}")
        imgReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                imgReference.downloadUrl
                    .addOnSuccessListener { uri ->
                        imgURL = uri.toString()
                        val newImgData = mapOf<String, Any>("avatar" to imgURL)
                        reference.updateChildren(newImgData)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { _ ->
                                CustomSnackbar.showSnackbar(getView(), requireContext(), getString(R.string.data_update_failed))
                            }
                    }
            }
            .addOnFailureListener { e ->

            }
    }
    private fun getFileExtension(fileUri: Uri?): String? {
        val contentResolver = requireContext().contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri!!))
    }

    /* Profile Info Select */
    private fun layoutClickListener()  {
        genderLinearLayout.setOnClickListener(linearLayoutOnClickListener())
        countryLinearLayout.setOnClickListener(linearLayoutOnClickListener())
        bornLinearLayout.setOnClickListener(linearLayoutOnClickListener())
    }
    private fun linearLayoutOnClickListener() : View.OnClickListener?  {
        return View.OnClickListener {
            val view = it as? View
            when(view?.id) {
                R.id.genderLinearLayout -> {
                    showBottomDialogGender()
                }
                R.id.countryLinearLayout -> {
                    showBottomDialogCountry()
                }
                R.id.bornLinearLayout -> {
                    showBottomDialogBorn()
                }
            }
        }
    }
    private fun showBottomDialogGender() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout_gender)
        val closeTextView = dialog.findViewById<TextView>(R.id.closeTextView)
        val unselectTextView = dialog.findViewById<TextView>(R.id.unselectTextView)
        val maleTextView = dialog.findViewById<TextView>(R.id.maleTextView)
        val femaleTextView = dialog.findViewById<TextView>(R.id.femaleTextView)
        closeTextView.setOnClickListener{
            dialog.dismiss()
        }
        unselectTextView.setOnClickListener{
            dialog.dismiss()
            genderTextView.text = unselectTextView.text
            handler.post(checkProfileChange)
        }
        maleTextView.setOnClickListener{
            dialog.dismiss()
            genderTextView.text = maleTextView.text
            handler.post(checkProfileChange)
        }
        femaleTextView.setOnClickListener{
            dialog.dismiss()
            genderTextView.text = femaleTextView.text
            handler.post(checkProfileChange)
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
    private fun showBottomDialogCountry() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout_country)
        val closeTextView = dialog.findViewById<TextView>(R.id.closeTextView)
        val country_RecyclerView = dialog.findViewById<RecyclerView>(R.id.country_RecyclerView)
        val countryList = ArrayList(countryList)
        /* RecyclerView */
        country_RecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val bottomRecyclerViewAdapter = BottomRecyclerViewAdapter(countryList)
        country_RecyclerView!!.adapter = bottomRecyclerViewAdapter
        ItemClickSupport.addTo(country_RecyclerView)
        country_RecyclerView.onItemClick{ recyclerView, position, _ ->
            dialog.dismiss()
            val itemString = (recyclerView.adapter as BottomRecyclerViewAdapter).getItem(position)
            countryTextView.text = itemString
            handler.post(checkProfileChange)
        }
        /////////////////////////////////////////////////////////
        closeTextView.setOnClickListener{
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
    private fun showBottomDialogBorn() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout_born)
        val closeTextView = dialog.findViewById<TextView>(R.id.closeTextView)
        val datePicker = dialog.findViewById<DatePicker>(R.id.datePicker)
        closeTextView.setOnClickListener{
            dialog.dismiss()
        }
        /* Date Initial */
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        var selectYear : String? = currentBornYear
        var selectDate : String? = currentBornDate
        datePicker.init(year, month, day) { _, year, month, day ->
            selectYear = year.toString()
            selectDate = makeDateString(day, month+1)
        }
        /* When dialog dismiss */
        dialog.setOnDismissListener{
            bornYearTextView.text = selectYear
            bornDateTextView.text = selectDate
            handler.post(checkProfileChange)
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }
    private fun makeDateString(day: Int, month: Int): String? {
        return getMonthFormat(month) + "-" + day
    }
    private fun getMonthFormat(month: Int): String? {
        return when(month) {
            1 -> "01"
            2 -> "02"
            3 -> "03"
            4 -> "04"
            5 -> "05"
            6 -> "06"
            7 -> "07"
            8 -> "08"
            9 -> "09"
            10 -> "10"
            11 -> "11"
            12 -> "12"
            else -> "01"
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
    private fun returnProfileFragment() {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentManager.popBackStack()
        fragmentTransaction.commit()
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}

