package com.example.bookreviewapp.sharedPreference

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean{
        return prefs.getBoolean(key, defValue)
    }

    fun setBoolean(key: String, boolean: Boolean){
        prefs.edit().putBoolean(key,boolean).apply()
    }

    fun getLong(key: String, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    fun setLong(key: String, long: Long) {
        prefs.edit().putLong(key, long).apply()
    }

}