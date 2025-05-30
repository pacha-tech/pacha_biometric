import 'package:flutter_test/flutter_test.dart';
import 'package:pacha_biometric/pacha_biometric.dart';
import 'package:pacha_biometric/pacha_biometric_platform_interface.dart';
import 'package:pacha_biometric/pacha_biometric_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockPachaBiometricPlatform
    with MockPlatformInterfaceMixin
    implements PachaBiometricPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final PachaBiometricPlatform initialPlatform = PachaBiometricPlatform.instance;

  test('$MethodChannelPachaBiometric is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelPachaBiometric>());
  });

  test('getPlatformVersion', () async {
    PachaBiometric pachaBiometricPlugin = PachaBiometric();
    MockPachaBiometricPlatform fakePlatform = MockPachaBiometricPlatform();
    PachaBiometricPlatform.instance = fakePlatform;

    expect(await pachaBiometricPlugin.getPlatformVersion(), '42');
  });
}
