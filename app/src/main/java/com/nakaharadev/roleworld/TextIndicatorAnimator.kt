package com.nakaharadev.roleworld

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.widget.TextView

class TextIndicatorAnimator(view: TextView) {
    private val view: TextView = view

    private var disappearanceAnimator: ObjectAnimator? = null
    private var appearanceAnimator: ObjectAnimator? = null

    init {
        disappearanceAnimator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f)
        disappearanceAnimator?.duration = 3000
        disappearanceAnimator?.startDelay = 1000

        disappearanceAnimator?.addListener(
            object: AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}

                override fun onAnimationEnd(p0: Animator) {
                    appearanceAnimator?.start()
                }

                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}

            }
        )

        appearanceAnimator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f)
        appearanceAnimator?.duration = 3000
        appearanceAnimator?.addListener(
            object: AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}

                override fun onAnimationEnd(p0: Animator) {
                    disappearanceAnimator?.start()
                }

                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}

            }
        )
    }

    public fun start() {
        disappearanceAnimator?.start()
    }

    public fun stop() {
        disappearanceAnimator?.end()
    }
}