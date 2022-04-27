package com.sprotte.geolocator.demo.misc
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Parcelable
import androidx.preference.PreferenceManager
import com.github.florent37.application.provider.application
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


private class SharedPreferenceDelegate<T>(
    private val context: Context,
    private val defaultValue: T,
    private val getter: SharedPreferences.(String, T) -> T,
    private val setter: Editor.(String, T) -> Editor,
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

fun <T> sharedPreference(key: String, defaultValue: T): ReadWriteProperty<Any, T> = sharedPreference(application!!, key, defaultValue)

@Suppress("UNCHECKED_CAST")
fun <T> sharedPreference(context: Context = application!!, key: String, defaultValue: T): ReadWriteProperty<Any, T> =
    when (defaultValue) {
        is Parcelable -> SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getParcelable, Editor::putParcelable, key)
        is Boolean -> SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getBoolean, Editor::putBoolean, key)
        is Int -> SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getInt, Editor::putInt, key)
        is Long -> SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getLong, Editor::putLong, key)
        is Float -> SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getFloat, Editor::putFloat, key)
        is String -> SharedPreferenceDelegate(context, defaultValue, SharedPreferences::getString, Editor::putString, key)
        else -> throw IllegalArgumentException()
    } as ReadWriteProperty<Any, T>


inline fun <reified T : Parcelable> SharedPreferences.getParcelable(key: String, defValue: T): T = getString(key, defValue.toJson())!!.fromJson()

inline fun <reified T : Parcelable> Editor.putParcelable(key: String, value: T): Editor = putString(key, value.toJson())