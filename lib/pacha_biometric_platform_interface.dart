import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'pacha_biometric_method_channel.dart';

abstract class PachaBiometricPlatform extends PlatformInterface {
  PachaBiometricPlatform() : super(token: _token);

  static final Object _token = Object();

  static PachaBiometricPlatform _instance = MethodChannelPachaBiometric();

  static PachaBiometricPlatform get instance => _instance;

  static set instance(PachaBiometricPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('getPlatformVersion() has not been implémentée.');
  }

  /// Authentifie l'utilisateur. Renvoie un message en cas de succès.
  Future<String?> authenticate();
}
