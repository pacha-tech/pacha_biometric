import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'pacha_biometric_platform_interface.dart';

/// Une implémentation de [PachaBiometricPlatform] qui utilise des canaux de méthode.
class MethodChannelPachaBiometric extends PachaBiometricPlatform {
  /// Le canal de méthode utilisé pour interagir avec la plateforme native.
  @visibleForTesting
  final MethodChannel methodChannel = const MethodChannel('pacha_biometric');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> authenticate() async {
    final result = await methodChannel.invokeMethod<String>('authenticate');
    return result;
  }
}