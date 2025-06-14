import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'pacha_biometric_platform_interface.dart';

class MethodChannelPachaBiometric extends PachaBiometricPlatform {
  @visibleForTesting
  final methodChannel = MethodChannel('com.example.pacha_biometric/system'); // Aligné avec PachaBiometricPlugin.kt

  @override
  Future<String?> getPlatformVersion() async {
    final String? version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> canAuthenticate() async {
    final bool? canAuthenticate = await methodChannel.invokeMethod<bool>('canAuthenticate');
    return canAuthenticate ?? false;
  }

  @override
  Future<bool> authenticate({bool useFace = false}) async {
    final bool? isAuthenticated = await methodChannel.invokeMethod<bool>(
      'authenticate',
      {'useFace': useFace},
    );
    return isAuthenticated ?? false;
  }

  @override
  Future<String?> capturePhoto() async {
    final String? photoPath = await methodChannel.invokeMethod<String>('capturePhoto');
    return photoPath;
  }

  @override
  Future<void> startService() async {
    await methodChannel.invokeMethod<void>('startService'); // Appelle la méthode native
  }
}