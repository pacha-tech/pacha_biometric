import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'pacha_biometric_platform_interface.dart';

/// Implémentation native via MethodChannel
class MethodChannelPachaBiometric extends PachaBiometricPlatform {
  @visibleForTesting
  final MethodChannel methodChannel = const MethodChannel('pacha_biometric');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> authenticate() async {
    try {
      final result = await methodChannel.invokeMethod<String>('authenticate');
      return result;
    } on PlatformException catch (e) {
      // On relance l'exception avec le message d'erreur
      throw Exception(e.message ?? 'Erreur biométrique inconnue');
    }
  }
}
