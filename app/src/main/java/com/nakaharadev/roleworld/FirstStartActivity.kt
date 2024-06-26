package com.nakaharadev.roleworld

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Base64
import java.util.Locale


class FirstStartActivity : Activity() {
    var network: Network? = null

    var authFailed = false
    var responseIsGet = false
    var responseMsg: Message? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.first_start)

        val images = ArrayList<Bitmap>()
        images.add((resources.getDrawable(R.drawable.first_start_bg) as BitmapDrawable).bitmap)
        images.add((resources.getDrawable(R.drawable.first_start_bg2) as BitmapDrawable).bitmap)
        images.add((resources.getDrawable(R.drawable.first_start_bg3) as BitmapDrawable).bitmap)

        val previewAnimator = PreviewAnimator(findViewById(R.id.preview), findViewById(R.id.preview_background), images)
        previewAnimator.start()

        initLangSelector()
        initNetwork()
    }

    private fun initNetwork() {
        network = Network()
        network?.setNetworkCallback(object: Network.NetworkCallback {
            override fun onConnected() {
                runOnUiThread {
                    findViewById<LinearLayout>(R.id.check_conn_indicator).visibility = View.GONE
                    findViewById<TextView>(R.id.preview_continue).visibility = View.VISIBLE

                    initContinueIndicator()
                }
            }

            override fun onGetMessage(msg: Message) {
                runOnUiThread { Toast.makeText(this@FirstStartActivity, msg.toString(), Toast.LENGTH_SHORT).show() }
                if (JSONObject(msg.getData()).getString("state") == "failed") {
                    authFailed = true
                } else {
                    responseMsg = msg
                }

                responseIsGet = true
            }
        })
        network?.run()
    }

    private fun initContinueIndicator() {
        val indicator = findViewById<TextView>(R.id.preview_continue)
        val animator = TextIndicatorAnimator(indicator)
        animator.start()

        findViewById<ImageView>(R.id.preview).setOnClickListener {
            animator.stop()

            findViewById<TextView>(R.id.welcome).visibility = View.GONE
            indicator.visibility = View.GONE

            startAuth()
        }
    }

    private fun startAuth() {
        findViewById<ImageView>(R.id.preview).setOnClickListener(null)

        val controller = AuthController(findViewById(R.id.auth_layout), this)
        controller.setCallback(object: AuthController.AuthCallback() {
            override fun onContinue(mode: Int, data: HashMap<String, String>) {
                if (data["state"] == "success") {
                    val message = Message()

                    message.setRequestType("auth")
                    if (mode == AuthController.SIGN_IN) {
                        message.setRequestMode("sign_in")
                    } else {
                        message.setRequestMode("sign_up")
                    }

                    val json = (data as Map<*, *>?)?.let { JSONObject(it) }
                    if (json != null) {
                        message.setData(json)
                    }

                    network?.sendMsg(message)

                    Thread {
                        while (!responseIsGet);

                        if (authFailed) {
                            runOnUiThread {
                                findViewById<RelativeLayout>(R.id.auth_layout).setBackgroundResource(R.drawable.failed_auth_bg)

                                val failMsgView = findViewById<TextView>(R.id.fail_auth_msg)
                                failMsgView.visibility = View.VISIBLE

                                if (mode == AuthController.SIGN_IN) {
                                    failMsgView.setText(R.string.sign_in_fail)
                                } else {
                                    failMsgView.setText(R.string.user_already_exists)
                                }
                            }
                        } else {
                            UserData.ID = responseMsg?.getUserId().toString()
                            if (mode == AuthController.SIGN_IN) {
                                UserData.NICKNAME = JSONObject(responseMsg?.getData().toString())["nickname"] as String
                                try {
                                    UserData.AVATAR = stringToBitmap(JSONObject(responseMsg?.getData().toString())["avatar"] as String)
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            } else {
                                UserData.NICKNAME = data["nickname"] as String
                            }

                            UserData.PASSWORD = data["password"] as String
                            UserData.EMAIL = data["email"] as String
                            UserData.LANG = if (Languages.activeLanguage == Languages.RU) "ru" else "en"

                            writeConfig()

                            startActivity(Intent(this@FirstStartActivity, LauncherActivity::class.java))
                            finish()
                        }
                    }.start()
                } else if (data["state"] == "field is empty") {
                    runOnUiThread {
                        findViewById<RelativeLayout>(R.id.auth_layout).setBackgroundResource(R.drawable.failed_auth_bg)

                        val failMsgView = findViewById<TextView>(R.id.fail_auth_msg)
                        failMsgView.visibility = View.VISIBLE

                        failMsgView.setText(R.string.empty_field)
                    }
                } else {
                    runOnUiThread {
                        findViewById<RelativeLayout>(R.id.auth_layout).setBackgroundResource(R.drawable.failed_auth_bg)

                        val failMsgView = findViewById<TextView>(R.id.fail_auth_msg)
                        failMsgView.visibility = View.VISIBLE

                        failMsgView.setText(R.string.passwords_isnt_equals)
                    }
                }
            }
        })
        controller.start()
    }

    private fun initLangSelector() {
        if (Locale.getDefault().language == Languages.RU) {
            findViewById<ImageView>(R.id.active_lang).setImageResource(R.drawable.rus_lang_icon)
            findViewById<ImageView>(R.id.other_lang).setImageResource(R.drawable.eng_lang_icon)
        }

        Languages.activeLanguage = Locale.getDefault().language

        findViewById<ImageView>(R.id.other_lang).visibility = View.GONE

        var isOpen = false

        findViewById<ImageView>(R.id.fall_lang_list_btn).setOnClickListener { it ->
            findViewById<ImageView>(R.id.other_lang).visibility = View.VISIBLE

            if (!isOpen) {
                val objAnim = ObjectAnimator.ofInt(it, "rotation", 0, 180)
                objAnim.duration = 300
                objAnim.start()

                val animator = ValueAnimator()
                animator.setIntValues(50, 100)
                animator.duration = 300
                animator.addUpdateListener {
                    val params = RelativeLayout.LayoutParams(dpToPx(80), dpToPx(it.animatedValue as Int))
                    params.setMargins(dpToPx(10), dpToPx(10), 0, 0)
                    findViewById<RelativeLayout>(R.id.lang_selector_layout).layoutParams = params
                }

                animator.start()
            } else {
                val objAnim = ObjectAnimator.ofInt(it, "rotation", 180, 0)
                objAnim.duration = 300
                objAnim.start()

                val animator = ValueAnimator()
                animator.setIntValues(100, 50)
                animator.duration = 300
                animator.addUpdateListener {
                    val params = RelativeLayout.LayoutParams(dpToPx(80), dpToPx(it.animatedValue as Int))
                    params.setMargins(dpToPx(10), dpToPx(10), 0, 0)
                    findViewById<RelativeLayout>(R.id.lang_selector_layout).layoutParams = params
                }

                animator.start()
            }

            isOpen = !isOpen
        }

        findViewById<ImageView>(R.id.other_lang).setOnClickListener {
            if (Languages.activeLanguage == Languages.EN) {
                Languages.activeLanguage = Languages.RU
            } else {
                Languages.activeLanguage = Languages.EN
            }

            val selectedLangIcon = ((it as ImageView).drawable as BitmapDrawable).bitmap
            it.setImageBitmap((findViewById<ImageView>(R.id.active_lang).drawable as BitmapDrawable).bitmap)
            findViewById<ImageView>(R.id.active_lang).setImageBitmap(selectedLangIcon)

            changeLang()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
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

    private fun stringToBitmap(data: String): Bitmap {
        val bitmapSource = Base64.getDecoder().decode(data)
        return BitmapFactory.decodeByteArray(bitmapSource, 0, bitmapSource.size)
    }

    private fun changeLang() {
        val locale = Locale(Languages.activeLanguage)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.locale = locale
        baseContext.resources.updateConfiguration(configuration, null)
        recreate()
    }

    private fun writeConfig() {
        val file = File(filesDir.path + "/main.conf")
        file.createNewFile()

        val json = JSONObject()
        json.put("nickname", UserData.NICKNAME)
        json.put("email", UserData.EMAIL)
        json.put("password", UserData.PASSWORD)
        json.put("id", UserData.ID)
        json.put("lang", UserData.LANG)
        if (UserData.AVATAR == null) {
            json.put("avatar", "null")
        } else {
            json.put("avatar", "/user_avatar.png")
            saveAvatarToFile("/user_avatar.png", UserData.AVATAR!!)
        }

        val output = DataOutputStream(FileOutputStream(file))
        output.writeUTF(json.toString())
        output.flush()
    }
}