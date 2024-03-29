package com.softyorch.firelogin.ui.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.softyorch.firelogin.databinding.ActivityLoginBinding
import com.softyorch.firelogin.databinding.DialogPhoneLoginBinding
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
            btnLoginPhone.setOnClickListener {
                showPhoneLogin()
            }
        }
    }

    private fun showPhoneLogin() {
        val phoneBinding = DialogPhoneLoginBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(this).apply { setView(phoneBinding.root) }.create()

        phoneBinding.apply {
            btnPhone.setOnClickListener {
                pbLoading.isVisible = true
                tiePhone.isEnabled = false
                btnPhone.isEnabled = false
                btnPhone.setTextColor(Color.WHITE)

                viewModel.loginWithPhone(
                    phoneNumber = tiePhone.text.toString(),
                    activity = this@LoginActivity
                ) { phoneVerification ->
                    when (phoneVerification) {
                        PhoneVerification.CodeSend -> {
                            tvHeader.isGone = true
                            tvPinCode.isVisible = true
                            pinView.isVisible = true
                            pbLoading.isGone = true
                            tiePhone.isGone = true
                            btnPhone.isGone = true

                            pinView.requestFocus()
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT)
                        }
                        PhoneVerification.VerifiedPhoneComplete -> navigateToDetail()
                        is PhoneVerification.VerifiedPhoneFailure -> showToast("Error al validar el teléfono: ${phoneVerification.msg}")
                    }
                }
            }
            pinView.doOnTextChanged { text, _, _, _ ->
                if (text?.length == 6) {
                    viewModel.verifyCode(text.toString()) {
                        navigateToDetail()
                    }
                }
            }
        }

        alertDialog.apply {
            show()
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    private fun navigateToDetail() {
        startActivity(Intent(this, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}