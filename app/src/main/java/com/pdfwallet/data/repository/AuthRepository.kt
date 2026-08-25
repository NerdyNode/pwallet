package com.pdfwallet.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.pdfwallet.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    @ApplicationScope private val appScope: CoroutineScope
) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)
    
    // We get this from the user's setup
    private val webClientId = com.pdfwallet.BuildConfig.WEB_CLIENT_ID

    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    suspend fun signInWithGoogle(activityContext: Context): Result<Unit> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()
                
            val signInWithGoogleOption = com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            
            // Try CustomCredential wrapping GoogleIdTokenCredential
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                auth.signInWithCredential(firebaseCredential).await()
                
                // Save user details for profile and backup/sync
                auth.currentUser?.let { user ->
                    user.email?.let { settingsRepository.setUserEmail(it) }
                    user.displayName?.let { settingsRepository.setUserName(it) }
                    user.photoUrl?.let { settingsRepository.setUserImage(it.toString()) }
                }

                _isUserLoggedIn.value = true
                Result.success(Unit)
            } catch(e: Exception) {
                Result.failure(Exception("Unsupported credential type"))
            }
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Result.failure(Exception("No Google account found on device. Please add one in Settings."))
        } catch (e: GetCredentialException) {
            Result.failure(Exception(e.localizedMessage ?: "Failed to get credentials"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
        appScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            settingsRepository.setUserEmail(null)
            settingsRepository.setUserName("Wallet User")
            settingsRepository.setUserImage(null)
        }
        _isUserLoggedIn.value = false
    }
}
