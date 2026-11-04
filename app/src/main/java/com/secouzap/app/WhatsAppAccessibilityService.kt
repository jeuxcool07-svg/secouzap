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
        handler.post { openChat() }
    }

    private fun openChat() {
        val prefs = getSharedPreferences("secouzap", MODE_PRIVATE)
        val phone = prefs.getString("phone", "") ?: ""
        val message = prefs.getString("message", "") ?: ""
        if (phone.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Numéro ou message vide", Toast.LENGTH_LONG).show()
            return
        }
        val link = "whatsapp://send?phone=" + phone + "&text=" + Uri.encode(message)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Impossible d'ouvrir WhatsApp", Toast.LENGTH_LONG).show()
            return
        }
        attempts = 0
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ tryClick() }, 2000)
    }

    private fun tryClick() {
        attempts++
        val done = clickSend(rootInActiveWindow)
        if (!done && attempts < 24) {
            handler.postDelayed({ tryClick() }, 500)
        }
    }
}
// FIN
