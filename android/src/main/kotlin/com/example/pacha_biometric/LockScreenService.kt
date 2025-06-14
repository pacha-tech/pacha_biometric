package com.example.pacha_biometric

import android.app.Service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import android.os.Build
import android.app.Notification

class LockScreenService : Service() {
    private val channelId = "lockScreenServiceChannel"
    private val notificationId = 1

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                Log.d("LockScreenService", "Screen unlocked, launching FlutterActivity")
                val lockIntent = Intent(context, FlutterActivity::class.java)
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(lockIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(notificationId, buildNotification())
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(screenReceiver, filter)
        Log.d("LockScreenService", "Service created and running in foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LockScreenService", "Service started with intent: $intent")
        return START_STICKY // Garde le service actif même après un crash ou une interruption
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
            Log.d("LockScreenService", "Service destroyed, receiver unregistered")
        } catch (e: IllegalArgumentException) {
            Log.e("LockScreenService", "Erreur lors du désenregistrement du receiver : ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lock Screen Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Anti-Theft Protection")
            .setContentText("Surveillance active")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true) // Empêche la suppression manuelle
            .build()
    }
}