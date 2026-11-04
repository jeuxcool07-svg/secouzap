package com.secouzap.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import kotlin.math.sqrt

class ShakeService : Service(), SensorEventListener {

    companion object {
        @Volatile
        var running = false
    }

    // ====== RÉGLAGES ======
    private val threshold = 25f      // force en m/s² (au repos ≈ 9,8)
    private val shakesNeeded = 3     // nombre de secousses requises
    private val windowMs = 1500L     // ... dans cet intervalle
    private val cooldownMs = 8000L   // pause après un envoi
    // ======================

    private lateinit var sensorManager: SensorManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val hits = ArrayList<Long>()
    private var lastSent = 0L

    override fun onCreate() {
        super.onCreate()

        val channelId = "secouzap_channel"
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Surveillance", NotificationManager.IMPORTANCE_LOW)
        )
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("SecouZap actif")
            .setContentText("Secoue le téléphone pour envoyer ton message")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }

        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.P
