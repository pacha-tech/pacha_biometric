import 'pacha_biometric_platform_interface.dart';

class PachaBiometric {
  Future<String?> getPlatformVersion() {
    return PachaBiometricPlatform.instance.getPlatformVersion();
  }

  Future<bool> authenticate() async {
    try {
      final result = await PachaBiometricPlatform.instance.authenticate();
      return result == "Authentication succeeded";
    } catch (e) {
      // Facultatif : log ou traitement de l'erreur
      return false;
    }
  }
}
