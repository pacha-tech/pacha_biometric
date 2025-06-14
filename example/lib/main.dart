import 'package:flutter/material.dart';
import 'package:pacha_biometric/pacha_biometric.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Pacha Biometric Example',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  MyHomePageState createState() => MyHomePageState();
}

class MyHomePageState extends State<MyHomePage> {
  final PachaBiometric _pachaBiometric = PachaBiometric();
  String _statusMessage = 'Prêt à tester';
  
  void _showSnackBar(String message) {
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(message)),
      );
    }
  }
  
  void _updateStatus(String message) {
    if (mounted) {
      setState(() {
        _statusMessage = message;
      });
    }
  }
  
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Exemple Pacha Biometric'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _statusMessage,
              style: Theme.of(context).textTheme.titleMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 30),
            ElevatedButton(
              onPressed: () async {
                try {
                  _updateStatus('Vérification de la compatibilité...');
                  final canAuth = await _pachaBiometric.canAuthenticate();
                  _updateStatus(canAuth ? 'Authentification biométrique disponible' : 'Pas d\'authentification biométrique');
                  _showSnackBar(canAuth ? 'Biométrie disponible' : 'Biométrie non disponible');
                } catch (e) {
                  _updateStatus('Erreur lors de la vérification');
                  _showSnackBar('Erreur : $e');
                }
              },
              child: const Text('Vérifier la compatibilité'),
            ),
            const SizedBox(height: 10),
            ElevatedButton(
              onPressed: () async {
                try {
                  _updateStatus('Authentification en cours...');
                  final isAuthenticated = await _pachaBiometric.authenticate(useFace: false);
                  _updateStatus(isAuthenticated ? 'Authentification réussie' : 'Authentification échouée');
                  _showSnackBar(isAuthenticated ? 'Authentifié avec succès' : 'Échec de l\'authentification');
                } catch (e) {
                  _updateStatus('Erreur lors de l\'authentification');
                  _showSnackBar('Erreur : $e');
                }
              },
              child: const Text('Authentifier (Empreinte)'),
            ),
            const SizedBox(height: 10),
            ElevatedButton(
              onPressed: () async {
                try {
                  _updateStatus('Authentification faciale en cours...');
                  final isAuthenticated = await _pachaBiometric.authenticate(useFace: true);
                  _updateStatus(isAuthenticated ? 'Authentification faciale réussie' : 'Authentification faciale échouée');
                  _showSnackBar(isAuthenticated ? 'Authentifié par visage' : 'Échec de l\'authentification faciale');
                } catch (e) {
                  _updateStatus('Erreur lors de l\'authentification faciale');
                  _showSnackBar('Erreur : $e');
                }
              },
              child: const Text('Authentifier (Visage)'),
            ),
            const SizedBox(height: 10),
            ElevatedButton(
              onPressed: () async {
                try {
                  _updateStatus('Démarrage du service...');
                  await _pachaBiometric.startService();
                  _updateStatus('Service de verrouillage activé');
                  _showSnackBar('Service démarré avec succès');
                } catch (e) {
                  _updateStatus('Erreur lors du démarrage du service');
                  _showSnackBar('Erreur : $e');
                }
              },
              child: const Text('Démarrer le service'),
            ),
            const SizedBox(height: 10),
            ElevatedButton(
              onPressed: () async {
                try {
                  _updateStatus('Capture photo en cours...');
                  final photoPath = await _pachaBiometric.capturePhoto();
                  _updateStatus(photoPath != null ? 'Photo capturée: $photoPath' : 'Aucune photo capturée');
                  _showSnackBar(photoPath != null ? 'Photo sauvegardée' : 'Échec de la capture');
                } catch (e) {
                  _updateStatus('Erreur lors de la capture');
                  _showSnackBar('Erreur : $e');
                }
              },
              child: const Text('Capturer une photo'),
            ),
            const SizedBox(height: 30),
            Text(
              'Testez toutes les fonctionnalités de votre plugin biométrique.',
              style: TextStyle(color: Colors.grey[600]),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}