package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "GoogleAuthManager"
    }

    private val credentialManager: CredentialManager = CredentialManager.create(context)

    fun saveWebClientId(clientId: String) {
        context.getSharedPreferences("google_auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("custom_web_client_id", clientId.trim())
            .apply()
    }

    /**
     * Resolves the Web Client ID required for Google ID Token verification.
     * Looks in:
     * 1. Saved custom web client id in SharedPreferences
     * 2. Auto-generated R.string.default_web_client_id from google-services plugin
     */
    fun getWebClientId(): String {
        try {
            val stored = context.getSharedPreferences("google_auth_prefs", Context.MODE_PRIVATE)
                .getString("custom_web_client_id", "") ?: ""
            if (stored.isNotBlank()) return stored

            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val found = context.getString(resId)
                if (found.isNotBlank()) return found
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error resolving web client id: ${e.message}")
        }
        return "834023963943-59ss20eq9jgje4qvg413iudptu29kvnu.apps.googleusercontent.com"
    }

    suspend fun signInWithGoogle(
        activity: Activity,
        customServerClientId: String? = null
    ): Result<FirebaseUser> {
        val serverClientId = customServerClientId?.takeIf { it.isNotBlank() }
            ?: getWebClientId()

        if (serverClientId.isBlank()) {
            val msg = "Google Sign-In setup: Web Client ID is not configured yet in Firebase Console. Please enable Google provider under Firebase Authentication -> Sign-in method, then download the updated google-services.json."
            Log.w(TAG, msg)
            return Result.failure(IllegalStateException(msg))
        }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val auth = FirebaseAuth.getInstance()
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user

                return if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Firebase returned empty user credentials."))
                }
            } else {
                return Result.failure(Exception("Unsupported credential type received: ${credential::class.java.name}"))
            }

        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In")
            return Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google accounts available on this device", e)
            return Result.failure(Exception("No Google account selected or available on this device."))
        } catch (e: FirebaseNetworkException) {
            Log.e(TAG, "Network failure during Google Auth", e)
            return Result.failure(Exception("Network error. Please check your internet connection and try again."))
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase Auth error code: ${e.errorCode}", e)
            return Result.failure(Exception("Authentication failed: ${e.localizedMessage}"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error: ${e.message}", e)
            return Result.failure(Exception(e.message ?: "Google Sign-In failed."))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in signInWithGoogle", e)
            return Result.failure(Exception(e.localizedMessage ?: "Unexpected error during Google Sign-In."))
        }
    }
}
