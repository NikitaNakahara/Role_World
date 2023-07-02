package com.nakaharadev.roleworld

import android.R.attr.label
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Base64


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

                if (JSONObject(json.getString("data"))["state"] as String == "success") {
                    if (json["id"] as String == UserData.ID) {
                        authorized = true
                    }
                }
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
        var updatedValue = ""
        var updatedValueData = ""

        setContentView(R.layout.profile_settings)

        findViewById<TextView>(R.id.user_profile_settings_account_exit).setOnClickListener {
            val dialog = findViewById<RelativeLayout>(R.id.user_profile_settings_account_exit_dialog)
            dialog.visibility = View.VISIBLE
            (dialog.getChildAt(1) as LinearLayout).getChildAt(0).setOnClickListener {
                dialog.visibility = View.GONE
            }
            (dialog.getChildAt(1) as LinearLayout).getChildAt(1).setOnClickListener {
                File(filesDir.path + "/main.conf").delete()
                File(filesDir.path + "/user_avatar.png").delete()

                network?.stop()

                startActivity(Intent(this, FirstStartActivity::class.java))
            }
        }

        findViewById<ImageView>(R.id.settings_back_btn).setOnClickListener {
            val updateMessage = Message()
            updateMessage.setRequestType("update")
            updateMessage.setRequestMode(updatedValue)
            updateMessage.setUserId(UserData.ID)

            val json = JSONObject()
            if (updatedValue != "avatar") {
                json.put("value", updatedValueData)
            } else {
                val byteArrayOutputStream = ByteArrayOutputStream()
                UserData.AVATAR?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                json.put("value", Base64.getEncoder().encodeToString(byteArray))
            }


            updateMessage.setData(json)

            network?.sendMsg(updateMessage)

            rewriteConfig()

            initMainAppView()
        }

        if (UserData.AVATAR != null) {
            findViewById<ImageView>(R.id.settings_user_avatar).setImageBitmap(UserData.AVATAR)
        }

        findViewById<EditText>(R.id.settings_nickname_input).setText(UserData.NICKNAME)
        findViewById<EditText>(R.id.settings_user_email).setText(UserData.EMAIL)
        findViewById<TextView>(R.id.settings_user_id).text = UserData.ID

        findViewById<EditText>(R.id.settings_nickname_input).setOnLongClickListener {
            it as EditText

            val emailEdit = findViewById<EditText>(R.id.settings_user_email)
            emailEdit.isFocusable = false
            emailEdit.isFocusableInTouchMode = false
            emailEdit.isClickable = false
            emailEdit.setBackgroundColor(Color.TRANSPARENT)

            updatedValue = "nickname"

            it.setBackgroundResource(R.drawable.editable_text)

            it.isFocusable = true
            it.isFocusableInTouchMode = true
            it.isClickable = true

            it.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)

            findViewById<LinearLayout>(R.id.profile_settings_bg).setOnClickListener { v ->
                updatedValueData = updateField(updatedValue, it.text?.toString().toString(), it, imm)
            }

            it.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updatedValueData = updateField(updatedValue, it.text?.toString().toString(), it, imm)

                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }

            return@setOnLongClickListener true
        }

        findViewById<EditText>(R.id.settings_user_email).setOnLongClickListener {
            it as EditText

            val nicknameEdit = findViewById<EditText>(R.id.settings_nickname_input)
            nicknameEdit.isFocusable = false
            nicknameEdit.isFocusableInTouchMode = false
            nicknameEdit.isClickable = false
            nicknameEdit.setBackgroundColor(Color.TRANSPARENT)


            updatedValue = "email"

            it.setBackgroundResource(R.drawable.editable_text)

            it.isFocusable = true
            it.isFocusableInTouchMode = true
            it.isClickable = true

            it.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)

            findViewById<LinearLayout>(R.id.profile_settings_bg).setOnClickListener { v ->
                updatedValueData = updateField(updatedValue, it.text?.toString().toString(), it, imm)
            }

            it.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updatedValueData = updateField(updatedValue, it.text?.toString().toString(), it, imm)

                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }

            return@setOnLongClickListener true
        }

        findViewById<ImageView>(R.id.settings_edit_user_avatar).setOnClickListener {
            updatedValue = "avatar"

            intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, GET_USER_AVATAR)
        }
    }

    private fun updateField(fieldName: String, fieldData: String, it: EditText, imm: InputMethodManager): String {
        if (fieldName == "nickname") {
            UserData.NICKNAME = fieldData

            it.isFocusable = false
            it.isFocusableInTouchMode = false
            it.isClickable = false

            imm.hideSoftInputFromWindow(it.windowToken, 0)

            it.setBackgroundColor(Color.TRANSPARENT)

            return UserData.NICKNAME
        } else if (fieldName == "email") {
            UserData.EMAIL = fieldData

            it.isFocusable = false
            it.isFocusableInTouchMode = false
            it.isClickable = false

            imm.hideSoftInputFromWindow(it.windowToken, 0)

            it.setBackgroundColor(Color.TRANSPARENT)

            return UserData.EMAIL
        }

        return ""
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