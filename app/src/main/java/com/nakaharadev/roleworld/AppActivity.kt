package com.nakaharadev.roleworld

import android.R.attr.label
import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
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
import android.widget.ViewFlipper
import com.google.android.material.imageview.ShapeableImageView
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Base64


class AppActivity : Activity() {
    private var network: Network? = null

    private val GET_USER_AVATAR = 0
    private val GET_CONSTRUCTOR_AVATAR = 1

    private val WORLDS_LAYOUT = 0
    private val CHARACTERS_LAYOUT = 1

    private var openedLayout = WORLDS_LAYOUT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        network = Network()

        network?.setNetworkCallback(object : Network.NetworkCallback {
            override fun onConnected() {}

            override fun onGetMessage(msg: Message) {}
        })
        network?.run()

        Database.init(this)

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

                if (requestCode == GET_CONSTRUCTOR_AVATAR) {
                    val uri = data?.data
                    findViewById<ImageView>(R.id.constructor_avatar).setImageBitmap(
                        MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            uri
                        )
                    )
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

        network?.sendMessageAndAddCallback(msg, "auth") {
            UserData.authorized = true
        }
    }

    private fun initMainAppView() {
        setContentView(R.layout.main_menu)

        initUserProfile()

        if (UserData.AVATAR != null) {
            saveAvatarToFile("/user_avatar.png", UserData.AVATAR!!)
        }

        initMenu()

        val characters = Database.getAllCharacters(isInit = true)
        if (characters != null) {
            for (character in characters) {
                UserData.CHARACTERS.add(character)
            }
        }

        for (character in UserData.CHARACTERS) {
            val elem = LayoutInflater.from(this).inflate(R.layout.character, null)
            val container = findViewById<LinearLayout>(R.id.characters_layout)

            elem.findViewById<ShapeableImageView>(R.id.character_avatar)
                .setImageBitmap(character.getAvatar())
            elem.findViewById<TextView>(R.id.character_name).text =
                character.getDataField(resources.getString(R.string.input_nickname_hint))

            container.removeView(findViewById(R.id.not_characters))
            container.addView(elem)
        }

        findViewById<ImageView>(R.id.add).setOnClickListener {
            var currentMode = openedLayout

            if (openedLayout == CHARACTERS_LAYOUT) {
                findViewById<TextView>(R.id.constructor_selector_character).setBackgroundResource(R.drawable.constructor_selected_cursor)
                findViewById<TextView>(R.id.constructor_selector_world).setBackgroundColor(
                    Color.parseColor(
                        "#00000000"
                    )
                )
            } else {
                findViewById<TextView>(R.id.constructor_selector_world).setBackgroundResource(R.drawable.constructor_selected_cursor)
                findViewById<TextView>(R.id.constructor_selector_character).setBackgroundColor(
                    Color.parseColor(
                        "#00000000"
                    )
                )
            }

            findViewById<TextView>(R.id.constructor_selector_character).setOnClickListener {
                findViewById<TextView>(R.id.constructor_selector_character).setBackgroundResource(R.drawable.constructor_selected_cursor)
                findViewById<TextView>(R.id.constructor_selector_world).setBackgroundColor(
                    Color.parseColor(
                        "#00000000"
                    )
                )

                currentMode = CHARACTERS_LAYOUT
            }

            findViewById<TextView>(R.id.constructor_selector_world).setOnClickListener {
                findViewById<TextView>(R.id.constructor_selector_world).setBackgroundResource(R.drawable.constructor_selected_cursor)
                findViewById<TextView>(R.id.constructor_selector_character).setBackgroundColor(
                    Color.parseColor(
                        "#00000000"
                    )
                )

                currentMode = WORLDS_LAYOUT
            }

            val constructor = findViewById<RelativeLayout>(R.id.constructor)

            constructor.visibility = LinearLayout.VISIBLE

            val darkening = findViewById<View>(R.id.darkening)
            darkening.isClickable = true
            darkening.setOnClickListener {
                val animator = ValueAnimator.ofFloat(1.0f, 0.0f)
                animator.duration = 300
                animator.addUpdateListener {
                    constructor.scaleX = it.animatedValue as Float
                    constructor.scaleY = it.animatedValue as Float
                    darkening.alpha = it.animatedValue as Float / 2.0f
                }
                animator.start()

                darkening.isClickable = false
            }

            val animator = ValueAnimator.ofFloat(0.0f, 1.0f)
            animator.duration = 300
            animator.addUpdateListener {
                constructor.scaleX = it.animatedValue as Float
                constructor.scaleY = it.animatedValue as Float
                darkening.alpha = it.animatedValue as Float / 2.0f
            }
            animator.start()

            findViewById<ShapeableImageView>(R.id.constructor_avatar).setOnClickListener {
                intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                startActivityForResult(intent, GET_CONSTRUCTOR_AVATAR)
            }

            findViewById<LinearLayout>(R.id.constructor_add_field).setOnClickListener {
                val elem = LayoutInflater.from(this).inflate(R.layout.constructor_field, null)
                findViewById<LinearLayout>(R.id.constructor_fields_layout).addView(
                    elem,
                    findViewById<LinearLayout>(R.id.constructor_fields_layout).childCount - 1
                )

                findViewById<ScrollView>(R.id.constructor_fields_scroll).post {
                    findViewById<ScrollView>(
                        R.id.constructor_fields_scroll
                    ).fullScroll(ScrollView.FOCUS_DOWN)
                }
            }

            findViewById<TextView>(R.id.constructor_done).setOnClickListener {
                if (currentMode == CHARACTERS_LAYOUT) {
                    val character = Character()

                    val layout = findViewById<LinearLayout>(R.id.constructor_fields_layout)

                    var nameIsExists = false
                    for (i in 0 until layout.childCount - 1) {
                        if (layout.getChildAt(i)
                                .findViewById<TextView>(R.id.constructor_field_title).text.toString()
                                .lowercase() == resources.getString(R.string.input_nickname_hint)
                        ) {
                            nameIsExists = true
                        }
                    }

                    if (nameIsExists) {
                        for (i in 0 until layout.childCount - 1) {
                            if (layout.getChildAt(i)
                                    .findViewById<TextView>(R.id.constructor_field_title).text.toString()
                                    .lowercase() == resources.getString(R.string.input_nickname_hint)
                            ) {
                                character.addDataField(
                                    layout.getChildAt(i)
                                        .findViewById<TextView>(R.id.constructor_field_title).text.toString()
                                        .lowercase(),
                                    layout.getChildAt(i)
                                        .findViewById<TextView>(R.id.constructor_field_data).text.toString()
                                )
                            } else {
                                character.addDataField(
                                    layout.getChildAt(i)
                                        .findViewById<TextView>(R.id.constructor_field_title).text.toString(),
                                    layout.getChildAt(i)
                                        .findViewById<TextView>(R.id.constructor_field_data).text.toString()
                                )
                            }
                        }
                    }

                    character.setAvatar((findViewById<ShapeableImageView>(R.id.constructor_avatar).drawable as BitmapDrawable).bitmap)

                    val message = Message()
                    message.setUserId(UserData.ID)
                    message.setRequestType("character")
                    message.setRequestMode("add")
                    message.setData(character.toString())

                    network?.sendMessageAndAddCallback(message, "add_character") {
                        runOnUiThread {
                            character.setID(message.getUserId())
                            UserData.CHARACTERS.add(character)

                            val elem =
                                LayoutInflater.from(this).inflate(R.layout.character, null) as LinearLayout
                            elem.findViewById<ShapeableImageView>(R.id.character_avatar)
                                .setImageBitmap(character.getAvatar())
                            elem.findViewById<TextView>(R.id.character_name).text =
                                character.getDataField(resources.getString(R.string.input_nickname_hint))


                            val container = findViewById<LinearLayout>(R.id.characters_layout)
                            container.removeView(findViewById(R.id.not_characters))
                            container.addView(elem)

                            Database.addCharacter(character)
                        }
                    }
                }

                val animator = ValueAnimator.ofFloat(1.0f, 0.0f)
                animator.duration = 300
                animator.addUpdateListener {
                    constructor.scaleX = it.animatedValue as Float
                    constructor.scaleY = it.animatedValue as Float
                    darkening.alpha = it.animatedValue as Float / 2.0f
                }
                animator.start()

                darkening.isClickable = false
            }
        }
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

        for (character in UserData.CHARACTERS) {
            val elem = LayoutInflater.from(this).inflate(R.layout.character, null)
            val container = findViewById<LinearLayout>(R.id.user_profile_settings_characters_layout)

            elem.findViewById<ImageView>(R.id.character_avatar)
                .setImageBitmap(character.getAvatar())
            elem.findViewById<TextView>(R.id.character_name).text = character.getDataField(resources.getString(R.string.input_nickname_hint))

            container.removeView(findViewById(R.id.user_profile_settings_characters_layout_empty))
            container.addView(elem)
        }

        findViewById<TextView>(R.id.user_profile_settings_account_exit).setOnClickListener {
            val dialog =
                findViewById<RelativeLayout>(R.id.user_profile_settings_account_exit_dialog)
            val darkening = findViewById<View>(R.id.profile_settings_darkening)
            var animator = ValueAnimator.ofFloat(0.0f, 1.0f)
            dialog.visibility = View.VISIBLE
            darkening.isClickable = true

            animator.addUpdateListener {
                dialog.scaleX = it.animatedValue as Float
                dialog.scaleY = it.animatedValue as Float
                darkening.alpha = (it.animatedValue as Float) / 2
            }
            animator.start()

            (dialog.getChildAt(1) as LinearLayout).getChildAt(0).setOnClickListener {
                darkening.isClickable = false
                animator = ValueAnimator.ofFloat(1.0f, 0.0f)
                animator.addUpdateListener {
                    dialog.scaleX = it.animatedValue as Float
                    dialog.scaleY = it.animatedValue as Float
                    darkening.alpha = (it.animatedValue as Float) / 2
                }
                animator.addListener(object : AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        dialog.visibility = View.GONE
                        darkening.isClickable = false
                    }
                })
                animator.start()
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
                updatedValueData =
                    updateField(updatedValue, it.text?.toString().toString(), it, imm)
            }

            it.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updatedValueData =
                        updateField(updatedValue, it.text?.toString().toString(), it, imm)

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
                updatedValueData =
                    updateField(updatedValue, it.text?.toString().toString(), it, imm)
            }

            it.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updatedValueData =
                        updateField(updatedValue, it.text?.toString().toString(), it, imm)

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

        findViewById<TextView>(R.id.settings_user_id).setOnLongClickListener {
            it as TextView

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("User ID", it.text.toString())
            clipboard.setPrimaryClip(clip)

            return@setOnLongClickListener true
        }
    }

    private fun updateField(
        fieldName: String,
        fieldData: String,
        it: EditText,
        imm: InputMethodManager
    ): String {
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

        Menu.setMenuCallback(object : Menu.MenuCallback() {
            override fun onMenuButtonPressed(id: Int) {
                if (id == R.id.app_menu_characters_btn) openedLayout = CHARACTERS_LAYOUT
                if (id == R.id.app_menu_worlds_btn) openedLayout = WORLDS_LAYOUT

                findViewById<ViewFlipper>(R.id.menu_flipper).displayedChild = openedLayout

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