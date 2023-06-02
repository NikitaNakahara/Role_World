package com.nakaharadev.roleworld

import android.app.Activity
import android.os.Bundle

class AppActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_flipper)
    }
}