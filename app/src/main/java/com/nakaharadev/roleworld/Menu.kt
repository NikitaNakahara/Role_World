package com.nakaharadev.roleworld

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.annotation.IdRes


@SuppressLint("StaticFieldLeak")
object Menu {
    private var menuLayout: LinearLayout? = null
    private var darkening: View? = null
    private var scrollViews: ArrayList<ScrollView>? = null
    private var menuBtn: ImageView? = null

    private var context: Context? = null

    private var menuIsShowed = false
    private var lastMenuIsShowed = false

    private var menuCallback: MenuCallback? = null

    fun setMenuCallback(callback: MenuCallback) {
        menuCallback = callback
    }

    fun setMenuLayout(layout: LinearLayout?) {
        menuLayout = layout

        for (i: Int in 1 until menuLayout?.childCount!!) {
            menuLayout?.getChildAt(i)?.setOnClickListener {
                menuCallback?.onMenuButtonPressed(it.id)

                for (j: Int in 1 until menuLayout?.childCount!!) {
                    context?.resources?.getColor(R.color.gray_bg)
                        ?.let { it1 -> menuLayout?.getChildAt(j)?.setBackgroundColor(it1) }
                }

                it.setBackgroundResource(R.drawable.active_menu_btn_bg)
            }
        }
    }

    fun setContext(_context: Context?) {
        context = _context
    }

    fun setDarkening(view: View?) {
        darkening = view
    }

    fun setScrollViews(views: ArrayList<ScrollView>?) {
        scrollViews = views
    }

    fun setMenuBtn(btn: ImageView?) {
        menuBtn = btn
    }

    fun isShowed(): Boolean {
        return menuIsShowed
    }

    fun showMenu() {
        if (!menuIsShowed) {
            val anim: ObjectAnimator = ObjectAnimator.ofFloat(
                menuLayout,
                "X",
                dpToPx(-301, context).toFloat(), 0.0f
            )
            anim.duration = 200
            anim.addUpdateListener { animation: ValueAnimator ->
                menuLayout!!.x = animation.animatedValue as Float
                darkening!!.alpha = (0.3f - -menuLayout!!.x / 1000.0f) * 1.5f
            }
            anim.start()
            menuBtn!!.setImageDrawable(context!!.resources.getDrawable(R.drawable.burger_menu_anim))
            if (menuBtn!!.drawable is Animatable) {
                (menuBtn!!.drawable as AnimatedVectorDrawable).start()
            }

            menuCallback?.onMenuOpen()
        }
        menuIsShowed = true
    }

    fun hiddenMenu() {
        if (menuIsShowed) {
            val anim: ObjectAnimator = ObjectAnimator.ofFloat(
                menuLayout,
                "X",
                0.0f, dpToPx(-301, context).toFloat()
            )
            anim.duration = 200
            anim.addUpdateListener { animation: ValueAnimator ->
                menuLayout!!.x = animation.animatedValue as Float
                darkening!!.alpha = (0.3f - -menuLayout!!.x / 1000.0f) * 1.5f
            }
            anim.start()
            menuBtn!!.setImageDrawable(context!!.resources.getDrawable(R.drawable.reversed_burger_menu_anim))
            if (menuBtn!!.drawable is Animatable) {
                (menuBtn!!.drawable as AnimatedVectorDrawable).start()
            }

            menuCallback?.onMenuHidden()
        }
        menuIsShowed = false
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initMoveMenuListener() {
        for (view: View in scrollViews!!) {
            view.setOnTouchListener(object: OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (v != null && event != null) {
                        return moveMenuSwipeListener(v, event)
                    }

                    return false
                }
            })
        }
    }

    private var x = 0f
    private var startX = 0f
    private var startY = 0f
    private var moveMenu = false
    private var swipeDirIsVertical = false

    private val MIN_SWIPE = 10

    private fun moveMenuSwipeListener(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                x = startX
                startY = event.y
                lastMenuIsShowed = menuIsShowed
            }

            MotionEvent.ACTION_MOVE -> {
                var menuX = menuLayout!!.x
                if (!swipeDirIsVertical) {
                    if (Math.abs(event.x - startX) > MIN_SWIPE &&
                        Math.abs(event.x - startX) > Math.abs(event.y - startY)
                    ) {
                        moveMenu = true
                    } else if (Math.abs(event.x - startX) < Math.abs(event.y - startY)) {
                        swipeDirIsVertical = true
                    }
                }
                return if (moveMenu) {
                    val deltaX = event.x - x
                    x = event.x
                    menuX += deltaX
                    darkening!!.alpha = (0.3f - -menuX / 1000.0f) * 1.5f
                    menuLayout!!.x = menuX
                    if (menuX < dpToPx(-301, context).toFloat()) {
                        menuLayout!!.x = dpToPx(-301, context).toFloat()
                    } else if (menuX > 0.0f) {
                        menuLayout!!.x = 0.0f
                    }
                    true
                } else {
                    false
                }
            }

            MotionEvent.ACTION_UP -> {
                val menuX = menuLayout!!.x
                if (menuX < dpToPx(-150, context).toFloat()) {
                    hiddenMenu(menuX)
                } else if (menuX > dpToPx(-150, context).toFloat()) {
                    showMenu(menuX)
                } else {
                    menuIsShowed = menuX != dpToPx(-301, context).toFloat()
                }
                if (lastMenuIsShowed != menuIsShowed) {
                    if (menuIsShowed) {
                        menuBtn!!.setImageDrawable(context!!.resources.getDrawable(R.drawable.burger_menu_anim))
                    } else {
                        menuBtn!!.setImageDrawable(context!!.resources.getDrawable(R.drawable.reversed_burger_menu_anim))
                    }
                    if (menuBtn!!.drawable is Animatable) {
                        (menuBtn!!.drawable as AnimatedVectorDrawable).start()
                    }
                }
                moveMenu = false
                swipeDirIsVertical = false
            }
        }
        return false
    }

    private fun showMenu(startX: Float) {
        val anim = ObjectAnimator.ofFloat(
            menuLayout,
            "X",
            startX, 0f
        )
        anim.duration = 200
        anim.addUpdateListener { animation: ValueAnimator ->
            menuLayout!!.x = animation.animatedValue as Float
            darkening!!.alpha = (0.3f - -menuLayout!!.x / 1000.0f) * 1.5f
        }
        anim.start()

        menuCallback?.onMenuOpen()

        menuIsShowed = true
    }

    private fun hiddenMenu(startX: Float) {
        val anim = ObjectAnimator.ofFloat(
            menuLayout,
            "X",
            startX, dpToPx(-301, context).toFloat()
        )
        anim.duration = 200
        anim.addUpdateListener { animation: ValueAnimator ->
            menuLayout!!.x = animation.animatedValue as Float
            darkening!!.alpha = (0.3f - -menuLayout!!.x / 1000.0f) * 1.5f
        }
        anim.start()

        menuCallback?.onMenuHidden()

        menuIsShowed = false
    }

    private fun dpToPx(dp: Int, context: Context?): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context?.resources?.displayMetrics
        ).toInt()
    }

    public abstract class MenuCallback {
        public abstract fun onMenuButtonPressed(@IdRes id: Int)
        public open fun onMenuHidden() {}
        public open fun onMenuOpen() {}
    }
}