package com.me.firebaseauthsample.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.me.firebaseauthsample.model.Authentication
import com.me.firebaseauthsample.repository.UserRepository
import com.me.firebaseauthsample.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(application: Application, private val userRepository: UserRepository): AndroidViewModel(application) {

    private val _userLoginSuccess = MutableLiveData<Event<Unit>>()
    val userLoginSuccess: LiveData<Event<Unit>> get() = _userLoginSuccess

    private val _userLoginFailed = MutableLiveData<Event<String>>()
    val userLoginFailed: LiveData<Event<String>> get() = _userLoginFailed

    private val _loadingEvent = MutableLiveData<Boolean>().apply { value = false }
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    fun login(email: String, password: String) {
        if (_loadingEvent.value == true) return

        _loadingEvent.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = userRepository.signIn(Authentication.Email(email, password))
                    ?.also { userRepository.saveUser(getApplication(), it) }
                if (user != null) {
                    _userLoginSuccess.postValue(Event(Unit))
                } else {
                    _userLoginFailed.postValue(Event("User data is empty"))
                }
            } catch (e: Throwable) {
                _userLoginFailed.postValue(Event(e.message ?: e.cause?.toString() ?: e.toString()))
            }
            _loadingEvent.postValue(false)
        }
    }
}