package com.secouzap.app

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.PowerManager
import kotlin.math.sqrt

class ShakeService : Service(), SensorEventListener {

    companion object {
        @Volatile
        var running = false
    }

    private val threshold = 25f
    private val needed = 3
    private val windowMs = 1500L
    private val cooldownMs = 8000L
    private val hits = ArrayList<Long>()
    private var lastSent = 0L
    private var lock: PowerManager.WakeLock? = null
    private var sm: SensorManager? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val l = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "secouzap:shake")
        l.acquire()
        lock = l
        val m = getSystemService(SENSOR_SERVICE) as SensorManager
        sm = m
        val acc = m.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        m.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME)
        running = true
    }

    override fun onSensorChanged(e: SensorEvent) {
        val x = e.values[0]
        val y = e.values[1]
        val z = e.values[2]
        val g = sqrt(x * x + y * y + z * z)
        val now = System.currentTimeMillis()
        if (g > threshold) {
            if (hits.isEmpty() || now - hits[hits.size - 1] > 200) hits.add(now)
        }
        hits.removeAll { now - it > windowMs }
        if (hits.size >= needed && now - lastSent > cooldownMs) {
            lastSent = now
            hits.clear()
            WhatsAppAccessibilityService.instance?.sendNow()
        }
    }

    override fun onAccuracyChanged(s: Sensor?, a: Int) {}

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int = START_STICKY

    override fun onDestroy() {
        sm?.unregisterListener(this)
        val l = lock
        if (l != null && l.isHeld) l.release()
        running = false
        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? = null
}
// FIN
