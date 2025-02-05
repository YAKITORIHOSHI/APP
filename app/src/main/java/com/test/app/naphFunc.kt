package com.test.app

import android.content.Context

object NaphExtra {

    // Function to save the checkbox state
    fun rememberMe(context: Context, remember: Boolean): Boolean {
        // Save the state using SharedPreferencesHelper
        SharedPreferencesHelper.saveBoolean(context, "remember_me", remember)

        return remember // Return the updated value
    }

    // Function to get the saved checkbox state
    fun isRemembered(context: Context): Boolean {
        // Retrieve the state using SharedPreferencesHelper
        return SharedPreferencesHelper.getBoolean(context, "remember_me")
    }
}
