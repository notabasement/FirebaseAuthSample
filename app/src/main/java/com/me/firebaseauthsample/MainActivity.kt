package com.me.firebaseauthsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.me.firebaseauthsample.login.LoginActivity
import com.me.firebaseauthsample.register.RegisterActivity
import com.me.firebaseauthsample.repository.UserRepository
import com.me.firebaseauthsample.utils.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { ViewModelFactory(application, UserRepository.getInstance()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLoginWithEmail.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        btnLoginWithApple.setOnClickListener {
            viewModel.loginWithApple(this)
        }
        btnLogout.setOnClickListener {
            viewModel.logout()
        }
        viewModel.userLoginSuccess.observe(this, {
            it.getContentIfNotHandled()?.run {
                if (this) {
                    Toast.makeText(applicationContext, "Login Successful...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.currentUser.observe(this, {
            val isLoggedIn = it != null
            if (isLoggedIn) {
                btnLogout.visibility = View.VISIBLE

                btnLoginWithApple.visibility = View.INVISIBLE
                btnRegister.visibility = View.INVISIBLE
                btnLoginWithEmail.visibility = View.INVISIBLE

                tvCurrentUserInfo.visibility = View.VISIBLE
                tvEmail.visibility = View.VISIBLE
                tvEmail.text = it?.email
            } else {
                btnLogout.visibility = View.INVISIBLE

                btnLoginWithApple.visibility = View.VISIBLE
                btnRegister.visibility = View.VISIBLE
                btnLoginWithEmail.visibility = View.VISIBLE

                tvCurrentUserInfo.visibility = View.INVISIBLE
                tvEmail.visibility = View.INVISIBLE
                tvEmail.text = null
            }
        })
    }
}