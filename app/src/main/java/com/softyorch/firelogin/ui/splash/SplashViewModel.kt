package com.softyorch.firelogin.ui.splash

import androidx.lifecycle.ViewModel
import com.softyorch.firelogin.data.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val authService: AuthService) : ViewModel() {

    fun checkDestination(): SplashDestination = when (authService.isUserLogged()) {
        true -> SplashDestination.Home
        else -> SplashDestination.Login
    }

}