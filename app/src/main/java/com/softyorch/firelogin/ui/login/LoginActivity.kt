package com.softyorch.firelogin.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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

    private lateinit var callbackManager: CallbackManager

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

                try {
                    val account = task.getResult(ApiException::class.java)!!
                    viewModel.onLoginGoogleSelected(account.idToken!!) {
                        navigateToDetail()
                        binding.pbLoading.isGone = true
                    }
                } catch (ex: ApiException) {
                    showToast("Ha ocurrido un error: ${ex.message}")
                }
            }
        }

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
                viewModel.onStandardLoginSelected(
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

            btnLoginGoogle.setOnClickListener {
                viewModel.onGoogleLoginSelected { gsc ->
                    pbLoading.isVisible = true
                    googleLauncher.launch(gsc.signInIntent)
                }
            }

            //Facebook
            LoginManager.getInstance().apply {
                this@LoginActivity.callbackManager = CallbackManager.Factory.create()
                registerCallback(
                    this@LoginActivity.callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onCancel() {
                            showToast("Prbamos con otra red social?")
                        }

                        override fun onError(error: FacebookException) {
                            showToast("Ha ocurrido un error ${error.message}")
                        }

                        override fun onSuccess(result: LoginResult) {
                            viewModel.onFacebookLoginSelected(result.accessToken) {
                                navigateToDetail()
                            }
                        }
                    }
                )
                btnLoginFacebook.setOnClickListener {
                    logInWithReadPermissions(
                        activityResultRegistryOwner = this@LoginActivity,
                        callbackManager = this@LoginActivity.callbackManager,
                        permissions = listOf("email", "public_profile")
                    )
                }
            }
            //Facebook end

            btnLoginAnonymously.setOnClickListener {
                viewModel.onAnonymouslyLoginSelected {
                    navigateToDetail()
                }
            }

            btnLoginGitHub.setOnClickListener {
                viewModel.onOauthLoginSelected(OAuthLogin.GitHub, this@LoginActivity) {
                    navigateToDetail()
                }
            }

            btnLoginMicrosoft.setOnClickListener {
                viewModel.onOauthLoginSelected(OAuthLogin.Microsoft, this@LoginActivity) {
                    navigateToDetail()
                }
            }

            btnLoginTwitter.setOnClickListener {
                viewModel.onOauthLoginSelected(OAuthLogin.Twitter, this@LoginActivity) {
                    navigateToDetail()
                }
            }

            btnLoginYahoo.setOnClickListener {
                viewModel.onOauthLoginSelected(OAuthLogin.Yahoo, this@LoginActivity) {
                    navigateToDetail()
                }
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

                viewModel.onPhoneLoginSelected(
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