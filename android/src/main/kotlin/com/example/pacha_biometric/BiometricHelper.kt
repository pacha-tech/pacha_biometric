package com.example.pacha_biometric

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val activity: FragmentActivity) {

    interface BiometricCallback {
        fun onSuccess(message: String)
        fun onError(errorMessage: String)
        fun onFailed()
    }

    fun authenticate(callback: BiometricCallback) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    callback.onSuccess("Authentication succeeded")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    callback.onError("Authentication error: $errString")
                }

                override fun onAuthenticationFailed() {
                    callback.onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentification biométrique")
            .setSubtitle("Utilisez votre empreinte digitale")
            .setNegativeButtonText("Annuler")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
