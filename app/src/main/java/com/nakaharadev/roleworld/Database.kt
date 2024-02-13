package com.nakaharadev.roleworld

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import android.util.Log
import java.util.Base64


@SuppressLint("StaticFieldLeak")
object Database {
    private var database: MySQLiteDatabase? = null

    private const val DATABASE_VERSION = 1
    private var context: Context? = null

    fun init(ctx: Context?) {
        context = ctx
        database = MySQLiteDatabase(ctx)
    }

    fun addCharacter(character: Character) {
        database?.addCharacter(character)
    }

    fun getAllCharacters(): ArrayList<Character> {
        return database!!.getAllCharacters()
    }

    private class MySQLiteDatabase : SQLiteOpenHelper {
        constructor(
            context: Context?
        ) : super(context, "database.db", null, DATABASE_VERSION)

        fun addCharacter(character: Character) {
            val db = writableDatabase

            val fields = ArrayList<String?>()

            for (title in character.getTitles()) {
                fields.add(title.removePrefix(" ").removeSuffix(" "))
                fields.add(character.getDataField(title)?.removePrefix(" ")?.removeSuffix(" "))
            }

            db.execSQL("INSERT INTO characters (_id, dataArray, avatar) VALUES ('" + character.getID() + "', '" + fields.toString() + "', '" + character.getAvatarAsString() + "');")
        }

        fun getAllCharacters(): ArrayList<Character> {
            val list = ArrayList<Character>()

            val cursor =
                readableDatabase.rawQuery("SELECT _id, dataArray, avatar FROM characters", null)
            cursor.moveToFirst()

            while (!cursor.isAfterLast) {
                val idIndex = cursor.getColumnIndex("_id")
                var id = ""
                if (idIndex != -1) id = cursor.getString(idIndex)

                val dataArrayIndex = cursor.getColumnIndex("dataArray")
                var dataArray = ""
                if (dataArrayIndex != -1) dataArray = cursor.getString(dataArrayIndex)

                val avatarIndex = cursor.getColumnIndex("avatar")
                var avatar = ""
                if (avatarIndex != -1) avatar = cursor.getString(avatarIndex)

                val avatarBytes = Base64.getDecoder().decode(avatar)
                val avatarBitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.size)

                val character = Character()
                character.setID(id)
                character.setAvatar(avatarBitmap)

                dataArray = dataArray.substring(1, dataArray.length - 1)
                val array = dataArray.split(", ")

                for (index in array.indices step 2) {
                    character.addDataField(array[index], array[index + 1])
                }

                list.add(character)

                cursor.moveToNext()
            }

            cursor.close()

            return list
        }

        override fun onCreate(db: android.database.sqlite.SQLiteDatabase?) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS characters (_id TEXT, dataArray TEXT, avatar TEXT);")
        }

        override fun onUpgrade(
            db: android.database.sqlite.SQLiteDatabase?,
            oldVersion: Int,
            newVersion: Int
        ) {
            db?.execSQL("DROP TABLE IF EXISTS characters")
            onCreate(db)
        }

    }
}