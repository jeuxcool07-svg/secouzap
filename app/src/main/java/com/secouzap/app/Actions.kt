package com.secouzap.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

fun Activity.toast(t: String) {
    Toast.makeText(this, t, Toast.LENGTH_SHORT).show()
}

fun LinearLayout.btn(label: String, action: () -> Unit) {
    val b = Button(context)
    b.text = label
    b.setOnClickListener { action() }
    addView(b)
}

fun Activity.saveSettings(phone: String, message: String) {
    val e = getSharedPreferences("secouzap", Context.MODE_PRIVATE).edit()
    e.putString("phone", phone)
    e.putString("message", message)
    e.apply()
}

fun Activity.askBattery() {
    val pm = getSystemService(PowerManager::class.java)
    if (pm.isIgnoringBatteryOptimizations(packageName)) {
        toast("Déjà fait")
    } else {
        val uri = Uri.parse("package:" + packageName)
        startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, uri))
    }
}

fun Activity.testSend() {
    val svc = WhatsAppAccessibilityService.instance
    if (svc == null) toast("Active d'abord l'accessibilité (bouton 1)") else svc.sendNow()
}

fun Activity.setWatching(on: Boolean) {
    val i = Intent(this, ShakeService::class.java)
    if (on) startForegroundService(i) else stopService(i)
    toast(if (on) "Surveillance démarrée" else "Surveillance arrêtée")
}
// FIN
