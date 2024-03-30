package com.softyorch.firelogin.ui.login

sealed class OAuthLogin {
    object GitHub: OAuthLogin()
    object Microsoft: OAuthLogin()
    object Twitter: OAuthLogin()
    object Yahoo: OAuthLogin()
}
