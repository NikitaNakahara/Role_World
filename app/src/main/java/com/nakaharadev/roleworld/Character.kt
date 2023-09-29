package com.nakaharadev.roleworld

import android.graphics.Bitmap
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Base64

class Character(name: String, sex: String, rasa: String, description: String, avatar: Bitmap) {
    private var name = name
    private var sex = sex
    private var rasa = rasa
    private var description = description
    private var avatar = avatar

    fun getName(): String { return name }
    fun getSex(): String { return sex }
    fun getRasa(): String { return rasa }
    fun getDescription(): String { return description }
    fun getAvatar(): Bitmap { return avatar }

    override fun toString(): String {
        val json = JSONObject()
        json.put("name", name)
        json.put("sex", sex)
        json.put("rasa", rasa)
        json.put("description", description)

        val byteArrayOutputStream = ByteArrayOutputStream()
        UserData.AVATAR?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        json.put("avatar", Base64.getEncoder().encodeToString(byteArray))

        return json.toString()
    }
}