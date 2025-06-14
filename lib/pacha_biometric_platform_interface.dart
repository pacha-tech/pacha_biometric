import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

/// The interface that implementations of pacha_biometric must implement.
///
/// Platform implementations should extend this class rather than implement it as `pacha_biometric`
/// does not consider newly added methods to be breaking changes. Extending this class
/// (using `extends`) ensures that the subclass will get the default implementation, while
/// platform implementations that `implements` this interface will be broken by newly added
/// [PachaBiometricPlatform] methods.
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
    throw UnimplementedError('getPlatformVersion() na pas été implémenté.');
  }

  Future<bool> canAuthenticate() {
    throw UnimplementedError('canAuthenticate() na pas été implémenté.');
  }

  Future<bool> authenticate({required bool useFace}) {
    throw UnimplementedError('authenticate() na pas été implémenté.');
  }

  Future<String?> capturePhoto() {
    throw UnimplementedError('capturePhoto() na pas été implémenté.');
  }

  Future<void> startService() {
    throw UnimplementedError('startService() na pas été implémenté.');
  }
}

class MethodChannelPachaBiometric extends PachaBiometricPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('com.example.pacha_biometric/system'); // Aligné avec PachaBiometricPlugin.kt

  @override
  Future<String?> getPlatformVersion() async {
    final String? version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> canAuthenticate() async {
    final bool? canAuthenticate = await methodChannel.invokeMethod<bool>('canAuthenticate');
    return canAuthenticate ?? false;
  }

  @override
  Future<bool> authenticate({bool useFace = false}) async {
    final bool? isAuthenticated = await methodChannel.invokeMethod<bool>(
      'authenticate',
      {'useFace': useFace},
    );
    return isAuthenticated ?? false;
  }

  @override
  Future<String?> capturePhoto() async {
    final String? photoPath = await methodChannel.invokeMethod<String>('capturePhoto');
    return photoPath;
  }

  @override
  Future<void> startService() async {
    await methodChannel.invokeMethod<void>('startService'); // Appelle la méthode native
  }
}