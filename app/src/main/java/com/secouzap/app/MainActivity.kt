package com.secouzap.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var phoneInput: EditText
    private lateinit var messageInput: EditText
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        val prefs = getSharedPreferences("secouzap", MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }

        root.addView(TextView(this).apply {
            text = "SecouZap"
            textSize = 28f
        })

        statusText = TextView(this).apply {
            textSize = 15f
            setPadding(0, dp(8), 0, dp(16))
        }
        root.addView(statusText)

        phoneInput = EditText(this).apply {
            hint = "Numéro de ton ami avec indicatif, sans + (ex : 22370000000)"
            inputType = InputType.TYPE_CLASS_PHONE
            setText(prefs.getString("phone", ""))
        }
        root.addView(phoneInput)

        messageInput = EditText(this).apply {
            hint = "Ton message"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
            setText(prefs.getString("message", ""))
        }
        root.addView(messageInput)

        root.addButton("Enregistrer") {
            save()
            toast("Enregistré ✅")
        }

        root.addButton("1. Activer l'accessibilité") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        root.addButton("2. Batterie non restreinte") {
            val pm = getSystemService(PowerManager::class.java)
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                toast("C'est déjà fait ✅")
            } else {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }

        root.addButton("3. Démarrer la surveillance") {
            save()
            startForegroundService(Intent(this@MainActivity, ShakeService::class.java))
            toast("Surveillance démarrée")
            refreshStatus()
        }

        root.addButton("Arrêter la surveillance") {
            stopService(Intent(this@MainActivity, ShakeService::class.java))
            toast("Surveillance arrêtée")
            refreshStatus()
        }

        root.addButton("Tester l'envoi") {
            save()
            val svc = WhatsAppAccessibilityService.instance
            if (svc == null) {
                toast("Active d'abord l'accessibilité (bouton 1)")
            } else {
                svc.sendNow()
            }
        }

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val access = if (WhatsAppAccessibilityService.instance != null) "✅
