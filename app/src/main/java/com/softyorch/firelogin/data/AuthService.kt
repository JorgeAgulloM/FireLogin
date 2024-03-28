package com.softyorch.firelogin.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthService @Inject constructor(private val firebaseAuth: FirebaseAuth) {

    suspend fun login(user: String, pass: String): FirebaseUser? =
        firebaseAuth.signInWithEmailAndPassword(user, pass).addOnFailureListener {
            Log.e("FireLoginTag", "Error in login!!!! ${it.message}")
        }.addOnCompleteListener {
            Log.i("FireLoginTag", "Success login!!!!")
        }.await().user

}