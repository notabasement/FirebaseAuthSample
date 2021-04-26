package com.me.firebaseauthsample.utils

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


private fun <T> asFlow(sharedPrefs: SharedPreferences, key: String, get: ((key: String) -> T)): Flow<T> = callbackFlow {
    safeOffer(get.invoke(key))
    val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
        if (key == k) {
            this@callbackFlow.safeOffer(get.invoke(key))
        }
    }
    sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    awaitClose {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
}

fun SharedPreferences.stringFlow(key: String) = asFlow<String>(this, key, ::getString)

fun SharedPreferences.intFlow(key: String) = asFlow<Int>(this, key, ::getInt)

fun SharedPreferences.booleanFlow(key: String) = asFlow<Boolean>(this, key, ::getBoolean)

fun SharedPreferences.floatFlow(key: String) = asFlow<Float>(this, key, ::getFloat)

fun SharedPreferences.longFlow(key: String) = asFlow<Long>(this, key, ::getLong)

fun SharedPreferences.stringSetFlow(key: String) =
    asFlow<Set<String>>(this, key, ::getStringSet)


fun SharedPreferences.getString(key: String) = this.getString(key, "") ?: ""

fun SharedPreferences.getBoolean(key: String) = this.getBoolean(key, false)

fun SharedPreferences.getFloat(key: String) = this.getFloat(key, 0f)

fun SharedPreferences.getInt(key: String) = this.getInt(key, 0)

fun SharedPreferences.getLong(key: String) = this.getLong(key, 0L)

fun SharedPreferences.getStringSet(key: String) = this.getStringSet(key, HashSet<String>()) ?: HashSet<String>()

