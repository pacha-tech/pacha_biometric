import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'pacha_biometric_method_channel.dart';

abstract class PachaBiometricPlatform extends PlatformInterface {
  /// Constructs a PachaBiometricPlatform.
  PachaBiometricPlatform() : super(token: _token);

  static final Object _token = Object();

  static PachaBiometricPlatform _instance = MethodChannelPachaBiometric();

  /// The default instance of [PachaBiometricPlatform] to use.
  ///
  /// Defaults to [MethodChannelPachaBiometric].
  static PachaBiometricPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [PachaBiometricPlatform] when
  /// they register themselves.
  static set instance(PachaBiometricPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<String?> authenticate();
}
