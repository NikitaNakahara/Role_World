package com.nakaharadev.roleworld

import android.graphics.Bitmap
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.ArrayList
import java.util.Base64
import java.util.HashMap

class Character {
    private val fields = HashMap<String, String>()
    private var avatar: Bitmap? = null

    private val titles = ArrayList<String>()

    private var id: String = ""

    fun addDataField(title: String, data: String) {
        titles.add(title)
        fields[title] = data
    }

    fun getDataField(title: String) : String? {
        return fields[title]
    }

    fun setAvatar(avatar: Bitmap) { this.avatar = avatar }
    fun getAvatar(): Bitmap? { return avatar }

    fun getAvatarAsString(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        avatar?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.getEncoder().encodeToString(byteArray)
    }

    fun setID(id: String) { this.id = id }
    fun getID(): String { return id }

    fun getTitles(): ArrayList<String> { return titles }

    override fun toString(): String {
        val json = JSONObject()

        val array = ArrayList<String?>()
        for (i in 0 until titles.size) {
            array.add(titles[i])
            array.add(fields[titles[i]])
        }
        json.put("data", array.toString())

        val byteArrayOutputStream = ByteArrayOutputStream()
        avatar?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        json.put("avatar", Base64.getEncoder().encodeToString(byteArray))

        return json.toString();
    }
}