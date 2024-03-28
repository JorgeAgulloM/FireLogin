package com.softyorch.firelogin.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
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

    private fun getCurrentUser() = firebaseAuth.currentUser

}