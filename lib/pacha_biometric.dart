
import 'pacha_biometric_platform_interface.dart';

class PachaBiometric {
  Future<String?> getPlatformVersion() {
    return PachaBiometricPlatform.instance.getPlatformVersion();
  }
}
