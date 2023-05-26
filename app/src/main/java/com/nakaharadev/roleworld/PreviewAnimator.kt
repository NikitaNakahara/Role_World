package com.nakaharadev.roleworld

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView

class PreviewAnimator(
    foregroundImageView: ImageView,
    backgroundImageView: ImageView,
    images: ArrayList<Bitmap>
) {
    private var foregroundImageView: ImageView? = foregroundImageView
    private var backgroundImageView: ImageView? = backgroundImageView
    private var images: ArrayList<Bitmap>? = images
    private var duration = 10000
    private var displayedImageIndex = 0

    public fun start() {
        if (images?.size!! <= 1) return

        val animator = ValueAnimator()
        animator.setFloatValues(100.0f, 0.0f)
        animator.duration = duration.toLong()
        animator.startDelay = 2000
        animator.repeatCount = ValueAnimator.INFINITE

        animator.addUpdateListener {
            foregroundImageView?.alpha = it.animatedValue as Float
        }

        animator.addListener(
            object: AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}

                override fun onAnimationEnd(p0: Animator) {}

                override fun onAnimationCancel(p0: Animator) {}

                override fun onAnimationRepeat(p0: Animator) {
                    if (images?.size == 2) {
                        swipeImages()
                    }
                }
            }
        )

        animator.start()
    }

    private fun swipeImages() {
        val foregroundBitmap = (foregroundImageView?.drawable as BitmapDrawable).bitmap
        foregroundImageView?.setImageBitmap((backgroundImageView?.drawable as BitmapDrawable).bitmap)
        backgroundImageView?.setImageBitmap(foregroundBitmap)
    }
}