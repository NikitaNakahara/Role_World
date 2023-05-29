package com.nakaharadev.roleworld

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.util.Locale


class FirstStartActivity : Activity() {
    var network: Network? = null

    var authFailed = false
    var responseIsGet = false
    var responseMsg = ""

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
        network?.setNetworkCallback(object: Network.NetworkCallback() {
            override fun onConnected() {
                runOnUiThread {
                    findViewById<LinearLayout>(R.id.check_conn_indicator).visibility = View.GONE
                    findViewById<TextView>(R.id.preview_continue).visibility = View.VISIBLE

                    initContinueIndicator()
                }
            }

            override fun onGetMessage(msg: String) {
                val json = JSONObject(msg)
                if (json["state"] == "failed") {
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
                    val json = (data as Map<*, *>?)?.let { JSONObject(it) }
                    json?.put("type", "auth")
                    if (mode == AuthController.SIGN_IN) {
                        json?.put("mode", "sign_in")
                    } else {
                        json?.put("mode", "sign_up")
                    }

                    network?.sendMsg(json.toString())

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
                            val json = JSONObject(responseMsg)
                            UserData.ID = json.getString("id")
                            if (mode == AuthController.SIGN_IN) {
                                UserData.NICKNAME = json["nickname"] as String
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

        val json = JSONObject()
        json.put("nickname", UserData.NICKNAME)
        json.put("email", UserData.EMAIL)
        json.put("password", UserData.PASSWORD)
        json.put("id", UserData.ID)
        json.put("lang", UserData.LANG)

        val writer = FileWriter(file)
        writer.write(json.toString())
    }
}