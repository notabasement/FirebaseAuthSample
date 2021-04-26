package com.me.firebaseauthsample.utils

import android.app.Application
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider
import com.me.firebaseauthsample.MainViewModel
import com.me.firebaseauthsample.login.LoginViewModel
import com.me.firebaseauthsample.register.RegisterViewModel
import com.me.firebaseauthsample.repository.UserRepository
import java.lang.Exception

class ViewModelFactory(private val application: Application, private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when(modelClass) {
            MainViewModel::class.java -> MainViewModel(application, userRepository)
            LoginViewModel::class.java -> LoginViewModel(application, userRepository)
            RegisterViewModel::class.java -> RegisterViewModel(application, userRepository)
            else -> throw Exception("Can't create view model instance")
        } as T
    }
}
