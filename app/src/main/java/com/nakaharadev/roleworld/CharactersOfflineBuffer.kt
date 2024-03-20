package com.nakaharadev.roleworld

import org.json.JSONArray
import org.json.JSONObject

object CharactersOfflineBuffer {
    private val list = ArrayList<Character>()

    fun isEmpty(): Boolean {
        return list.size == 0
    }

    fun load(jsonString: String) {
        val json = JSONObject(jsonString)
        val array = json.getJSONArray("characters")

        for (i: Int in 0 until array.length()) {
            add(Character(array.getJSONObject(i)))
        }
    }

    fun remove(index: Int) {
        list.removeAt(index)
    }

    fun remove(character: Character) {
        list.remove(character)
    }

    fun add(character: Character) {
        list.add(character)
    }

    fun getAll(): ArrayList<Character> {
        return list
    }

    fun clear() {
        list.clear()
    }

    fun toJSON(): JSONObject {
        val json = JSONObject()
        val array = JSONArray()

        for (elem in list) {
            array.put(elem.toJSON())
        }

        json.put("characters", array)

        return json
    }

    override fun toString(): String {
        return toJSON().toString()
    }
}