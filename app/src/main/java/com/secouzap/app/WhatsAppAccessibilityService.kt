package com.secouzap.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class WhatsAppAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: WhatsAppAccessibilityService? = null
    }

    private val handler = Handler(Looper.getMainLooper())
    private val queue = ArrayList<String>()
    private var message = ""
    private var busy = false
    private var attempts = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun sendNow() {
        handler.post { start() }
    }

    private fun start() {
        if (busy) return
        val prefs = getSharedPreferences("secouzap", MODE_PRIVATE)
        message = prefs.getString("message", "") ?: ""
        val numbers = parseNumbers(prefs.getString("phone", "") ?: "")
        if (numbers.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Numéro ou message vide", Toast.LENGTH_LONG).show()
            return
        }
        queue.clear()
        queue.addAll(numbers)
        busy = true
        next()
    }

    private fun next() {
        if (queue.isEmpty()) {
            busy = false
            return
        }
        val phone = queue.removeAt(0)
        val link = "whatsapp://send?phone=" + phone + "&text=" + Uri.encode(message)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Impossible d'ouvrir WhatsApp", Toast.LENGTH_LONG).show()
            queue.clear()
            busy = false
            return
        }
        attempts = 0
        handler.postDelayed({ tryClick() }, 2000)
    }

    private fun tryClick() {
        attempts++
        if (clickSend(rootInActiveWindow)) {
            handler.postDelayed({ next() }, 4000)
        } else if (attempts < 24) {
            handler.postDelayed({ tryClick() }, 500)
        } else {
            handler.postDelayed({ next() }, 1000)
        }
    }
}
// FIN
