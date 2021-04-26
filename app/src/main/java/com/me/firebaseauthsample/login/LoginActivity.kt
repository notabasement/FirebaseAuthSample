package com.me.firebaseauthsample.login

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.me.firebaseauthsample.R
import com.me.firebaseauthsample.repository.UserRepository
import com.me.firebaseauthsample.utils.ViewModelFactory
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels { ViewModelFactory(application, UserRepository.getInstance()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btnLogin.setOnClickListener {
            viewModel.login(edtEmail.text.trim().toString(), edtPassword.text.trim().toString())
        }

        viewModel.userLoginSuccess.observe(this, {
            it.getContentIfNotHandled()?.run {
                if (this) {
                    Toast.makeText(applicationContext, "Login Successful...", Toast.LENGTH_SHORT).show()
                    onBackPressed()
                } else {
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.loadingEvent.observe(this, {
            if (it) {
                btnLogin.visibility = View.INVISIBLE
                prgLoading.visibility = View.VISIBLE
            } else {
                prgLoading.visibility = View.INVISIBLE
                btnLogin.visibility = View.VISIBLE
            }
        })
    }
}