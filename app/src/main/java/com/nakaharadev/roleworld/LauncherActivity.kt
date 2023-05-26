package com.nakaharadev.roleworld

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.io.File

class LauncherActivity : Activity() {
    private val CONFIG_PATH = "/main.conf"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val confIsExists = checkConfigFile()
        if (!confIsExists) {
            startActivity(Intent(this, FirstStartActivity::class.java))
        }
    }

    fun checkConfigFile(): Boolean {
        val file = File(filesDir.path + CONFIG_PATH)
        return file.exists()
    }
}