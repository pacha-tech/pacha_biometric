package com.example.pacha_biometric

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class PachaBiometricPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel
    private var context: Context? = null
    private var activity: Activity? = null
    private var pendingResult: Result? = null
    private val TAG = "PachaBiometricPlugin"

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "pacha_biometric")
        channel.setMethodCallHandler(this)
        Log.d(TAG, "Plugin attached to engine")
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.d(TAG, "Received method call: ${call.method}")
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "canAuthenticate" -> {
                if (context == null) {
                    Log.e(TAG, "Context is null")
                    result.error("NO_CONTEXT", "Contexte non disponible.", null)
                    return
                }
                val biometricManager = BiometricManager.from(context!!)
                val canAuthenticate = biometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                Log.d(TAG, "Can authenticate: $canAuthenticate")
                result.success(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
            }
            "authenticate" -> {
                if (activity == null) {
                    Log.e(TAG, "Activity is null")
                    result.error("NO_ACTIVITY", "Aucune activité liée à l'authentification.", null)
                    return
                }

                if (pendingResult != null) {
                    Log.w(TAG, "Authentication already in progress")
                    result.error("AUTH_IN_PROGRESS", "Une authentification est déjà en cours.", null)
                    return
                }

                pendingResult = result

                val useFace = call.argument<Boolean>("useFace") ?: false
                Log.d(TAG, "Authenticate called with useFace=$useFace")

                val biometricHelper = BiometricHelper(activity as FragmentActivity)
                biometricHelper.authenticate(useFace, object : BiometricHelper.BiometricCallback {
                    override fun onSuccess(message: String) {
                        Log.d(TAG, "Authentication succeeded: $message")
                        safeSuccess(true)
                    }

                    override fun onError(errorMessage: String) {
                        Log.e(TAG, "Authentication error: $errorMessage")
                        safeError("AUTH_ERROR", errorMessage)
                    }

                    override fun onFailed() {
                        Log.w(TAG, "Authentication failed")
                        safeSuccess(false)
                    }
                })
            }
            else -> {
                Log.w(TAG, "Method not implemented: ${call.method}")
                result.notImplemented()
            }
        }
    }

    private fun safeSuccess(value: Boolean) {
        pendingResult?.success(value)
        pendingResult = null
    }

    private fun safeError(code: String, message: String) {
        pendingResult?.error(code, message, null)
        pendingResult = null
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        context = null
        Log.d(TAG, "Plugin detached from engine")
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        Log.d(TAG, "Attached to activity")
    }

    override fun onDetachedFromActivity() {
        activity = null
        Log.d(TAG, "Detached from activity")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        Log.d(TAG, "Reattached to activity for config changes")
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        Log.d(TAG, "Detached from activity for config changes")
    }
}