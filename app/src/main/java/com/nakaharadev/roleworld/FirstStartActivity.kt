package com.nakaharadev.roleworld

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.widget.ImageView

class FirstStartActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.first_start)

        val images = ArrayList<Bitmap>()
        images.add((resources.getDrawable(R.drawable.first_start_bg) as BitmapDrawable).bitmap)
        images.add((resources.getDrawable(R.drawable.first_start_bg2) as BitmapDrawable).bitmap)

        val previewAnimator = PreviewAnimator(findViewById(R.id.preview), findViewById(R.id.preview_background), images)
        previewAnimator.start()
    }
}