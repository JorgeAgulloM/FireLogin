package com.softyorch.firelogin.ui.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.softyorch.firelogin.data.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private lateinit var verificationCode: String

    fun login(user: String, pass: String, navigateToDetails: () -> Unit) {
        if (user.isNotEmpty() && pass.isNotEmpty()) {
            viewModelScope.launch {
                _isLoading.value = true

                val result = withContext(Dispatchers.IO) {
                    authService.login(user, pass)
                }

                if (result != null) {
                    navigateToDetails()
                } else {

                }

                _isLoading.value = false
            }
        }
    }

    fun verifyCode(phoneCode: String, onSuccessVerification: () -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                authService.verifyCode(verificationCode, phoneCode)
            }

            if (result != null) {
                onSuccessVerification()
            }
        }
    }

    fun loginWithPhone(phoneNumber: String, activity: Activity, onCallback: (PhoneVerification) -> Unit) {
        if (phoneNumber.isNotEmpty()) viewModelScope.launch {
            _isLoading.update { true }

            val callback = onVerificationStateChangedCallbacks(onCallback)

            withContext(Dispatchers.IO) {
                authService.loginWithPhone(phoneNumber, activity, callback)
            }

            _isLoading.update { false }
        }
    }

    fun loginWithGoogle(idToken: String, navigateToDetails: () -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                authService.loginWithGoogle(idToken)
            }

            if (result != null) {
                navigateToDetails()
            }
        }
    }

    fun onGoogleLoginSelected(googleLauncherLogin: (GoogleSignInClient) -> Unit) {
        val gsc = authService.getGoogleClient()
        googleLauncherLogin(gsc)
    }

    private fun onVerificationStateChangedCallbacks(onCallback: (PhoneVerification) -> Unit) =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credentials: PhoneAuthCredential) {
                viewModelScope.launch {

                    val result = withContext(Dispatchers.IO) {
                        authService.completeRegisterWithPhoneVerification(credentials)
                    }

                    if (result != null) {
                        onCallback(PhoneVerification.VerifiedPhoneComplete)
                    }
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                _isLoading.update { false }
                onCallback(PhoneVerification.VerifiedPhoneFailure(exception.message.orEmpty()))
            }

            override fun onCodeSent(verificationCode: String, p1: PhoneAuthProvider.ForceResendingToken) {
                _isLoading.update { false }
                this@LoginViewModel.verificationCode = verificationCode
                onCallback(PhoneVerification.CodeSend)
            }
        }

    fun loginWithFacebook(accessToken: AccessToken, navigateToDetail: () -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                authService.loginWithFacebook(accessToken)
            }

            if (result != null) {
                navigateToDetail()
            }
        }
    }

}