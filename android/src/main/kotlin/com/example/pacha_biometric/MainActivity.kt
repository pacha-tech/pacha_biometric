      package com.example.pacha_biometric

      import android.content.Intent
      import android.os.Bundle
      import io.flutter.embedding.android.FlutterActivity
      import io.flutter.embedding.engine.FlutterEngine
      import io.flutter.plugin.common.MethodChannel

      class MainActivity: FlutterActivity() {
          private val CHANNEL = "com.example.pacha_biometric/system"

          override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
              super.configureFlutterEngine(flutterEngine)
              MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
                  if (call.method == "startService") {
                      val serviceIntent = Intent(this, LockScreenService::class.java)
                      startForegroundService(serviceIntent)
                      result.success(true)
                  } else {
                      result.notImplemented()
                  }
              }
          }
      }
      