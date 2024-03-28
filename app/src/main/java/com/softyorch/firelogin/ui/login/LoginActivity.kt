package com.softyorch.firelogin.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.softyorch.firelogin.databinding.ActivityLoginBinding
import com.softyorch.firelogin.ui.detail.DetailActivity
import com.softyorch.firelogin.ui.signup.SignUpActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    private fun initUI() {
        initListeners()
        initUiState()
    }

    private fun initUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { show ->
                    binding.pbLoading.isVisible = show
                }
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                viewModel.login(
                    user = binding.tieUser.text.toString(),
                    pass = binding.tiePass.text.toString()
                ) {
                    navigateToDetail()
                }
            }
            tvSignUp.setOnClickListener {
                navigateToSignUp()
            }
        }
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    private fun navigateToDetail() {
        startActivity(Intent(this, DetailActivity::class.java))
    }
}