package com.sprotte.geolocator.demo.kotlin

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.github.florent37.application.provider.application
import com.sprotte.geolocator.demo.misc.safeContext

val sharedPreferences: SharedPreferences?
    get() {
        return PreferenceManager
            .getDefaultSharedPreferences(application?.safeContext() ?: return null)
    }