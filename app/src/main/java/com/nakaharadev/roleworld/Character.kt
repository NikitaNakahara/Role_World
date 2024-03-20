package com.nakaharadev.roleworld

import android.graphics.Bitmap
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Base64

class Character {
    private val fields = HashMap<String, String>()
    private var avatar: Bitmap? = null

    private val titles = ArrayList<String>()

    private var id: String = ""

    constructor()
    constructor(data: String) : this(JSONObject(data))

    constructor(data: JSONObject) {
        val characterData = data.getString("data")

        val arrayString: String = characterData.substring(1, characterData.length - 1)
        val array = arrayString.split(", ")

        var i = 0
        while (i < array.size - 1) {
            addDataField(array[i], array[i + 1])
            i += 2
        }
    }

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

    fun toJSON(): JSONObject {
        val json = JSONObject()

        val array = ArrayList<String?>()
        for (i in 0 until titles.size) {
            array.add(titles[i])
            array.add(fields[titles[i]])
        }

        json.put("data", array.toString())

        return json
    }

    override fun toString(): String {
        return toString(true)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Character) {
            val otherTitles = other.titles

            if (titles.size != otherTitles.size) return false

            for (i in 0 until titles.size) {
                if (fields[titles[i]]?.equals(other.getDataField(otherTitles[i])) == false) {
                    return false
                }
            }

            return true
        }

        return false
    }

    fun toString(withAvatar: Boolean): String {
        val json = toJSON()

        if (withAvatar) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            avatar?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            json.put("avatar", Base64.getEncoder().encodeToString(byteArray))
        }

        return json.toString()
    }
}