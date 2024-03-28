package com.softyorch.firelogin.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softyorch.firelogin.data.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
}