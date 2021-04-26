package com.me.firebaseauthsample

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.me.firebaseauthsample.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication: MultiDexApplication(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onAppForegrounded() {
        GlobalScope.launch(Dispatchers.IO) {
            val userRepository = UserRepository.getInstance()
            userRepository.reloadUser()?.also { userRepository.saveUser(this@MyApplication, it) }
        }
    }
}