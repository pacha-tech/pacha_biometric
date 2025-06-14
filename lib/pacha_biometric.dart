import 'pacha_biometric_platform_interface.dart';

class PachaBiometric {
  Future<String?> getPlatformVersion() {
    return PachaBiometricPlatform.instance.getPlatformVersion();
  }

  Future<bool> canAuthenticate() {
    return PachaBiometricPlatform.instance.canAuthenticate();
  }

  Future<bool> authenticate({bool useFace = false}) {
    return PachaBiometricPlatform.instance.authenticate(useFace: useFace);
  }

  Future<String?> capturePhoto() {
    return PachaBiometricPlatform.instance.capturePhoto();
  }

  Future<void> startService() {
    return PachaBiometricPlatform.instance.startService();
  }
}