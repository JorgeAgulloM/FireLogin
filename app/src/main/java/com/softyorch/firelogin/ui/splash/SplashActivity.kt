package com.softyorch.firelogin.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.softyorch.firelogin.databinding.ActivitySplashBinding
import com.softyorch.firelogin.ui.detail.DetailActivity
import com.softyorch.firelogin.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        when (viewModel.checkDestination()) {
            SplashDestination.Home -> navigateToDetail()
            SplashDestination.Login -> navigateToLogin()
        }
    }

    private fun navigateToDetail() {
        startActivity(Intent(this, DetailActivity::class.java))
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

}