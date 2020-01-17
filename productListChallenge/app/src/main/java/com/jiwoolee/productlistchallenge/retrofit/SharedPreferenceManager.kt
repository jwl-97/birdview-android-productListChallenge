package com.jiwoolee.productlistchallenge.retrofit

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceManager {
    private const val PREFERENCES_NAME = "rebuild_preference"
    private const val DEFAULT_VALUE_STRING = ""
    private const val DEFAULT_VALUE_BOOLEAN = false
    private const val DEFAULT_VALUE_Int = 0

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    /* 저장 */
    fun setString(context: Context, key: String, value: String) {
        val prefs =
            getPreferences(
                context
            )
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun setInt(context: Context, key: String, value: Int) {
        val prefs =
            getPreferences(
                context
            )
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        val prefs =
            getPreferences(
                context
            )
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun setToken(context: Context, key: String, value: Boolean) {
        val prefs =
            getPreferences(
                context
            )
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /* 로드 */
    fun getString(context: Context, key: String): String? {
        val prefs =
            getPreferences(
                context
            )
        return prefs.getString(key,
            DEFAULT_VALUE_STRING
        )
    }

    fun getInt(context: Context, key: String): Int {
        val prefs =
            getPreferences(
                context
            )
        return prefs.getInt(key,
            DEFAULT_VALUE_Int
        )
    }

    fun getBoolean(context: Context, key: String): Boolean {
        val prefs =
            getPreferences(
                context
            )
        return prefs.getBoolean(key,
            DEFAULT_VALUE_BOOLEAN
        )
    }

    fun getToken(context: Context, key: String): Boolean {
        val prefs =
            getPreferences(
                context
            )
        return prefs.getBoolean(key,
            DEFAULT_VALUE_BOOLEAN
        )
    }

    /* 키 값 삭제 */
    fun removeKey(context: Context, key: String) {
        val prefs =
            getPreferences(
                context
            )
        val edit = prefs.edit()
        edit.remove(key)
        edit.apply()
    }

    /*모든 데이터 삭제*/
    fun clear(context: Context) {
        val prefs =
            getPreferences(
                context
            )
        val edit = prefs.edit()
        edit.clear()
        edit.apply()
    }
}