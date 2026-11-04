package com.secouzap.app

fun parseNumbers(raw: String): List<String> {
    val list = ArrayList<String>()
    for (part in raw.split(",", ";", "\n")) {
        val digits = part.filter { it.isDigit() }
        if (digits.length >= 8) list.add(digits)
    }
    return list.take(20)
}
// FIN
