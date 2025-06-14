import 'package:flutter_test/flutter_test.dart';

import 'package:pacha_biometric_example/main.dart';

void main() {
  testWidgets('Counter increments smoke test', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MyApp());

    // Verify that our app starts with the correct title.
    expect(find.text('Exemple Pacha Biometric'), findsOneWidget);
    expect(find.text('Activer le verrouillage'), findsOneWidget);

    // Tap the button and trigger a frame.
    await tester.tap(find.text('Activer le verrouillage'));
    await tester.pump();

    // The actual behavior will depend on your plugin implementation
  });
}