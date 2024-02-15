package com.nakaharadev.roleworld

import android.graphics.Bitmap

object UserData {
    var NICKNAME = ""
    var PASSWORD = ""
    var ID = ""
    var EMAIL = ""
    var LANG = ""
    var AVATAR: Bitmap? = null
    var CHARACTERS = ArrayList<Character>()
    var WORLDS = ArrayList<World>()

    var authorized = false
}