package com.secouzap.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class WhatsAppAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: WhatsAppAccessibilityService? = null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var waiting = false
    private var attempts = 0

    private val retry = object : Runnable {
        override fun run() {
            if (!waiting) return
            attempts++
            if (trySend()) {
                waiting = false
                return
            }
            if (attempts < 24) {
                handler.postDelayed(this, 500)
            } else {
                waiting = false
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun sendNow() {
        handler.post {
            val prefs = getSharedPreferences("secouzap", MODE_PRIVATE)
            val phone = prefs.getString("phone", "") ?: ""
            val message = prefs.getString("message", "") ?: ""

            if (phone.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Numéro ou message vide", Toast.LENGTH_LONG).show()
                return@post
            }

            val uri = Uri.parse("https://wa.me/$phone?text=" + Uri.encode(message))
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            handler.removeCallbacks(retry)
            waiting = true
            attempts = 0

            try {
                intent.setPackage("com.whatsapp")
                startActivity(intent)
            } catch (e: Exception) {
                try {
                    intent.setPackage(null)
                    startActivity(intent)
                } catch (e2: Exception) {
                    waiting = false
                    Toast.makeText(this, "Impossible d'ouvrir WhatsApp", Toast.LENGTH_LONG).show()
                    return@post
                }
            }

            handler.postDelayed(retry, 2000)
        }
    }

    private fun trySend(): Boolean {
        val root = rootInActiveWindow ?: return false
        val pkg = root.packageName?.toString() ?: return false
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return false
        val button = findSendButton(root) ?: return false
        return clickNode(button)
    }

    private fun findSendBu
