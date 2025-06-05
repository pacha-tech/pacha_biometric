import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'pacha_biometric_method_channel.dart';

abstract class PachaBiometricPlatform extends PlatformInterface {
  /// Constructeur de PachaBiometricPlatform.
  PachaBiometricPlatform() : super(token: _token);

  static final Object _token = Object();

  static PachaBiometricPlatform _instance = MethodChannelPachaBiometric();

  /// L'instance par défaut de [PachaBiometricPlatform].
  static PachaBiometricPlatform get instance => _instance;

  /// Définit l'instance spécifique à la plateforme.
  static set instance(PachaBiometricPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> authenticate();
}