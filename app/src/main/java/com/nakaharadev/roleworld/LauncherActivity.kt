package com.nakaharadev.roleworld

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import org.json.JSONObject
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.lang.StringBuilder

class LauncherActivity : Activity() {
    private val CONFIG_PATH = "/main.conf"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val confIsExists = checkConfigFile()
        if (!confIsExists) {
            startActivity(Intent(this, FirstStartActivity::class.java))
        } else {
            parseConfig(readConfig())
            startActivity(Intent(this, AppActivity::class.java))
        }
    }

    private fun checkConfigFile(): Boolean {
        val file = File(filesDir.path + CONFIG_PATH)
        return file.exists()
    }

    private fun readConfig(): String {
        val input = DataInputStream(FileInputStream(File(filesDir.path + CONFIG_PATH)))
        return input.readUTF()
    }

    private fun parseConfig(config: String) {
        val json = JSONObject(config)

        UserData.NICKNAME = json["nickname"] as String
        UserData.EMAIL = json["email"] as String
        UserData.ID = json["id"] as String
        UserData.PASSWORD = json["password"] as String
        UserData.LANG = json["lang"] as String
    }
}