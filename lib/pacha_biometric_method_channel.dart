import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'pacha_biometric_platform_interface.dart';

/// An implementation of [PachaBiometricPlatform] that uses method channels.
class MethodChannelPachaBiometric extends PachaBiometricPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('pacha_biometric');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<String?> authenticate() async {
    final result = await _channel.invokeMethod<String>('authenticate');
    return result;
  }
}
