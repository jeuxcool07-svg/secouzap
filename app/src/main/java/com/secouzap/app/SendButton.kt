package com.secouzap.app

import android.view.accessibility.AccessibilityNodeInfo

fun clickSend(root: AccessibilityNodeInfo?): Boolean {
    if (root == null) return false
    val pkg = root.packageName?.toString() ?: return false
    if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return false
    val button = findSend(root) ?: return false
    var n: AccessibilityNodeInfo? = button
    while (n != null) {
        if (n.isClickable && n.isEnabled) {
            return n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        n = n.parent
    }
    return false
}

private fun findSend(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
    for (id in listOf("com.whatsapp:id/send", "com.whatsapp.w4b:id/send")) {
        val list = node.findAccessibilityNodeInfosByViewId(id)
        if (!list.isNullOrEmpty()) return list[0]
    }
    return findByDescription(node)
}

private fun findByDescription(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
    val d = node.contentDescription?.toString()?.lowercase() ?: ""
    if (d == "envoyer" || d == "send") return node
    for (i in 0 until node.childCount) {
        val c = node.getChild(i) ?: continue
        val f = findByDescription(c)
        if (f != null) return f
    }
    return null
}
// FIN
