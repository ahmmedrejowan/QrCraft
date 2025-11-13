package com.rejown.qrcraft.utils

import android.util.Patterns
import com.rejown.qrcraft.domain.models.ValidationRule

/**
 * Comprehensive input validation utility for form fields
 * Provides security-focused validation for URLs, emails, phone numbers, and general input
 */
object InputValidator {

    /**
     * Validates input against a validation rule
     * @param value The input value to validate
     * @param rule The validation rule to apply
     * @return Error message if validation fails, null if valid
     */
    fun validate(value: String, rule: ValidationRule): String? {
        return when (rule) {
            is ValidationRule.Required -> {
                if (value.isBlank()) rule.message else null
            }
            is ValidationRule.Email -> {
                validateEmail(value)
            }
            is ValidationRule.Phone -> {
                validatePhone(value)
            }
            is ValidationRule.Url -> {
                validateUrl(value)
            }
            is ValidationRule.MinLength -> {
                if (value.length < rule.min) {
                    "Must be at least ${rule.min} characters"
                } else null
            }
            is ValidationRule.MaxLength -> {
                if (value.length > rule.max) {
                    "Must not exceed ${rule.max} characters"
                } else null
            }
            is ValidationRule.Pattern -> {
                if (!rule.regex.matches(value)) {
                    rule.message
                } else null
            }
        }
    }

    /**
     * Validates multiple rules against a value
     * @return First error message encountered, or null if all validations pass
     */
    fun validateAll(value: String, rules: List<ValidationRule>): String? {
        for (rule in rules) {
            val error = validate(value, rule)
            if (error != null) return error
        }
        return null
    }

