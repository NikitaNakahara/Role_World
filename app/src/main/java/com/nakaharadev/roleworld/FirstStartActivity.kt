package com.nakaharadev.roleworld

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import java.util.Locale





class FirstStartActivity : Activity() {
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
}