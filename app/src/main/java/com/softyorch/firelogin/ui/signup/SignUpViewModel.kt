package com.softyorch.firelogin.ui.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SignUpViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {


    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun register(email: String, pass: String, navigateToDetail: () -> Unit) {
        if (email.isNotEmpty() && pass.isNotEmpty()) {
            viewModelScope.launch {
                _isLoading.update { true }

                try {
                    val result = withContext(Dispatchers.IO) {
                        authService.signUp(email, pass)
                    }

                    if (result != null) {
                        navigateToDetail()
                    } else {
                        Log.w("FireLoginTag", "Firebase fail signUp")
                    }

                } catch (e: Exception) {
                    Log.e("FireLoginTag", "Firebase error: ${e.message.orEmpty()}")
                }
                _isLoading.update { false }
            }
        }
    }

}