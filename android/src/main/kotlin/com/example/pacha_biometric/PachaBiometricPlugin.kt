package com.example.pacha_biometric

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.NonNull
import androidx.biometric.BiometricManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import androidx.camera.core.ImageReader

class PachaBiometricPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var context: Context? = null
    private var activity: Activity? = null
    private var pendingResult: Result? = null
    private val TAG = "PachaBiometricPlugin"
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null
    private var permissionResult: Result? = null
    private val CAMERA_PERMISSION_CODE = 1001

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.example.pacha_biometric/system")
        channel.setMethodCallHandler(this)
        Log.d(TAG, "Plugin attached to engine")
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.d(TAG, "Received method call: ${call.method}")
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
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
                Log.d(TAG, "Can authenticate (all biometrics): $canAuthenticate")
                result.success(canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
            }
            "canAuthenticateFace" -> {
                if (context == null) {
                    Log.e(TAG, "Context is null")
                    result.error("NO_CONTEXT", "Contexte non disponible.", null)
                    return
                }
                val biometricManager = BiometricManager.from(context!!)
                val canAuthenticate = biometricManager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                Log.d(TAG, "Can authenticate face: $canAuthenticate")
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
                    override fun onError(errorMessage: String?) {
                        Log.e(TAG, "Authentication error: $errorMessage")
                        safeError("AUTH_ERROR", errorMessage ?: "Erreur inconnue")
                    }
                    override fun onFailed() {
                        Log.w(TAG, "Authentication failed")
                        safeSuccess(false)
                    }
                })
            }
            "capturePhoto" -> {
                if (activity == null || context == null) {
                    Log.e(TAG, "Activity or context is null")
                    result.error("NO_ACTIVITY", "Aucune activité pour la caméra.", null)
                    return
                }
                if (ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Requesting camera photo")
                    permissionResult = result
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(android.Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_CODE
                    )
                    return
                }
                capturePhoto(result)
            }
            "startService" -> {
                if (context == null) {
                    Log.e(TAG, "Context is null")
                    result.error("NO_CONTEXT", "Contexte non disponible.", null)
                    return
                }
                val serviceIntent = Intent(context, LockScreenService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context?.startForegroundService(serviceIntent)
                } else {
                    context?.startService(serviceIntent)
                }
                Log.d(TAG, "Service started")
                result.success(true)
            }
            else -> {
                Log.w(TAG, "Method not implemented: ${call.method}")
                result.error("NOT_IMPLEMENTED", "${call.method} not implemented", null)
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted")
                permissionResult?.let { capturePhoto(it) }
            } else {
                Log.e(TAG, "Camera permission denied")
                permissionResult?.error("PERMISSION_DENIED", "Permission de caméra refusée.", null)
            }
            permissionResult = null
        }
    }

    private fun capturePhoto(result: Result) {
        try {
            val cameraManager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val frontCameraId = cameraManager.cameraIdList.find { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            } ?: run {
                Log.e(TAG, "No front camera found")
                result.error("NO_CAMERA", "Aucune caméra frontale disponible.", null)
                return
            }

            handlerThread = HandlerThread("CameraBackground")
            handlerThread?.start()
            handler = Handler(handlerThread!!.looper)

            cameraManager.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    Log.d(TAG, "Camera opened successfully")
                    takePicture(result)
                }
                override fun onDisconnected(camera: CameraDevice) {
                    Log.e(TAG, "Camera disconnected")
                    closeCamera()
                    result.error("CAMERA_DISCONNECTED", "Caméra déconnectée.", null)
                }
                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Camera error: $error")
                    closeCamera()
                    result.error("CAMERA_ERROR", "Erreur caméra: $error", null)
                }
            }, handler)
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing photo: ${e.localizedMessage}", e)
            result.error("CAMERA_ERROR", "Erreur capture photo: ${e.localizedMessage}", null)
        }
    }

    private fun takePicture(result: Result) {
        try {
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            val outputFile = createOutputFile()
            val imageReader: ImageReader = ImageReader.newInstance(640, 480, android.graphics.ImageFormat.JPEG, 1) // Type explicite
            captureBuilder.addTarget(imageReader.surface)

            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireNextImage()
                try {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    FileOutputStream(outputFile).use { it.write(bytes) }
                    Log.d(TAG, "Photo saved to: ${outputFile.absolutePath}")
                    val path = outputFile.absolutePath // Gestion explicite de String?
                    result.success(path)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving photo: ${e.localizedMessage}", e)
                    result.error("SAVE_ERROR", "Erreur sauvegarde photo: ${e.localizedMessage}", null)
                } finally {
                    image.close()
                    closeCamera()
                }
            }, handler)

            cameraDevice!!.createCaptureSession(
                listOf(imageReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        try {
                            session.capture(captureBuilder.build(), null, handler)
                        } catch (e: Exception) {
                            Log.e(TAG, "Capture failed: ${e.localizedMessage}", e)
                            result.error("CAPTURE_ERROR", "Échec capture: ${e.localizedMessage}", null)
                            closeCamera()
                        }
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Capture session configuration failed")
                        result.error("CONFIG_ERROR", "Échec configuration session caméra.", null)
                        closeCamera()
                    }
                },
                handler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error taking picture: ${e.localizedMessage}", e)
            result.error("CAMERA_ERROR", "Erreur prise photo: ${e.localizedMessage}", null)
            closeCamera()
        }
    }

    private fun createOutputFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "anti_theft_$timeStamp.jpg")
    }

    private fun closeCamera() {
        try {
            cameraCaptureSession?.close()
            cameraCaptureSession = null
            cameraDevice?.close()
            cameraDevice = null
            handlerThread?.quitSafely()
            handlerThread = null
            handler = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing camera: ${e.localizedMessage}", e)
        }
    }

    private fun safeSuccess(value: Boolean) {
        pendingResult?.success(value)
        pendingResult = null
    }

    private fun safeError(code: String?, message: String) {
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
        binding.addRequestPermissionsResultListener { requestCode, permissions, grantResults ->
            onRequestPermissionsResult(requestCode, permissions, grantResults)
            true
        }
        Log.d(TAG, "Attached to activity")
    }

    override fun onDetachedFromActivity() {
        activity = null
        Log.d(TAG, "Detached from activity")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener { requestCode, permissions, grantResults ->
            onRequestPermissionsResult(requestCode, permissions, grantResults)
            true
        }
        Log.d(TAG, "Reattached to activity for config changes")
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        Log.d(TAG, "Detached from activity for config changes")
    }
}