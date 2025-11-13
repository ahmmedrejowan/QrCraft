package com.rejown.qrcraft.utils

import com.rejown.qrcraft.domain.models.ValidationRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Comprehensive unit tests for InputValidator
 * Tests validation methods that don't require Android dependencies
 *
 * Note: Email and URL validation tests are skipped as they rely on
 * Android's Patterns class which requires Robolectric or instrumented tests.
 */
class InputValidatorTest {

    // ============ Email Validation Tests (Skipped - requires Android Patterns) ============

    @Test
    fun `validateEmail returns null for empty string`() {
        // Empty validation doesn't require Android Patterns
        assertNull(InputValidator.validateEmail(""))
    }

    // Note: Length validation test requires Android Patterns for email validation

    // ============ Phone Validation Tests ============

    @Test
    fun `validatePhone returns null for valid phone numbers`() {
        val validPhones = listOf(
            "+1234567890",
            "1234567890",
            "+44 20 7946 0958",
            "(123) 456-7890",
            "123.456.7890"
        )

        validPhones.forEach { phone ->
            assertNull("Expected valid: $phone", InputValidator.validatePhone(phone))
        }
    }

    @Test
    fun `validatePhone returns error for invalid phone format`() {
        val invalidPhones = listOf(
            "12345", // Too short
            "123456789012345678", // Too long
            "abcdefghij",
            "++1234567890"
        )

        invalidPhones.forEach { phone ->
            assertNotNull("Expected invalid: $phone", InputValidator.validatePhone(phone))
        }
    }

    @Test
    fun `validatePhone returns null for empty string`() {
        assertNull(InputValidator.validatePhone(""))
    }

    // ============ URL Validation Tests (Skipped - requires Android Patterns) ============

    @Test
    fun `validateUrl returns null for empty string`() {
        assertNull(InputValidator.validateUrl(""))
    }

    // Note: Length validation test requires Android Patterns for URL validation

    // ============ WiFi Validation Tests ============

    @Test
    fun `validateWifiSSID returns null for valid SSID`() {
        val validSSIDs = listOf(
            "MyNetwork",
            "Home WiFi",
            "Network-123"
        )

        validSSIDs.forEach { ssid ->
            assertNull("Expected valid SSID: $ssid", InputValidator.validateWifiSSID(ssid))
        }
    }

    @Test
    fun `validateWifiSSID returns error for empty SSID`() {
        assertNotNull(InputValidator.validateWifiSSID(""))
    }

    @Test
    fun `validateWifiSSID returns error for SSID longer than 32 characters`() {
        val longSSID = "a".repeat(33)
        assertNotNull(InputValidator.validateWifiSSID(longSSID))
    }

    @Test
    fun `validateWifiPassword returns null for valid password`() {
        val password = "MySecurePassword123"
        assertNull(InputValidator.validateWifiPassword(password, "WPA2"))
    }

    @Test
    fun `validateWifiPassword returns null for open network`() {
        assertNull(InputValidator.validateWifiPassword("", "nopass"))
        assertNull(InputValidator.validateWifiPassword("", "NOPASS"))
    }

    @Test
    fun `validateWifiPassword returns error for short password`() {
        val shortPassword = "1234567" // 7 chars
        assertNotNull(InputValidator.validateWifiPassword(shortPassword, "WPA2"))
    }

    @Test
    fun `validateWifiPassword returns error for password longer than 63 characters`() {
        val longPassword = "a".repeat(64)
        assertNotNull(InputValidator.validateWifiPassword(longPassword, "WPA2"))
    }

    // ============ Credit Card Validation Tests ============

    @Test
    fun `validateCreditCard returns null for valid card numbers`() {
        val validCards = listOf(
            "4532015112830366", // Visa
            "5425233430109903", // Mastercard
            "374245455400126"   // Amex
        )

        validCards.forEach { card ->
            assertNull("Expected valid card: $card", InputValidator.validateCreditCard(card))
        }
    }

