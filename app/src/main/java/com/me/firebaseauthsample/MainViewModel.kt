package com.me.firebaseauthsample

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.me.firebaseauthsample.model.Authentication
import com.me.firebaseauthsample.repository.UserRepository
import com.me.firebaseauthsample.utils.Event
import com.me.firebaseauthsample.utils.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val userRepository: UserRepository): AndroidViewModel(application) {

    private val _userLoginSuccess = MutableLiveData<Event<Boolean>>()
    val userLoginSuccess: LiveData<Event<Boolean>> get() = _userLoginSuccess

    val currentUser by lazy { userRepository.getUser(getApplication()).asLiveData(viewModelScope) }

    fun loginWithApple(activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepository.signIn(Authentication.Apple(activity))?.also { userRepository.saveUser(getApplication(), it) }
            if (user != null) {
                _userLoginSuccess.postValue(Event(true))
            } else {
                _userLoginSuccess.postValue(Event(false))
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.signOut()
        }
    }
}