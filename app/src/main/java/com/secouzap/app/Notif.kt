package com.secouzap.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.pm.ServiceInfo
import android.os.Build

fun Service.startAsForeground() {
    val id = "secouzap_channel"
    val nm = getSystemService(NotificationManager::class.java)
    nm.createNotificationChannel(
        NotificationChannel(id, "Surveillance", NotificationManager.IMPORTANCE_LOW)
    )
    val n = Notification.Builder(this, id)
        .setContentTitle("SecouZap actif")
        .setContentText("Secoue le téléphone pour envoyer ton message")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setOngoing(true)
        .build()
    if (Build.VERSION.SDK_INT >= 34) {
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    } else {
        startForeground(1, n)
    }
}
// FIN
