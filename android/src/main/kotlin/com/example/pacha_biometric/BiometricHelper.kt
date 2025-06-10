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

    fun authenticate(useFace: Boolean, callback: BiometricCallback) {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = if (useFace) {
            BiometricManager.Authenticators.BIOMETRIC_WEAK // Prioriser la reconnaissance faciale
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG // Prioriser l'empreinte
        }
        val canAuthenticate = biometricManager.canAuthenticate(authenticators)

        Log.d(TAG, "Vérification biométrique (useFace=$useFace) : code=$canAuthenticate")

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            val errorMessage = when (canAuthenticate) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Aucun capteur ${if (useFace) "facial" else "d'empreinte"} disponible."
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Capteur ${if (useFace) "facial" else "d'empreinte"} indisponible."
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Aucune ${if (useFace) "donnée faciale" else "empreinte"} enregistrée. Ajoutez-en dans les paramètres."
                else -> "${if (useFace) "Reconnaissance faciale" else "Biométrie"} non configurée (code: $canAuthenticate)."
            }
            Log.e(TAG, "Échec de disponibilité : $errorMessage")
            callback.onError(errorMessage)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val authType = when (result.authenticationType) {
                        BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> if (useFace) "Reconnaissance faciale" else "Empreinte digitale"
                        BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> "PIN/Motif"
                        else -> "Inconnu"
                    }
                    Log.d(TAG, "✅ Authentification réussie (type: $authType)")
                    callback.onSuccess("Authentification réussie.")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "❌ Erreur biométrique [code: $errorCode]: $errString")
                    callback.onError("Erreur ${if (useFace) "faciale" else "biométrique"} [code: $errorCode]: $errString")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "👎 ${if (useFace) "Visage" else "Empreinte"} capté mais non reconnu.")
                    callback.onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentification ${if (useFace) "faciale" else "biométrique"}")
            .setSubtitle("Utilisez votre ${if (useFace) "visage" else "empreinte digitale"}")
            .setNegativeButtonText("Annuler")
            .setAllowedAuthenticators(authenticators)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
            Log.d(TAG, "📲 Authentification ${if (useFace) "faciale" else "biométrique"} démarrée")
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Exception lors du démarrage : ${e.localizedMessage}", e)
            callback.onError("Erreur inattendue : ${e.localizedMessage ?: "inconnue"}")
        }
    }
}