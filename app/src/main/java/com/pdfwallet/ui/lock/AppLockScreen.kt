package com.pdfwallet.ui.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfwallet.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    onUnlocked: () -> Unit,
    viewModel: AppLockViewModel = hiltViewModel()
) {
    var context = LocalContext.current
    while (context is android.content.ContextWrapper) {
        if (context is FragmentActivity) break
        context = context.baseContext
    }
    val fragmentActivity = context as FragmentActivity
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun triggerShake() {
        scope.launch {
            shakeOffset.snapTo(30f)
            shakeOffset.animateTo(0f, spring(dampingRatio = 0.3f, stiffness = 500f))
        }
    }

    fun authenticate() {
        val biometricManager = BiometricManager.from(fragmentActivity)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(fragmentActivity)
                val biometricPrompt = BiometricPrompt(fragmentActivity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            errorMsg = "Authentication error: $errString"
                            triggerShake()
                        }
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onUnlocked()
                        }
                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            errorMsg = "Authentication failed"
                            triggerShake()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock PDF Wallet")
                    .setSubtitle("Use your biometric credential to access the wallet")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                errorMsg = "No biometrics enrolled. Please set up fingerprint or face unlock in device settings."
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                errorMsg = "Biometric hardware unavailable."
            }
            else -> {
                errorMsg = "Authentication unavailable. Please try again."
            }
        }
    }

    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled == false) {
            onUnlocked()
        } else if (isBiometricEnabled == true) {
            authenticate()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(Dimens.ScreenPaddingLarge)
                    .offset(x = androidx.compose.ui.unit.Dp(shakeOffset.value))
            ) {
                Column(
                    modifier = Modifier
                        .padding(Dimens.SpacingExtraLarge)
                        .fillMaxWidth(0.8f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(Dimens.IconHero)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = "Fingerprint icon",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(Dimens.SpacingLarge).fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                    
                    Text("App Locked", style = MaterialTheme.typography.headlineMedium)
                    
                    Spacer(modifier = Modifier.height(Dimens.SpacingNormal))
                    
                    errorMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(Dimens.SpacingNormal))
                    }
                    
                    Button(onClick = { authenticate() }) {
                        Text("Unlock")
                    }
                }
            }
        }
    }
}
