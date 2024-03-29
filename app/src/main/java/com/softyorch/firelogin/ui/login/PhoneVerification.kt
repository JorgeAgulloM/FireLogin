package com.softyorch.firelogin.ui.login

sealed class PhoneVerification {
    object CodeSend: PhoneVerification()
    object VerifiedPhoneComplete: PhoneVerification()
    data class VerifiedPhoneFailure(val msg: String): PhoneVerification()
}
