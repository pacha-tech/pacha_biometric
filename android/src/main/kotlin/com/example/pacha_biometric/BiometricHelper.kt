package com.example.pacha_biometric

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val activity: FragmentActivity) {

    interface BiometricCallback {
        fun onSuccess(message: String)
        fun onError(errorMessage: String)
        fun onFailed()
    }

    private val TAG = "BiometricHelper"

    fun authenticate(callback: BiometricCallback) {
        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or 
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        Log.d(TAG, "canAuthenticate status: $canAuthenticate")

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            val errorMessage = when (canAuthenticate) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Pas de matériel biométrique disponible."
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Matériel biométrique actuellement indisponible."
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Aucune donnée biométrique enregistrée."
                else -> "Biométrie non disponible ou non configurée."
            }
            Log.e(TAG, errorMessage)
            callback.onError(errorMessage)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentification réussie")
                    callback.onSuccess("Authentification réussie.")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Erreur d'authentification [$errorCode]: $errString")
                    callback.onError("Erreur : $errString")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d(TAG, "Authentification échouée")
                    callback.onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentification requise")
            .setSubtitle("Utilisez votre empreinte digitale pour continuer")
            .setNegativeButtonText("Annuler")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Exception lors de l'authentification biométrique", e)
            callback.onError("Erreur inattendue : ${e.localizedMessage ?: "inconnue"}")
        }
    }
}
