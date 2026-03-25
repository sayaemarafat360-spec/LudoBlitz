package com.ludoblitz.app.data.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ludoblitz.app.BuildConfig
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    val currentUser get() = auth.currentUser
    val userId get() = auth.currentUser?.uid
    val isLoggedIn get() = auth.currentUser != null

    suspend fun signInEmail(email: String, pass: String): Result<String> = try {
        val r = auth.signInWithEmailAndPassword(email, pass).await()
        Result.success(r.user!!.uid)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signUpEmail(email: String, pass: String, name: String): Result<String> = try {
        val r = auth.createUserWithEmailAndPassword(email, pass).await()
        Result.success(r.user!!.uid)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signInGoogle(token: String): Result<String> = try {
        val cred = GoogleAuthProvider.getCredential(token, null)
        val r = auth.signInWithCredential(cred).await()
        Result.success(r.user!!.uid)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun signInGuest(): Result<String> = try {
        val r = auth.signInAnonymously().await()
        Result.success(r.user!!.uid)
    } catch (e: Exception) { Result.failure(e) }

    fun signOut() { auth.signOut() }
}
