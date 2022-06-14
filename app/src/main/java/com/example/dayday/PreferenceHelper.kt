package com.example.dayday

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {
    private const val DEFAULT_SHARED_PREF_FILE_NAME = "sample preference"
    private const val DEFAULT_BOOLEAN_VALUE = false
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(DEFAULT_SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun setBoolean(context: Context, key: String?, value: Boolean?) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.putBoolean(key, value!!)
        editor.commit()
    }

    fun getBoolean(context: Context, key: String?): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(key, DEFAULT_BOOLEAN_VALUE)
    }

    fun removeKey(context: Context, key: String?) {
        val prefs = getPreferences(context)
        val edit = prefs.edit()
        edit.remove(key)
        edit.commit()
    }

    fun clear(context: Context) {
        val prefs = getPreferences(context)
        val edit = prefs.edit()
        edit.clear()
        edit.commit()
    }
}
