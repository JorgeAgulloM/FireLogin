package com.softyorch.firelogin.data

import android.app.Activity
import android.content.Context
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.softyorch.firelogin.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {

    suspend fun login(user: String, pass: String): FirebaseUser? =
        firebaseAuth.signInWithEmailAndPassword(user, pass).await().user

    suspend fun loginAnonymously(): FirebaseUser? = firebaseAuth.signInAnonymously().await().user

    fun loginWithPhone(
        phoneNumber: String,
        activity: Activity,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {

        //Solo para pruebas para evitar la creación de la cuenta.
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

    suspend fun loginWithGoogle(idToken: String): FirebaseUser? {
        val credentials = GoogleAuthProvider.getCredential(idToken, null)
        return completeRegisterWithCredentials(credentials)
    }

    suspend fun loginWithFacebook(accessToken: AccessToken): FirebaseUser? {
        val credentials = FacebookAuthProvider.getCredential(accessToken.token)
        return completeRegisterWithCredentials(credentials)
    }

    suspend fun loginWithGitHub(activity: Activity): FirebaseUser? {
        val provider = OAuthProvider.newBuilder("github.com").apply {
            scopes = listOf("user:email")
        }.build()

        return initRegisterWithProvider(activity, provider)
    }

    suspend fun loginWithMicrosoft(activity: Activity): FirebaseUser? {
        val provider = OAuthProvider.newBuilder("microsoft.com").apply {
            scopes = listOf("mail.read", "calendars.read")
        }.build()

        return initRegisterWithProvider(activity, provider)
    }

    suspend fun loginWithTwitter(activity: Activity): FirebaseUser? {
        val provider = OAuthProvider.newBuilder("twitter.com").build()

        return initRegisterWithProvider(activity, provider)
    }

    suspend fun loginWithYahoo(activity: Activity): FirebaseUser? {
        val provider = OAuthProvider.newBuilder("yahoo.com").build()

        return initRegisterWithProvider(activity, provider)
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
        getGoogleClient().signOut()
        LoginManager.getInstance().logOut()
    }

    fun getGoogleClient(): GoogleSignInClient {
        // cuando llamamos la primera vez a R.string.default_web_client_id no existe, pero al terminar el build y buildear la
        // app se autogenera dicho string
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun completeRegisterWithPhoneVerification(credentials: AuthCredential): FirebaseUser? =
        completeRegisterWithCredentials(credentials)

    fun isUserLogged(): Boolean = getCurrentUser() != null

    suspend fun verifyCode(verificationCode: String, phoneCode: String): FirebaseUser? {
        val credentials = PhoneAuthProvider.getCredential(verificationCode, phoneCode)
        return completeRegisterWithCredentials(credentials)
    }

    private suspend fun completeRegisterWithCredentials(credentials: AuthCredential): FirebaseUser? =
        suspendCancellableCoroutine { cancellableContinuation ->
            firebaseAuth.signInWithCredential(credentials)
                .addOnSuccessListener { authResult ->
                    cancellableContinuation.resume(authResult.user)
                }
                .addOnFailureListener { ex ->
                    cancellableContinuation.resumeWithException(ex)
                }
        }


    private suspend fun initRegisterWithProvider(
        activity: Activity,
        provider: OAuthProvider
    ): FirebaseUser? {
        return suspendCancellableCoroutine { cancellableContinuation ->
            firebaseAuth.pendingAuthResult?.addOnSuccessListener { authResult ->
                cancellableContinuation.resume(authResult.user)
            }?.addOnFailureListener { ex ->
                cancellableContinuation.resumeWithException(ex)
            } ?: completeRegisterWithProvider(activity, provider, cancellableContinuation)
        }
    }

    private fun completeRegisterWithProvider(
        activity: Activity,
        provider: OAuthProvider,
        cancellableContinuation: CancellableContinuation<FirebaseUser?>
    ) {
        firebaseAuth.startActivityForSignInWithProvider(activity, provider)
            .addOnSuccessListener { authResume ->
                cancellableContinuation.resume(authResume.user)
            }.addOnFailureListener { ex ->
                cancellableContinuation.resumeWithException(ex)
            }
    }

    private fun getCurrentUser() = firebaseAuth.currentUser

}
