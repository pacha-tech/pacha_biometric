import 'package:flutter/services.dart';

class PachaBiometric {
  static const MethodChannel _channel = MethodChannel('pacha_biometric');

  Future<String?> getPlatformVersion() async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<bool> canAuthenticate() async {
    try {
      final bool canAuthenticate = await _channel.invokeMethod('canAuthenticate');
      return canAuthenticate;
    } catch (e) {
      print('Erreur lors de la vérification biométrique : $e');
      return false;
    }
  }

  Future<bool> authenticate({bool useFace = false}) async {
    try {
      final bool isAuthenticated = await _channel.invokeMethod('authenticate', {
        'useFace': useFace,
      });
      return isAuthenticated;
    } catch (e) {
      print('Erreur lors de l\'authentification : $e');
      throw e;
    }
  }
}