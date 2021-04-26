package com.me.firebaseauthsample.model

sealed class Authentication {
    class Email(val email: String, val password: String) : Authentication()
    class Apple(val activity: Any) : Authentication()
}