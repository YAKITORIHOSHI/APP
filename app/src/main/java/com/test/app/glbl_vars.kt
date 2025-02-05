package com.test.app

var screenDirectory: Boolean? = null

object GlobalVar {

    var glob_Nav: Boolean? = null

    var glob_username: String = ""
    var glob_password: String = ""
    var glob_email: String = ""
    var glob_userUID: String = ""
    var glob_access: Boolean? = null // Optional Inclusion
    var glob_Logged: Boolean? = null

}

fun fetchRemember(context: MainActivity) {

    screenDirectory = SharedPreferencesHelper.getBoolean(context, "remember_me")

}