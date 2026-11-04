package com.secouzap.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var phone: EditText
    private lateinit var message: EditText
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        val prefs = getSharedPreferences("secouzap", MODE_PRIVATE)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(50, 80, 50, 80)

        status = TextView(this)
        status.textSize = 16f
        root.addView(status)

        phone = EditText(this)
        phone.hint = "Numéro avec indicatif, sans + (ex : 22370000000)"
        phone.inputType = InputType.TYPE_CLASS_PHONE
        phone.setText(prefs.getString("phone", ""))
        root.addView(phone)

        message = EditText(this)
        message.hint = "Ton message"
        message.setText(prefs.getString("message", ""))
        root.addView(message)

        root.btn("Enregistrer") { save(); toast("Enregistré") }
        root.btn("1. Activer l'accessibilité") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        root.btn("2. Batterie non restreinte") { askBattery() }
        root.btn("3. Démarrer la surveillance") { save(); setWatching(true); later() }
        root.btn("Arrêter la surveillance") { setWatching(false); later() }
        root.btn("Tester l'envoi") { save(); testSend() }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun later() {
        status.postDelayed({ refresh() }, 600)
    }

    private fun refresh() {
        val a = if (WhatsAppAccessibilityService.instance != null) "OUI" else "NON"
        val s = if (ShakeService.running) "OUI" else "NON"
        status.text = "Accessibilité : $a    Surveillance : $s"
    }

    private fun save() {
        val digits = phone.text.toString().filter { it.isDigit() }
        saveSettings(digits, message.text.toString())
        phone.setText(digits)
    }
}
// FIN
