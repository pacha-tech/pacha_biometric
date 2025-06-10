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
  Future<bool> canAuthenticate() async {
    try {
      final bool result = await methodChannel.invokeMethod<bool>('canAuthenticate') ?? false;
      return result;
    } on PlatformException catch (e) {
      throw Exception(e.message ?? 'Erreur lors de la vérification biométrique');
    }
  }

  @override
  Future<bool> authenticate() async {
    try {
      final bool result = await methodChannel.invokeMethod<bool>('authenticate') ?? false;
      return result;
    } on PlatformException catch (e) {
      throw Exception(e.message ?? 'Erreur biométrique inconnue');
    }
  }
}