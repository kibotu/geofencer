package com.sprotte.geolocator.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import androidx.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private class SharedPreferenceDelegate<T>(
    private val context: Context,
    private val defaultValue: T,
    private val getter: SharedPreferences.(String, T) -> T,
    private val setter: SharedPreferences.Editor.(String, T) -> Editor,
    private val key: String
) : ReadWriteProperty<Any, T> {

    private val safeContext: Context by lazyFast { context.safeContext() }

    private val sharedPreferences: SharedPreferences by lazyFast {
        PreferenceManager.getDefaultSharedPreferences(safeContext)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>) =
        sharedPreferences
            .getter(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        sharedPreferences
            .edit()
            .setter(key, value)
            .apply()
}

@Suppress("UNCHECKED_CAST")
internal fun <T> Context.sharedPreference(key: String, defaultValue: T): ReadWriteProperty<Any, T> =
    when (defaultValue) {
        is Boolean -> SharedPreferenceDelegate(
            this,
            defaultValue,
            SharedPreferences::getBoolean,
            Editor::putBoolean,
            key
        )

        is Int -> SharedPreferenceDelegate(
            this,
            defaultValue,
            SharedPreferences::getInt,
            Editor::putInt,
            key
        )

        is Long -> SharedPreferenceDelegate(
            this,
            defaultValue,
            SharedPreferences::getLong,
            Editor::putLong,
            key
        )

        is Float -> SharedPreferenceDelegate(
            this,
            defaultValue,
            SharedPreferences::getFloat,
            Editor::putFloat,
            key
        )

        is String -> SharedPreferenceDelegate(
            this,
            defaultValue,
            SharedPreferences::getString,
            Editor::putString,
            key
        )

        else -> throw IllegalArgumentException()
    } as ReadWriteProperty<Any, T>