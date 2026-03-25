package com.ludoblitz.app.data.firebase

import android.content.Context
import com.ludoblitz.app.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Authentication Manager
 * Handles all authentication: Email, Google, Guest
 */
@Singleton
class FirebaseAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        // Using Web Client ID from BuildConfig (loaded from google-services.json)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    // Current user flow
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean = auth.currentUser != null
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    /**
     * Get current user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Sign in with Email and Password
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            throw mapAuthException(e)
        }
    }
    
    /**
     * Sign up with Email and Password
     */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            // Update profile with display name
            result.user?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
            )?.await()
            result
        } catch (e: Exception) {
            throw mapAuthException(e)
        }
    }
    
    /**
     * Sign in with Google
     */
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
        } catch (e: Exception) {
            throw mapAuthException(e)
        }
    }
    
    /**
     * Sign in as Guest (Anonymous)
     */
    suspend fun signInAsGuest(): AuthResult {
        return try {
            auth.signInAnonymously().await()
        } catch (e: Exception) {
            throw mapAuthException(e)
        }
    }
    
    /**
     * Convert Guest account to Permanent account
     */
    suspend fun convertGuestToPermanent(email: String, password: String): AuthResult {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            user.linkWithCredential(credential).await()
        } catch (e: Exception) {
            throw mapAuthException(e)
        }
    }
    
    /**
     * Link Google account to current account
     */
    suspend fun linkGoogleAccount(idToken: String): AuthResult {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            user.linkWithCredential(credential).await()
        } catch (e: Exception) {
            throw mapAuthException(e)
        }
    }
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            throw mapAuthException(e)
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        
        val profileUpdates = UserProfileChangeRequest.Builder()
        displayName?.let { profileUpdates.setDisplayName(it) }
        photoUrl?.let { profileUpdates.setPhotoUri(android.net.Uri.parse(it)) }
        
        user.updateProfile(profileUpdates.build()).await()
    }
    
    /**
     * Update email
     */
    suspend fun updateEmail(newEmail: String) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.updateEmail(newEmail).await()
    }
    
    /**
     * Update password
     */
    suspend fun updatePassword(newPassword: String) {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.updatePassword(newPassword).await()
    }
    
    /**
     * Sign out
     */
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }
    
    /**
     * Delete account
     */
    suspend fun deleteAccount() {
        val user = auth.currentUser ?: throw Exception("No user logged in")
        user.delete().await()
    }
    
    /**
     * Get Google Sign In Intent
     */
    fun getGoogleSignInIntent(): android.content.Intent {
        return googleSignInClient.signInIntent
    }
    
    /**
     * Map Firebase exceptions to user-friendly messages
     */
    private fun mapAuthException(e: Exception): Exception {
        return when (e) {
            is FirebaseAuthInvalidUserException -> Exception("User not found or disabled")
            is FirebaseAuthInvalidCredentialsException -> Exception("Invalid email or password")
            is FirebaseAuthUserCollisionException -> Exception("An account already exists with this email")
            is FirebaseAuthWeakPasswordException -> Exception("Password is too weak")
            is FirebaseAuthEmailException -> Exception("Invalid email address")
            is FirebaseAuthTooManyRequestsException -> Exception("Too many failed attempts. Try again later")
            else -> Exception(e.message ?: "Authentication failed")
        }
    }
}
