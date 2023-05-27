package com.nakaharadev.roleworld

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

class AuthController(private val layout: RelativeLayout, private val context: Context) {
    private var authCallback: AuthCallback? = null

    private val signInLayout = layout.getChildAt(1) as LinearLayout
    private val signUpLayout = layout.getChildAt(0) as LinearLayout
    private val changeModeBtn = layout.getChildAt(3) as LinearLayout
    private val changeModeTextView = changeModeBtn.getChildAt(0) as TextView
    private val continueButton = layout.getChildAt(2) as Button

    private var resizeToSignUpAnim: ValueAnimator? = null
    private var resizeToSignInAnim: ValueAnimator? = null
    private var disappearanceSignInAnim: ObjectAnimator? = null
    private var disappearanceSignUpAnim: ObjectAnimator? = null
    private var appearanceSignInAnim: ObjectAnimator? = null
    private var appearanceSignUpAnim: ObjectAnimator? = null

    private var disappearanceChangeModeTextViewAnim: ObjectAnimator? = null
    private var appearanceChangeModeTextViewAnim: ObjectAnimator? = null

    private var openAuthAnimX: ObjectAnimator? = null
    private var openAuthAnimY: ObjectAnimator? = null

    private val SIGN_IN = 0
    private val SIGN_UP = 1
    private var authMode = SIGN_UP

    init {
        initResizeAnim()
        initChangeAuthLayoutAnim()
        initChangeModeTextViewAnim()
        initOpenAuthAnim()

        authCallback = object: AuthCallback() {
            override fun onChangeMode(mode: Int) {}
            override fun onContinue(mode: Int, data: HashMap<String, String>) {}
        }
    }

    public fun setCallback(callback: AuthCallback) {
        authCallback = callback
    }

    public fun start() {
        continueButton.setOnClickListener {
            val map = HashMap<String, String>()
            if (authMode == SIGN_IN) {
                map["email"] = (signInLayout.getChildAt(0) as EditText).text.toString()
                map["password"] = (signInLayout.getChildAt(1) as EditText).text.toString()
            } else {
                map["nickname"] = (signInLayout.getChildAt(0) as EditText).text.toString()
                map["email"] = (signInLayout.getChildAt(1) as EditText).text.toString()
                map["password"] = (signInLayout.getChildAt(2) as EditText).text.toString()
            }

            authCallback?.onContinue(authMode, map)
        }

        changeModeBtn.setOnClickListener {
            disappearanceChangeModeTextViewAnim?.start()

            if (authMode == SIGN_IN) {
                disappearanceSignInAnim?.start()
                resizeToSignUpAnim?.start()
            } else {
                disappearanceSignUpAnim?.start()
                resizeToSignInAnim?.start()
            }

            authMode = if (authMode == SIGN_IN) SIGN_UP else SIGN_IN

            authCallback?.onChangeMode(authMode)
        }

        layout.visibility = View.VISIBLE

        openAuthAnimX?.start()
        openAuthAnimY?.start()
    }

    private fun initOpenAuthAnim() {
        openAuthAnimX = ObjectAnimator.ofFloat(layout, "scaleX", 0.0f, 1.0f)
        openAuthAnimX?.duration = 300

        openAuthAnimY = ObjectAnimator.ofFloat(layout, "scaleY", 0.0f, 1.0f)
        openAuthAnimY?.duration = 300

        openAuthAnimX?.addListener(object: AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                signUpLayout.visibility = View.VISIBLE

                appearanceSignUpAnim?.startDelay = 300
                appearanceSignUpAnim?.start()
            }

            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })
    }

    private fun initResizeAnim() {
        resizeToSignUpAnim = ValueAnimator()
        resizeToSignUpAnim?.setIntValues(250, 350)
        resizeToSignUpAnim?.duration = 400
        resizeToSignUpAnim?.addUpdateListener {
            val params = RelativeLayout.LayoutParams(
                dpToPx(250), dpToPx(it.animatedValue as Int)
            )
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            layout.layoutParams = params
        }

        resizeToSignInAnim = ValueAnimator()
        resizeToSignInAnim?.setIntValues(350, 250)
        resizeToSignInAnim?.duration = 400
        resizeToSignInAnim?.addUpdateListener {
            val params = RelativeLayout.LayoutParams(
                dpToPx(250), dpToPx(it.animatedValue as Int)
            )
            params.addRule(RelativeLayout.CENTER_IN_PARENT)
            layout.layoutParams = params
        }
    }

    private fun initChangeAuthLayoutAnim() {
        // disappearance sign in
        disappearanceSignInAnim = ObjectAnimator.ofFloat(signInLayout, "alpha", 1.0f, 0.0f)
        disappearanceSignInAnim?.duration = 200
        disappearanceSignInAnim?.addListener(object: AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                signInLayout.visibility = View.GONE
                signUpLayout.visibility = View.VISIBLE

                appearanceSignUpAnim?.startDelay = 0
                appearanceSignUpAnim?.start()
            }

            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })

        // disappearance sign up
        disappearanceSignUpAnim = ObjectAnimator.ofFloat(signUpLayout, "alpha", 1.0f, 0.0f)
        disappearanceSignUpAnim?.duration = 200
        disappearanceSignUpAnim?.addListener(object: AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                signUpLayout.visibility = View.GONE
                signInLayout.visibility = View.VISIBLE

                appearanceSignInAnim?.startDelay = 0
                appearanceSignInAnim?.start()
            }

            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })

        // appearance sign in
        appearanceSignInAnim = ObjectAnimator.ofFloat(signInLayout, "alpha", 0.0f, 1.0f)
        appearanceSignInAnim?.duration = 200

        // appearance sign up
        appearanceSignUpAnim = ObjectAnimator.ofFloat(signUpLayout, "alpha", 0.0f, 1.0f)
        appearanceSignUpAnim?.duration = 200
    }

    private fun initChangeModeTextViewAnim() {
        disappearanceChangeModeTextViewAnim = ObjectAnimator.ofFloat(changeModeTextView, "alpha", 1.0f, 0.0f)
        disappearanceChangeModeTextViewAnim?.duration = 200
        disappearanceSignUpAnim?.addListener(object: AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                if (authMode == SIGN_IN) {
                    changeModeTextView.setText(R.string.sign_up)
                } else {
                    changeModeTextView.setText(R.string.sign_in)
                }

                appearanceChangeModeTextViewAnim?.start()
                Toast.makeText(context, "mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })

        appearanceChangeModeTextViewAnim = ObjectAnimator.ofFloat(changeModeTextView, "alpha", 0.0f, 1.0f)
        appearanceChangeModeTextViewAnim?.duration = 200
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    public abstract class AuthCallback {
        public abstract fun onChangeMode(mode: Int)
        public abstract fun onContinue(mode: Int, data: HashMap<String, String>)
    }
}
