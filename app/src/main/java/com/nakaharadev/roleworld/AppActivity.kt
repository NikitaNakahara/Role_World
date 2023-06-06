package com.nakaharadev.roleworld

import android.R.attr.label
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream


class AppActivity : Activity() {
    private var network: Network? = null

    private val GET_USER_AVATAR = 0

    private var authorized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        network = Network()
        network?.setNetworkCallback(object : Network.NetworkCallback {
            override fun onConnected() {}

            override fun onGetMessage(msg: Message) {
                val json = JSONObject(msg.toString())

                if (json.getJSONObject("data")["state"] as String == "success") {
                    if (json["id"] as String == UserData.ID) {
                        authorized = true
                    }
                }
            }

            override fun onSendMessage(msg: Message) {
                runOnUiThread { Toast.makeText(this@AppActivity, msg.toString(), Toast.LENGTH_SHORT).show() }
            }
        })
        network?.run()

        authUser()

        initMainAppView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (intent != null) {
                if (requestCode == GET_USER_AVATAR) {
                    val uri = data?.data
                    UserData.AVATAR = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    findViewById<ImageView>(R.id.settings_user_avatar).setImageBitmap(UserData.AVATAR)
                }
            }
        }
    }

    private fun authUser() {
        val msg = Message()
        msg.setRequestType("auth")
        msg.setRequestMode("sign_in")

        val msgData = JSONObject()
        msgData.put("email", UserData.EMAIL)
        msgData.put("password", UserData.PASSWORD)

        msg.setData(msgData)

        //Toast.makeText(this, msg.toString(), Toast.LENGTH_SHORT).show()

        network?.sendMsg(msg)
    }

    private fun initMainAppView() {
        setContentView(R.layout.main_menu)

        initUserProfile()
        if (UserData.AVATAR != null) {
            saveAvatarToFile("/user_avatar.png", UserData.AVATAR!!)
        }
        initMenu()
    }

    private fun initUserProfile() {
        val idView = findViewById<TextView>(R.id.app_menu_user_id)
        val spannableString = SpannableString(UserData.ID)
        spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
        idView.text = spannableString

        idView.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label.toString(), idView.text)
            clipboard.setPrimaryClip(clip)
        }

        if (UserData.AVATAR != null) {
            findViewById<ImageView>(R.id.app_menu_user_avatar).setImageBitmap(UserData.AVATAR)
        }
        findViewById<TextView>(R.id.app_menu_user_nickname).text = UserData.NICKNAME
        findViewById<TextView>(R.id.app_menu_user_email).text = UserData.EMAIL

        findViewById<ImageView>(R.id.user_profile_settings).setOnClickListener {
            Menu.hiddenMenu()
            initUserProfileSettings()
        }
    }

    private fun initUserProfileSettings() {
        setContentView(R.layout.profile_settings)

        findViewById<ImageView>(R.id.settings_back_btn).setOnClickListener {
            rewriteConfig()

            initMainAppView()
        }

        if (UserData.AVATAR != null) {
            findViewById<ImageView>(R.id.settings_user_avatar).setImageBitmap(UserData.AVATAR)
        }

        findViewById<EditText>(R.id.settings_nickname_input).setText(UserData.NICKNAME)
        findViewById<EditText>(R.id.settings_user_email).setText(UserData.EMAIL)
        findViewById<TextView>(R.id.settings_user_id).text = UserData.ID

        findViewById<ImageView>(R.id.settings_edit_nickname).setOnClickListener {
            val edit = findViewById<EditText>(R.id.settings_nickname_input)

            it.visibility = View.GONE

            edit.isFocusable = true
            edit.isFocusableInTouchMode = true
            edit.isClickable = true

            edit.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT)

            edit.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    UserData.NICKNAME = v?.text?.toString().toString()

                    it.visibility = View.VISIBLE
                    edit.isFocusable = false
                    edit.isFocusableInTouchMode = false
                    edit.isClickable = false

                    imm.hideSoftInputFromWindow(edit.windowToken, 0)

                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }
        }

        findViewById<ImageView>(R.id.settings_edit_email).setOnClickListener {
            val edit = findViewById<EditText>(R.id.settings_user_email)

            it.visibility = View.GONE
            edit.isFocusable = true
            edit.isFocusableInTouchMode = true
            edit.isClickable = true

            edit.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT)

            edit.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    UserData.EMAIL = v?.text?.toString().toString()

                    it.visibility = View.VISIBLE
                    edit.isFocusable = false
                    edit.isFocusableInTouchMode = false
                    edit.isClickable = false

                    imm.hideSoftInputFromWindow(edit.windowToken, 0)

                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }
        }

        findViewById<ImageView>(R.id.settings_edit_user_avatar).setOnClickListener {
            intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, GET_USER_AVATAR)
        }
    }

    private fun initMenu() {
        Menu.setContext(this)
        Menu.setMenuLayout(findViewById(R.id.app_menu_layout))
        Menu.setDarkening(findViewById(R.id.darkening))

        Menu.setMenuCallback(object: Menu.MenuCallback() {
            override fun onMenuButtonPressed(id: Int) {
                Menu.hiddenMenu()
            }

            override fun onMenuOpen() {
                findViewById<View>(R.id.darkening).setOnClickListener {
                    Menu.hiddenMenu()
                }
            }

            override fun onMenuHidden() {
                findViewById<View>(R.id.darkening).isClickable = false
                findViewById<View>(R.id.darkening).setOnClickListener(null)
            }
        })

        val scrolls = ArrayList<ScrollView>()
        scrolls.add(findViewById(R.id.worlds_scroll))
        Menu.setScrollViews(scrolls)

        Menu.setMenuBtn(findViewById(R.id.open_close_menu_btn))
        findViewById<ImageView>(R.id.open_close_menu_btn).setOnClickListener {
            if (Menu.isShowed()) {
                Menu.hiddenMenu()
            } else {
                Menu.showMenu()
            }
        }

        Menu.initMoveMenuListener()
    }

    private fun saveAvatarToFile(name: String, avatar: Bitmap) {
        val bos = ByteArrayOutputStream()
        avatar.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapData = bos.toByteArray()

        val fos = FileOutputStream(filesDir.path + name)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
    }

    private fun rewriteConfig() {
        val file = File(filesDir.path + "/main.conf")
        file.createNewFile()

        val json = JSONObject()
        json.put("nickname", UserData.NICKNAME)
        json.put("email", UserData.EMAIL)
        json.put("password", UserData.PASSWORD)
        json.put("id", UserData.ID)
        json.put("lang", UserData.LANG)
        if (UserData.AVATAR != null) {
            json.put("avatar", "/user_avatar.png")
        } else {
            json.put("avatar", "null")
        }

        val output = DataOutputStream(FileOutputStream(file, false))
        output.writeUTF(json.toString())
        output.flush()
    }
}