    @Test
    fun `validateCreditCard accepts card numbers with spaces and dashes`() {
        val formattedCard = "4532-0151-1283-0366"
        assertNull(InputValidator.validateCreditCard(formattedCard))
    }

    @Test
    fun `validateCreditCard returns error for invalid Luhn checksum`() {
        val invalidCard = "4532015112830367" // Last digit wrong
        assertNotNull(InputValidator.validateCreditCard(invalidCard))
    }

    @Test
    fun `validateCreditCard returns error for invalid format`() {
        val invalidCards = listOf(
            "123", // Too short
            "12345678901234567890", // Too long
            "abcd1234abcd1234"
        )

        invalidCards.forEach { card ->
            assertNotNull("Expected invalid card: $card", InputValidator.validateCreditCard(card))
        }
    }

    @Test
    fun `validateCreditCard returns null for empty string`() {
        assertNull(InputValidator.validateCreditCard(""))
    }

    // ============ Crypto Address Validation Tests ============

    @Test
    fun `validateCryptoAddress returns null for valid Bitcoin address`() {
        val validBitcoin = listOf(
            "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", // Legacy
            "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" // SegWit
        )

        validBitcoin.forEach { address ->
            assertNull("Expected valid Bitcoin: $address", InputValidator.validateCryptoAddress(address, "bitcoin"))
        }
    }

    @Test
    fun `validateCryptoAddress returns null for valid Ethereum address`() {
        val validEth = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEbb" // Fixed to 40 hex chars
        assertNull(InputValidator.validateCryptoAddress(validEth, "ethereum"))
    }

    @Test
    fun `validateCryptoAddress returns error for invalid Bitcoin address`() {
        val invalidBitcoin = "invalid_bitcoin_address"
        assertNotNull(InputValidator.validateCryptoAddress(invalidBitcoin, "bitcoin"))
    }

    @Test
    fun `validateCryptoAddress returns error for invalid Ethereum address`() {
        val invalidEth = "0xinvalid"
        assertNotNull(InputValidator.validateCryptoAddress(invalidEth, "ethereum"))
    }

    @Test
    fun `validateCryptoAddress returns null for unknown crypto type`() {
        assertNull(InputValidator.validateCryptoAddress("anyaddress", "unknown"))
    }

    // ============ Date Validation Tests ============

    @Test
    fun `validateDate returns null for valid dates`() {
        val validDates = listOf(
            "2024-01-15",
            "2024-12-31",
            "2024-02-29" // Leap year
        )

        validDates.forEach { date ->
            assertNull("Expected valid date: $date", InputValidator.validateDate(date))
        }
    }

    @Test
    fun `validateDate returns error for invalid format`() {
        val invalidDates = listOf(
            "2024/01/15",
            "15-01-2024",
            "2024-1-15"
        )

        invalidDates.forEach { date ->
            assertNotNull("Expected invalid format: $date", InputValidator.validateDate(date))
        }
    }

    @Test
    fun `validateDate returns error for invalid dates`() {
        val invalidDates = mapOf(
            "2024-13-01" to "Invalid month",
            "2024-00-15" to "Invalid month",
            "2024-01-32" to "Invalid day",
            "2024-02-30" to "Invalid day for February",
            "2023-02-29" to "Not a leap year",
            "2024-04-31" to "Invalid day for April"
        )

        invalidDates.forEach { (date, reason) ->
            assertNotNull("Expected invalid ($reason): $date", InputValidator.validateDate(date))
        }
    }

    @Test
    fun `validateDate returns error for year out of range`() {
        assertNotNull(InputValidator.validateDate("1899-01-01"))
        assertNotNull(InputValidator.validateDate("2101-01-01"))
    }

    // ============ Coordinate Validation Tests ============

    @Test
    fun `validateCoordinates returns null for valid coordinates`() {
        assertNull(InputValidator.validateCoordinates("40.7128", "-74.0060"))
        assertNull(InputValidator.validateCoordinates("0", "0"))
        assertNull(InputValidator.validateCoordinates("90", "180"))
        assertNull(InputValidator.validateCoordinates("-90", "-180"))
    }

