package com.softyorch.firelogin.ui.signup

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.softyorch.firelogin.databinding.ActivitySignUpBinding
import com.softyorch.firelogin.ui.detail.DetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private val viewModel: SignUpViewModel by viewModels()
    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
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
        binding.btnSingUp.setOnClickListener {
            viewModel.register(
                email = binding.tieEmail.text.toString(),
                pass = binding.tiePass.text.toString()
            ) {
                navigateToDetail()
            }
        }
    }

    private fun navigateToDetail() {
        startActivity(Intent(this, DetailActivity::class.java))
    }


}