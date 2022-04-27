package com.sprotte.geolocator.demo.misc

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

val gson by lazy {
    GsonBuilder()
        .disableHtmlEscaping()
        .create()
}

inline fun <reified T> T.toJson(): String = gson.toJson(this)

inline fun <reified T> String.fromJson(): T = gson.fromJson(this)

inline fun <reified T> Gson.fromJson(json: String): T = this.fromJson<T>(json, object : TypeToken<T>() {}.type)