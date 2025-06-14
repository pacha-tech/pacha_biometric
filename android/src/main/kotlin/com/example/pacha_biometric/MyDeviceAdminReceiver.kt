package com.example.pacha_biometric

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context?, intent: Intent?) {
        super.onEnabled(context, intent) // Appel explicite au parent
        Toast.makeText(context, "Administrateur activé", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context?, intent: Intent?) {
        super.onDisabled(context, intent) // Appel explicite au parent
        Toast.makeText(context, "Administrateur désactivé", Toast.LENGTH_SHORT).show()
    }
}