    @Test
    fun `validateCoordinates returns error for latitude out of range`() {
        assertNotNull(InputValidator.validateCoordinates("91", "0"))
        assertNotNull(InputValidator.validateCoordinates("-91", "0"))
    }

    @Test
    fun `validateCoordinates returns error for longitude out of range`() {
        assertNotNull(InputValidator.validateCoordinates("0", "181"))
        assertNotNull(InputValidator.validateCoordinates("0", "-181"))
    }

    @Test
    fun `validateCoordinates returns error for invalid format`() {
        assertNotNull(InputValidator.validateCoordinates("invalid", "0"))
        assertNotNull(InputValidator.validateCoordinates("0", "invalid"))
    }

    // ============ Sanitize Input Tests ============

    @Test
    fun `sanitizeInput removes control characters`() {
        val input = "Hello\u0000\u001FWorld"
        val sanitized = InputValidator.sanitizeInput(input)
        assertEquals("HelloWorld", sanitized)
    }

    @Test
    fun `sanitizeInput removes script tags`() {
        val input = "Hello<script>alert('xss')</script>World"
        val sanitized = InputValidator.sanitizeInput(input)
        assertEquals("HelloWorld", sanitized)
    }

    @Test
    fun `sanitizeInput trims whitespace`() {
        val input = "  Hello World  "
        val sanitized = InputValidator.sanitizeInput(input)
        assertEquals("Hello World", sanitized)
    }

    // ============ ValidationRule Tests ============

    @Test
    fun `validate Required rule returns error for blank value`() {
        val rule = ValidationRule.Required("Field is required")
        assertNotNull(InputValidator.validate("", rule))
        assertNotNull(InputValidator.validate("   ", rule))
    }

    @Test
    fun `validate Required rule returns null for non-blank value`() {
        val rule = ValidationRule.Required("Field is required")
        assertNull(InputValidator.validate("value", rule))
    }

    @Test
    fun `validate MinLength rule returns error for short value`() {
        val rule = ValidationRule.MinLength(5)
        assertNotNull(InputValidator.validate("1234", rule))
    }

    @Test
    fun `validate MinLength rule returns null for valid length`() {
        val rule = ValidationRule.MinLength(5)
        assertNull(InputValidator.validate("12345", rule))
        assertNull(InputValidator.validate("123456", rule))
    }

    @Test
    fun `validate MaxLength rule returns error for long value`() {
        val rule = ValidationRule.MaxLength(5)
        assertNotNull(InputValidator.validate("123456", rule))
    }

    @Test
    fun `validate MaxLength rule returns null for valid length`() {
        val rule = ValidationRule.MaxLength(5)
        assertNull(InputValidator.validate("12345", rule))
        assertNull(InputValidator.validate("1234", rule))
    }

    @Test
    fun `validate Pattern rule returns error for non-matching value`() {
        val rule = ValidationRule.Pattern(Regex("^[0-9]+$"), "Only numbers allowed")
        assertNotNull(InputValidator.validate("abc123", rule))
    }

    @Test
    fun `validate Pattern rule returns null for matching value`() {
        val rule = ValidationRule.Pattern(Regex("^[0-9]+$"), "Only numbers allowed")
        assertNull(InputValidator.validate("123456", rule))
    }

    @Test
    fun `validateAll returns first error when multiple rules fail`() {
        val rules = listOf(
            ValidationRule.Required("Required"),
            ValidationRule.MinLength(5),
            ValidationRule.Email
        )
        val error = InputValidator.validateAll("", rules)
        assertEquals("Required", error)
    }

    @Test
    fun `validateAll returns null when all rules pass`() {
        val rules = listOf(
            ValidationRule.Required("Required"),
            ValidationRule.MinLength(3)
        )
        val error = InputValidator.validateAll("test", rules)
        assertNull(error)
    }
}
