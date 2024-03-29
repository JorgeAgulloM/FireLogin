package com.softyorch.firelogin.data

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthService @Inject constructor(private val firebaseAuth: FirebaseAuth) {

    suspend fun login(user: String, pass: String): FirebaseUser? =
        suspendCancellableCoroutine { cancellableContinuation ->
            firebaseAuth.signInWithEmailAndPassword(user, pass)
                .addOnSuccessListener {
                    cancellableContinuation.resume(it.user)
                }.addOnFailureListener { except ->
                    cancellableContinuation.resumeWithException(except)
                }
        }

    fun loginWithPhone(
        phoneNumber: String,
        activity: Activity,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {

        //Solo para pruebas
        //firebaseAuth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber("+34 123456789", "123456")

        val options = PhoneAuthOptions
            .newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signUp(email: String, pass: String): FirebaseUser? =
        suspendCancellableCoroutine { cancellableContinuation ->
            firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    cancellableContinuation.resume(it.user)
                }
                .addOnFailureListener { except ->
                    cancellableContinuation.resumeWithException(except)
                }
        }

    fun logout() {
        firebaseAuth.signOut()
    }

    fun isUserLogged(): Boolean = getCurrentUser() != null

    suspend fun verifyCode(verificationCode: String, phoneCode: String): FirebaseUser? {
        val credentials = PhoneAuthProvider.getCredential(verificationCode, phoneCode)
        return completeRegisterWithPhone(credentials)
    }

    suspend fun completeRegisterWithPhone(credentials: PhoneAuthCredential): FirebaseUser? =
        suspendCancellableCoroutine { cancellableContinuation ->
            firebaseAuth.signInWithCredential(credentials)
                .addOnSuccessListener { authResult ->
                    cancellableContinuation.resume(authResult.user)
                }
                .addOnFailureListener { ex ->
                    cancellableContinuation.resumeWithException(ex)
                }
        }

    private fun getCurrentUser() = firebaseAuth.currentUser

}
