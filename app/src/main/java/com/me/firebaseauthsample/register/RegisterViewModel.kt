package com.me.firebaseauthsample.register

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

class RegisterViewModel(application: Application, private val userRepository: UserRepository): AndroidViewModel(application) {

    private val _userLoginSuccess = MutableLiveData<Event<Boolean>>()
    val userLoginSuccess: LiveData<Event<Boolean>> get() = _userLoginSuccess

    private val _duplicateEvent = MutableLiveData<Event<Unit>>()
    val duplicateEvent: LiveData<Event<Unit>> get() = _duplicateEvent

    private val _loadingEvent = MutableLiveData<Boolean>().apply { value = false }
    val loadingEvent: LiveData<Boolean> get() = _loadingEvent

    fun register(email: String, password: String) {
        if (_loadingEvent.value == true) return

        _loadingEvent.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val isExisted = userRepository.isExistUserForEmail(email)
            if (isExisted) {
                _loadingEvent.postValue(false)
                _duplicateEvent.postValue(Event(Unit))
                return@launch
            }

            val user = userRepository.signUpWithEmail(email, password)?.also { userRepository.saveUser(getApplication(), it) }
            if (user != null) {
                _userLoginSuccess.postValue(Event(true))
            } else {
                _userLoginSuccess.postValue(Event(false))
            }
            _loadingEvent.postValue(false)
        }
    }
}