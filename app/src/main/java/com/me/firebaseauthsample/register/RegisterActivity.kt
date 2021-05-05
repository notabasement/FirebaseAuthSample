package com.me.firebaseauthsample.register

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.me.firebaseauthsample.R
import com.me.firebaseauthsample.repository.UserRepository
import com.me.firebaseauthsample.utils.ViewModelFactory
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity: AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels { ViewModelFactory(application, UserRepository.getInstance()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnRegister.setOnClickListener {
            viewModel.register(edtEmail.text.trim().toString(), edtPassword.text.trim().toString())
        }

        viewModel.userLoginSuccess.observe(this, {
            it.getContentIfNotHandled()?.run {
                Toast.makeText(applicationContext, "Register Successful...", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
        })
        viewModel.userLoginFailed.observe(this, {
            it.getContentIfNotHandled()?.run {
                Log.v("RegisterActivity", "Error message $this")
                Toast.makeText(applicationContext, this, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.duplicateEvent.observe(this, {
            it.getContentIfNotHandled()?.run {
                Toast.makeText(applicationContext, "Duplicate register email", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.loadingEvent.observe(this, {
            if (it) {
                btnRegister.visibility = View.INVISIBLE
                prgLoading.visibility = View.VISIBLE
            } else {
                prgLoading.visibility = View.INVISIBLE
                btnRegister.visibility = View.VISIBLE
            }
        })
    }
}