    /**
     * Validates email format
     * Uses Android's Patterns.EMAIL_ADDRESS for RFC 5322 compliance
     */
    fun validateEmail(email: String): String? {
        if (email.isBlank()) return null // Empty is valid (use Required rule if needed)

        return when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                "Invalid email format"
            }
            email.length > 254 -> {
                "Email address too long"
            }
            else -> null
        }
    }

    /**
     * Validates phone number format
     * Accepts international formats with optional country code
     */
    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return null

        // Remove common separators for validation
        val cleaned = phone.replace(Regex("[\\s\\-().]"), "")

        return when {
            // Allow + for international, then digits only
            !cleaned.matches(Regex("^\\+?[0-9]{7,15}$")) -> {
                "Invalid phone number format"
            }
            cleaned.length < 7 -> {
                "Phone number too short"
            }
            cleaned.length > 15 -> {
                "Phone number too long"
            }
            else -> null
        }
    }

    /**
     * Validates URL format with security checks
     * Detects potentially malicious patterns
     */
    fun validateUrl(url: String): String? {
        if (url.isBlank()) return null

        return when {
            // Basic pattern validation
            !Patterns.WEB_URL.matcher(url).matches() -> {
                "Invalid URL format"
            }
            // Length check
            url.length > 2048 -> {
                "URL too long"
            }
            // Security: Check for suspicious patterns
            containsSuspiciousPatterns(url) -> {
                "URL contains suspicious patterns"
            }
            // Security: Check for IP address URLs (potentially suspicious)
            containsRawIpAddress(url) && !isLocalhost(url) -> {
                "Direct IP address URLs are not recommended"
            }
            else -> null
        }
    }

    /**
     * Detects potentially malicious URL patterns
     */
    private fun containsSuspiciousPatterns(url: String): Boolean {
        val suspiciousPatterns = listOf(
            // JavaScript execution
            Regex("javascript:", RegexOption.IGNORE_CASE),
            Regex("data:", RegexOption.IGNORE_CASE),
            Regex("vbscript:", RegexOption.IGNORE_CASE),

            // HTML/Script injection attempts
            Regex("<script", RegexOption.IGNORE_CASE),
            Regex("onerror\\s*=", RegexOption.IGNORE_CASE),
            Regex("onload\\s*=", RegexOption.IGNORE_CASE),

            // Unicode homograph attacks (common substitutions)
            Regex("[а-яА-Я]"), // Cyrillic characters

            // Double encoding attempts
            Regex("%[0-9a-fA-F]{2}%[0-9a-fA-F]{2}"),

            // Path traversal
            Regex("\\.\\./"),

            // Null byte injection
            Regex("%00"),
        )

        return suspiciousPatterns.any { it.containsMatchIn(url) }
    }

    /**
     * Checks if URL contains a raw IP address
     */
    private fun containsRawIpAddress(url: String): Boolean {
        val ipPattern = Regex("""https?://(\d{1,3}\.){3}\d{1,3}""")
        return ipPattern.containsMatchIn(url)
    }

    /**
     * Checks if URL is localhost (allowed IP address)
     */
    private fun isLocalhost(url: String): Boolean {
        return url.contains("127.0.0.1") ||
               url.contains("localhost") ||
               url.contains("0.0.0.0")
    }

    /**
     * Sanitizes input by removing potentially dangerous characters
     * Use for general text input that will be displayed or stored
     */
    fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("[\u0000-\u001F\u007F-\u009F]"), "") // Remove control characters
            .replace(Regex("<script.*?>.*?</script>", RegexOption.IGNORE_CASE), "") // Remove script tags
            .trim()
    }

    /**
     * Validates WiFi SSID
     */
    fun validateWifiSSID(ssid: String): String? {
        return when {
            ssid.isBlank() -> "SSID cannot be empty"
            ssid.length > 32 -> "SSID cannot exceed 32 characters"
            ssid.toByteArray().size > 32 -> "SSID contains invalid characters"
            else -> null
        }
    }

    /**
     * Validates WiFi password
     */
    fun validateWifiPassword(password: String, securityType: String): String? {
        if (securityType.equals("nopass", ignoreCase = true)) {
            return null // No password needed for open networks
        }

        return when {
            password.isBlank() -> "Password cannot be empty for secured network"
            password.length < 8 -> "Password must be at least 8 characters"
            password.length > 63 -> "Password cannot exceed 63 characters"
            else -> null
        }
    }

    /**
     * Validates credit card number using Luhn algorithm
     */
    fun validateCreditCard(cardNumber: String): String? {
        val cleaned = cardNumber.replace(Regex("[\\s\\-]"), "")

        return when {
            cleaned.isBlank() -> null
            !cleaned.matches(Regex("^[0-9]{13,19}$")) -> {
                "Invalid card number format"
            }
            !luhnCheck(cleaned) -> {
                "Invalid card number (failed checksum)"
            }
            else -> null
        }
    }

    /**
     * Luhn algorithm for credit card validation
     */
    private fun luhnCheck(cardNumber: String): Boolean {
        var sum = 0
        var alternate = false

        for (i in cardNumber.length - 1 downTo 0) {
            var digit = cardNumber[i].toString().toInt()

            if (alternate) {
                digit *= 2
                if (digit > 9) digit -= 9
            }

            sum += digit
            alternate = !alternate
        }

        return sum % 10 == 0
    }

    /**
     * Validates cryptocurrency address (basic format check)
     */
    fun validateCryptoAddress(address: String, type: String): String? {
        if (address.isBlank()) return null

        return when (type.lowercase()) {
            "bitcoin", "btc" -> {
                when {
                    !address.matches(Regex("^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$|^bc1[a-z0-9]{39,59}$")) -> {
                        "Invalid Bitcoin address format"
                    }
                    else -> null
                }
            }
            "ethereum", "eth" -> {
                when {
                    !address.matches(Regex("^0x[a-fA-F0-9]{40}$")) -> {
                        "Invalid Ethereum address format"
                    }
                    else -> null
                }
            }
            else -> null // Unknown type, skip validation
        }
    }

    /**
     * Validates date format (YYYY-MM-DD)
     */
    fun validateDate(date: String): String? {
        if (date.isBlank()) return null

        val datePattern = Regex("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$")

        return when {
            !datePattern.matches(date) -> {
                "Invalid date format (use YYYY-MM-DD)"
            }
            else -> {
                // Additional validation for valid dates
                val parts = date.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()

                when {
                    year < 1900 || year > 2100 -> "Invalid year"
                    month < 1 || month > 12 -> "Invalid month"
                    day < 1 || day > 31 -> "Invalid day"
                    month in listOf(4, 6, 9, 11) && day > 30 -> "Invalid day for month"
                    month == 2 && day > 29 -> "Invalid day for February"
                    month == 2 && day == 29 && !isLeapYear(year) -> "Not a leap year"
                    else -> null
                }
            }
        }
    }

    /**
     * Checks if a year is a leap year
     */
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    /**
     * Validates geographic coordinates
     */
    fun validateCoordinates(lat: String, lon: String): String? {
        val latDouble = lat.toDoubleOrNull()
        val lonDouble = lon.toDoubleOrNull()

        return when {
            latDouble == null -> "Invalid latitude format"
            lonDouble == null -> "Invalid longitude format"
            latDouble < -90 || latDouble > 90 -> "Latitude must be between -90 and 90"
            lonDouble < -180 || lonDouble > 180 -> "Longitude must be between -180 and 180"
            else -> null
        }
    }
}
