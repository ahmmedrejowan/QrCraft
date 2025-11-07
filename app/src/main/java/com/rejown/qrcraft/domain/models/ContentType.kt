package com.rejown.qrcraft.domain.models

enum class ContentType(val displayName: String) {
    URL("Website"),
    EMAIL("Email"),
    PHONE("Phone"),
    SMS("SMS"),
    WIFI("WiFi"),
    CONTACT("Contact"),
    CALENDAR("Calendar Event"),
    GEO("Location"),
    TEXT("Plain Text"),
    CRYPTO("Cryptocurrency"),
    PRODUCT("Product");

    companion object {
        fun detectFromContent(content: String): ContentType {
            return when {
                content.startsWith("http://", ignoreCase = true) ||
                        content.startsWith("https://", ignoreCase = true) -> URL

                content.startsWith("mailto:", ignoreCase = true) ||
                        android.util.Patterns.EMAIL_ADDRESS.matcher(content).matches() -> EMAIL

                content.startsWith("tel:", ignoreCase = true) ||
                        content.matches(Regex("^[+]?[0-9]{10,}$")) -> PHONE

                content.startsWith("smsto:", ignoreCase = true) ||
                        content.startsWith("sms:", ignoreCase = true) -> SMS

                content.startsWith("WIFI:", ignoreCase = true) -> WIFI

                content.startsWith("BEGIN:VCARD", ignoreCase = true) -> CONTACT

                content.startsWith("BEGIN:VEVENT", ignoreCase = true) -> CALENDAR

                content.startsWith("geo:", ignoreCase = true) ||
                        content.matches(Regex("^-?\\d+\\.\\d+,-?\\d+\\.\\d+$")) -> GEO

                content.startsWith("bitcoin:", ignoreCase = true) ||
                        content.startsWith("ethereum:", ignoreCase = true) ||
                        content.matches(Regex("^(bc1|0x)[a-zA-Z0-9]{25,}$")) -> CRYPTO

                content.matches(Regex("^[0-9]{8,14}$")) -> PRODUCT

                else -> TEXT
            }
        }
    }
}
