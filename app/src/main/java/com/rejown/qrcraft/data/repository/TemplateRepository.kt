package com.rejown.qrcraft.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import com.rejown.qrcraft.domain.models.*

/**
 * Repository providing all available QR code templates
 */
object TemplateRepository {

    fun getAllTemplates(): List<Template> = listOf(
        // ============ GENERAL & PERSONAL ============
        createPlainTextTemplate(),
        createNumberTemplate(),
        createCustomDataTemplate(),

        // ============ COMMUNICATION ============
        createPhoneTemplate(),
        createEmailTemplate(),
        createSMSTemplate(),
        createContactTemplate(),
        createWhatsAppTemplate(),

        // ============ SOCIAL & WEB ============
        createURLTemplate(),
        createSocialProfileTemplate(),
        createWiFiTemplate(),
        createYouTubeTemplate(),

        // ============ LOCATION & EVENTS ============
        createGeoLocationTemplate(),
        createCalendarEventTemplate(),
        createGoogleMapsTemplate(),

        // ============ BUSINESS & PROFESSIONAL ============
        createBusinessCardTemplate(),
        createDigitalResumeTemplate(),
        createCryptoTemplate(),
        createPaymentLinkTemplate(),

        // ============ PRODUCT & INVENTORY ============
        createProductBarcodeTemplate(),
        createProductQRTemplate(),
        createInventoryTagTemplate(),

        // ============ DOCUMENTS & FILES ============
        createPDFLinkTemplate(),
        createAppDownloadTemplate(),
        createFileShareTemplate(),

        // ============ TICKETS & PASSES ============
        createEventTicketTemplate(),
        createBoardingPassTemplate(),
        createCouponTemplate()
    )

    fun getTemplateById(id: String): Template? {
        return getAllTemplates().find { it.id == id }
    }

    fun getTemplatesByCategory(category: TemplateCategory): List<Template> {
        return getAllTemplates().filter { it.category == category }
    }

    // ============ TEMPLATE CREATORS ============

    private fun createPlainTextTemplate() = Template(
        id = "plain_text",
        name = "Plain Text",
        description = "Create QR code with any text content",
        icon = Icons.Default.Description,
        category = TemplateCategory.GENERAL,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = BarcodeFormat.values().toList(),
        fields = listOf(
            FieldDefinition(
                key = "text",
                label = "Text Content",
                type = FieldType.MULTILINE_TEXT,
                required = true,
                placeholder = "Enter any text",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields -> fields["text"] ?: "" }
    )

    private fun createNumberTemplate() = Template(
        id = "number",
        name = "Number",
        description = "Generate code for numeric data",
        icon = Icons.Default.Numbers,
        category = TemplateCategory.GENERAL,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E,
            BarcodeFormat.CODE_128
        ),
        fields = listOf(
            FieldDefinition(
                key = "number",
                label = "Number",
                type = FieldType.NUMBER,
                required = true,
                placeholder = "123456",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields -> fields["number"] ?: "" }
    )

    private fun createCustomDataTemplate() = Template(
        id = "custom_data",
        name = "Custom Data",
        description = "Any custom formatted data",
        icon = Icons.Default.DataObject,
        category = TemplateCategory.GENERAL,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = BarcodeFormat.values().toList(),
        fields = listOf(
            FieldDefinition(
                key = "data",
                label = "Custom Data",
                type = FieldType.MULTILINE_TEXT,
                required = true,
                placeholder = "Enter custom data",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields -> fields["data"] ?: "" }
    )

    private fun createPhoneTemplate() = Template(
        id = "phone",
        name = "Phone Number",
        description = "Create QR to call a phone number",
        icon = Icons.Default.Phone,
        category = TemplateCategory.COMMUNICATION,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.PDF417
        ),
        fields = listOf(
            FieldDefinition(
                key = "phone",
                label = "Phone Number",
                type = FieldType.PHONE,
                required = true,
                placeholder = "+1 234 567 8900",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields ->
            val phone = fields["phone"] ?: ""
            if (phone.startsWith("tel:")) phone else "tel:$phone"
        }
    )

    private fun createEmailTemplate() = Template(
        id = "email",
        name = "Email Address",
        description = "Send email with QR code scan",
        icon = Icons.Default.Email,
        category = TemplateCategory.COMMUNICATION,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.PDF417
        ),
        fields = listOf(
            FieldDefinition(
                key = "email",
                label = "Email Address",
                type = FieldType.EMAIL,
                required = true,
                placeholder = "user@example.com",
                validation = ValidationRule.Email
            ),
            FieldDefinition(
                key = "subject",
                label = "Subject (Optional)",
                type = FieldType.TEXT,
                placeholder = "Email subject"
            ),
            FieldDefinition(
                key = "body",
                label = "Message (Optional)",
                type = FieldType.MULTILINE_TEXT,
                placeholder = "Email message"
            )
        ),
        formatContentProvider = { fields ->
            val email = fields["email"] ?: ""
            val subject = fields["subject"]
            val body = fields["body"]

            buildString {
                append("mailto:$email")
                if (!subject.isNullOrBlank() || !body.isNullOrBlank()) {
                    append("?")
                    if (!subject.isNullOrBlank()) append("subject=$subject")
                    if (!subject.isNullOrBlank() && !body.isNullOrBlank()) append("&")
                    if (!body.isNullOrBlank()) append("body=$body")
                }
            }
        }
    )

    private fun createSMSTemplate() = Template(
        id = "sms",
        name = "SMS Message",
        description = "Send pre-filled SMS message",
        icon = Icons.Default.Sms,
        category = TemplateCategory.COMMUNICATION,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "phone",
                label = "Phone Number",
                type = FieldType.PHONE,
                required = true,
                placeholder = "+1 234 567 8900",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "message",
                label = "Message",
                type = FieldType.MULTILINE_TEXT,
                required = true,
                placeholder = "Your message here",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields ->
            val phone = fields["phone"] ?: ""
            val message = fields["message"] ?: ""
            "smsto:$phone:$message"
        }
    )

    private fun createContactTemplate() = Template(
        id = "contact",
        name = "Contact (vCard)",
        description = "Share complete contact information",
        icon = Icons.Default.ContactPage,
        category = TemplateCategory.COMMUNICATION,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "name",
                label = "Full Name",
                type = FieldType.TEXT,
                required = true,
                placeholder = "John Doe",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "phone",
                label = "Phone",
                type = FieldType.PHONE,
                placeholder = "+1 234 567 8900"
            ),
            FieldDefinition(
                key = "email",
                label = "Email",
                type = FieldType.EMAIL,
                placeholder = "john@example.com"
            ),
            FieldDefinition(
                key = "organization",
                label = "Organization",
                type = FieldType.TEXT,
                placeholder = "Company Name"
            ),
            FieldDefinition(
                key = "website",
                label = "Website",
                type = FieldType.URL,
                placeholder = "https://example.com"
            ),
            FieldDefinition(
                key = "address",
                label = "Address",
                type = FieldType.MULTILINE_TEXT,
                placeholder = "123 Main St, City, State, ZIP"
            )
        ),
        formatContentProvider = { fields ->
            buildString {
                appendLine("BEGIN:VCARD")
                appendLine("VERSION:3.0")
                appendLine("FN:${fields["name"]}")
                fields["phone"]?.let { if (it.isNotBlank()) appendLine("TEL:$it") }
                fields["email"]?.let { if (it.isNotBlank()) appendLine("EMAIL:$it") }
                fields["organization"]?.let { if (it.isNotBlank()) appendLine("ORG:$it") }
                fields["website"]?.let { if (it.isNotBlank()) appendLine("URL:$it") }
                fields["address"]?.let { if (it.isNotBlank()) appendLine("ADR:;;$it") }
                append("END:VCARD")
            }
        }
    )

    private fun createWhatsAppTemplate() = Template(
        id = "whatsapp",
        name = "WhatsApp",
        description = "Direct WhatsApp chat link",
        icon = Icons.AutoMirrored.Filled.Chat,
        category = TemplateCategory.COMMUNICATION,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "phone",
                label = "Phone Number (with country code)",
                type = FieldType.PHONE,
                required = true,
                placeholder = "1234567890",
                helperText = "Enter without + or spaces",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "message",
                label = "Pre-filled Message (Optional)",
                type = FieldType.MULTILINE_TEXT,
                placeholder = "Hello!"
            )
        ),
        formatContentProvider = { fields ->
            val phone = fields["phone"] ?: ""
            val message = fields["message"]
            buildString {
                append("https://wa.me/$phone")
                if (!message.isNullOrBlank()) {
                    append("?text=$message")
                }
            }
        }
    )

    private fun createURLTemplate() = Template(
        id = "url",
        name = "Website URL",
        description = "Open a website or web page",
        icon = Icons.Default.Language,
        category = TemplateCategory.SOCIAL_WEB,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.PDF417
        ),
        fields = listOf(
            FieldDefinition(
                key = "url",
                label = "URL",
                type = FieldType.URL,
                required = true,
                placeholder = "https://example.com",
                validation = ValidationRule.Url
            )
        ),
        formatContentProvider = { fields ->
            val url = fields["url"] ?: ""
            if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else {
                "https://$url"
            }
        }
    )

    private fun createSocialProfileTemplate() = Template(
        id = "social_profile",
        name = "Social Profile",
        description = "Link to social media profile",
        icon = Icons.Default.Share,
        category = TemplateCategory.SOCIAL_WEB,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "platform",
                label = "Platform",
                type = FieldType.DROPDOWN,
                required = true,
                options = listOf("Instagram", "Twitter", "Facebook", "LinkedIn", "TikTok", "YouTube", "GitHub"),
                defaultValue = "Instagram"
            ),
            FieldDefinition(
                key = "username",
                label = "Username",
                type = FieldType.TEXT,
                required = true,
                placeholder = "yourusername",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields ->
            val platform = fields["platform"] ?: "Instagram"
            val username = fields["username"] ?: ""
            when (platform) {
                "Instagram" -> "https://instagram.com/$username"
                "Twitter" -> "https://twitter.com/$username"
                "Facebook" -> "https://facebook.com/$username"
                "LinkedIn" -> "https://linkedin.com/in/$username"
                "TikTok" -> "https://tiktok.com/@$username"
                "YouTube" -> "https://youtube.com/@$username"
                "GitHub" -> "https://github.com/$username"
                else -> "https://$platform.com/$username"
            }
        }
    )

    private fun createWiFiTemplate() = Template(
        id = "wifi",
        name = "WiFi Credentials",
        description = "Share WiFi network credentials",
        icon = Icons.Default.Wifi,
        category = TemplateCategory.SOCIAL_WEB,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(BarcodeFormat.QR_CODE), // WiFi only supports QR
        fields = listOf(
            FieldDefinition(
                key = "ssid",
                label = "Network Name (SSID)",
                type = FieldType.TEXT,
                required = true,
                placeholder = "MyWiFiNetwork",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "password",
                label = "Password",
                type = FieldType.TEXT,
                required = true,
                placeholder = "Enter password",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "encryption",
                label = "Security Type",
                type = FieldType.DROPDOWN,
                required = true,
                options = listOf("WPA/WPA2", "WPA3", "WEP", "None"),
                defaultValue = "WPA/WPA2"
            ),
            FieldDefinition(
                key = "hidden",
                label = "Hidden Network",
                type = FieldType.CHECKBOX,
                defaultValue = "false"
            )
        ),
        formatContentProvider = { fields ->
            val encryption = when (fields["encryption"]) {
                "WPA/WPA2" -> "WPA"
                "WPA3" -> "SAE"
                "WEP" -> "WEP"
                else -> "nopass"
            }
            val hidden = if (fields["hidden"] == "true") "true" else "false"
            "WIFI:T:$encryption;S:${fields["ssid"]};P:${fields["password"]};H:$hidden;;"
        }
    )

    private fun createYouTubeTemplate() = Template(
        id = "youtube",
        name = "YouTube Video",
        description = "Direct link to YouTube video",
        icon = Icons.Default.VideoLibrary,
        category = TemplateCategory.SOCIAL_WEB,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "video_id",
                label = "Video URL or ID",
                type = FieldType.TEXT,
                required = true,
                placeholder = "dQw4w9WgXcQ or full URL",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields ->
            val input = fields["video_id"] ?: ""
            if (input.contains("youtube.com") || input.contains("youtu.be")) {
                input
            } else {
                "https://youtube.com/watch?v=$input"
            }
        }
    )

    private fun createGeoLocationTemplate() = Template(
        id = "geo_location",
        name = "Geographic Location",
        description = "Share GPS coordinates or address",
        icon = Icons.Default.LocationOn,
        category = TemplateCategory.LOCATION_EVENTS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "latitude",
                label = "Latitude",
                type = FieldType.NUMBER,
                required = true,
                placeholder = "37.7749",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "longitude",
                label = "Longitude",
                type = FieldType.NUMBER,
                required = true,
                placeholder = "-122.4194",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields ->
            val lat = fields["latitude"] ?: "0"
            val lon = fields["longitude"] ?: "0"
            "geo:$lat,$lon"
        }
    )

    private fun createCalendarEventTemplate() = Template(
        id = "calendar_event",
        name = "Calendar Event",
        description = "Add event to calendar",
        icon = Icons.Default.CalendarMonth,
        category = TemplateCategory.LOCATION_EVENTS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "title",
                label = "Event Title",
                type = FieldType.TEXT,
                required = true,
                placeholder = "Meeting with Team",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "start",
                label = "Start Date/Time",
                type = FieldType.DATETIME,
                required = true,
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "end",
                label = "End Date/Time",
                type = FieldType.DATETIME,
                required = true,
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "location",
                label = "Location",
                type = FieldType.TEXT,
                placeholder = "Office Room 101"
            ),
            FieldDefinition(
                key = "description",
                label = "Description",
                type = FieldType.MULTILINE_TEXT,
                placeholder = "Event details"
            )
        ),
        formatContentProvider = { fields ->
            buildString {
                appendLine("BEGIN:VEVENT")
                appendLine("SUMMARY:${fields["title"]}")
                appendLine("DTSTART:${fields["start"]}")
                appendLine("DTEND:${fields["end"]}")
                fields["location"]?.let { if (it.isNotBlank()) appendLine("LOCATION:$it") }
                fields["description"]?.let { if (it.isNotBlank()) appendLine("DESCRIPTION:$it") }
                append("END:VEVENT")
            }
        }
    )

    private fun createGoogleMapsTemplate() = Template(
        id = "google_maps",
        name = "Google Maps Location",
        description = "Direct Google Maps link",
        icon = Icons.Default.Map,
        category = TemplateCategory.LOCATION_EVENTS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "location",
                label = "Location Name or Address",
                type = FieldType.TEXT,
                required = true,
                placeholder = "Statue of Liberty, New York",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields ->
            val location = fields["location"] ?: ""
            "https://maps.google.com/?q=$location"
        }
    )

    private fun createBusinessCardTemplate() = Template(
        id = "business_card",
        name = "Business Card",
        description = "Professional digital business card",
        icon = Icons.Default.Badge,
        category = TemplateCategory.BUSINESS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "name",
                label = "Full Name",
                type = FieldType.TEXT,
                required = true,
                placeholder = "John Doe",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "title",
                label = "Job Title",
                type = FieldType.TEXT,
                placeholder = "Software Engineer"
            ),
            FieldDefinition(
                key = "company",
                label = "Company",
                type = FieldType.TEXT,
                placeholder = "Tech Corp"
            ),
            FieldDefinition(
                key = "phone",
                label = "Phone",
                type = FieldType.PHONE,
                placeholder = "+1 234 567 8900"
            ),
            FieldDefinition(
                key = "email",
                label = "Email",
                type = FieldType.EMAIL,
                placeholder = "john@techcorp.com"
            ),
            FieldDefinition(
                key = "website",
                label = "Website",
                type = FieldType.URL,
                placeholder = "https://techcorp.com"
            ),
            FieldDefinition(
                key = "linkedin",
                label = "LinkedIn",
                type = FieldType.TEXT,
                placeholder = "linkedin.com/in/johndoe"
            ),
            FieldDefinition(
                key = "address",
                label = "Address",
                type = FieldType.MULTILINE_TEXT,
                placeholder = "123 Main St, City, State, ZIP"
            )
        ),
        formatContentProvider = { fields ->
            buildString {
                appendLine("BEGIN:VCARD")
                appendLine("VERSION:3.0")
                appendLine("FN:${fields["name"]}")
                fields["title"]?.let { if (it.isNotBlank()) appendLine("TITLE:$it") }
                fields["company"]?.let { if (it.isNotBlank()) appendLine("ORG:$it") }
                fields["phone"]?.let { if (it.isNotBlank()) appendLine("TEL:$it") }
                fields["email"]?.let { if (it.isNotBlank()) appendLine("EMAIL:$it") }
                fields["website"]?.let { if (it.isNotBlank()) appendLine("URL:$it") }
                fields["linkedin"]?.let { if (it.isNotBlank()) appendLine("X-SOCIALPROFILE;TYPE=linkedin:$it") }
                fields["address"]?.let { if (it.isNotBlank()) appendLine("ADR:;;$it") }
                append("END:VCARD")
            }
        }
    )

    private fun createDigitalResumeTemplate() = Template(
        id = "digital_resume",
        name = "Digital Resume/Portfolio",
        description = "Link to online resume or portfolio",
        icon = Icons.Default.Work,
        category = TemplateCategory.BUSINESS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "name",
                label = "Your Name",
                type = FieldType.TEXT,
                required = true,
                placeholder = "John Doe",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "url",
                label = "Portfolio URL",
                type = FieldType.URL,
                required = true,
                placeholder = "https://johndoe.com",
                validation = ValidationRule.Url
            ),
            FieldDefinition(
                key = "headline",
                label = "Headline/Tagline",
                type = FieldType.TEXT,
                placeholder = "Full Stack Developer"
            )
        ),
        formatContentProvider = { fields ->
            val url = fields["url"] ?: ""
            if (url.startsWith("http")) url else "https://$url"
        }
    )

    private fun createCryptoTemplate() = Template(
        id = "crypto",
        name = "Cryptocurrency Address",
        description = "Share crypto wallet address",
        icon = Icons.Default.CurrencyBitcoin,
        category = TemplateCategory.BUSINESS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "crypto",
                label = "Cryptocurrency",
                type = FieldType.DROPDOWN,
                required = true,
                options = listOf("Bitcoin", "Ethereum", "USDT", "Litecoin", "Dogecoin", "Other"),
                defaultValue = "Bitcoin"
            ),
            FieldDefinition(
                key = "address",
                label = "Wallet Address",
                type = FieldType.TEXT,
                required = true,
                placeholder = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "amount",
                label = "Amount (Optional)",
                type = FieldType.NUMBER,
                placeholder = "0.001"
            )
        ),
        formatContentProvider = { fields ->
            val crypto = fields["crypto"] ?: "Bitcoin"
            val address = fields["address"] ?: ""
            val amount = fields["amount"]

            when (crypto.lowercase()) {
                "bitcoin" -> {
                    if (!amount.isNullOrBlank()) {
                        "bitcoin:$address?amount=$amount"
                    } else {
                        "bitcoin:$address"
                    }
                }
                "ethereum" -> {
                    if (!amount.isNullOrBlank()) {
                        "ethereum:$address?value=$amount"
                    } else {
                        "ethereum:$address"
                    }
                }
                else -> address
            }
        }
    )

    private fun createPaymentLinkTemplate() = Template(
        id = "payment_link",
        name = "Payment Link",
        description = "Payment gateway or UPI link",
        icon = Icons.Default.Payment,
        category = TemplateCategory.BUSINESS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC
        ),
        fields = listOf(
            FieldDefinition(
                key = "gateway",
                label = "Payment Gateway",
                type = FieldType.DROPDOWN,
                required = true,
                options = listOf("UPI", "PayPal", "Stripe", "Other"),
                defaultValue = "UPI"
            ),
            FieldDefinition(
                key = "id",
                label = "Payment ID/UPI ID/URL",
                type = FieldType.TEXT,
                required = true,
                placeholder = "user@upi or payment URL",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "amount",
                label = "Amount (Optional)",
                type = FieldType.NUMBER,
                placeholder = "100.00"
            )
        ),
        formatContentProvider = { fields ->
            val gateway = fields["gateway"] ?: "UPI"
            val id = fields["id"] ?: ""
            val amount = fields["amount"]

            when (gateway) {
                "UPI" -> {
                    if (!amount.isNullOrBlank()) {
                        "upi://pay?pa=$id&am=$amount"
                    } else {
                        "upi://pay?pa=$id"
                    }
                }
                else -> id
            }
        }
    )

    private fun createProductBarcodeTemplate() = Template(
        id = "product_barcode",
        name = "Product Barcode",
        description = "Standard product barcode (EAN/UPC)",
        icon = Icons.Default.QrCode2,
        category = TemplateCategory.PRODUCT,
        defaultFormat = BarcodeFormat.EAN_13,
        allowedFormats = listOf(
            BarcodeFormat.EAN_8,
            BarcodeFormat.EAN_13,
            BarcodeFormat.UPC_A,
            BarcodeFormat.UPC_E
        ),
        fields = listOf(
            FieldDefinition(
                key = "barcode",
                label = "Barcode Number",
                type = FieldType.NUMBER,
                required = true,
                placeholder = "1234567890123",
                helperText = "13 digits for EAN-13, 8 for EAN-8",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields -> fields["barcode"] ?: "" }
    )

    private fun createProductQRTemplate() = Template(
        id = "product_qr",
        name = "Product QR Code",
        description = "Product info with custom data",
        icon = Icons.Default.QrCode,
        category = TemplateCategory.PRODUCT,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX,
            BarcodeFormat.PDF417
        ),
        fields = listOf(
            FieldDefinition(
                key = "name",
                label = "Product Name",
                type = FieldType.TEXT,
                required = true,
                placeholder = "Product XYZ",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "sku",
                label = "SKU/ID",
                type = FieldType.TEXT,
                placeholder = "SKU-12345"
            ),
            FieldDefinition(
                key = "price",
                label = "Price",
                type = FieldType.NUMBER,
                placeholder = "99.99"
            ),
            FieldDefinition(
                key = "description",
                label = "Description",
                type = FieldType.MULTILINE_TEXT,
                placeholder = "Product details"
            ),
            FieldDefinition(
                key = "url",
                label = "Product URL",
                type = FieldType.URL,
                placeholder = "https://example.com/product"
            )
        ),
        formatContentProvider = { fields ->
            val url = fields["url"]
            if (!url.isNullOrBlank()) {
                if (url.startsWith("http")) url else "https://$url"
            } else {
                buildString {
                    append("Product: ${fields["name"]}")
                    fields["sku"]?.let { if (it.isNotBlank()) append("\nSKU: $it") }
                    fields["price"]?.let { if (it.isNotBlank()) append("\nPrice: $$it") }
                    fields["description"]?.let { if (it.isNotBlank()) append("\n$it") }
                }
            }
        }
    )

    private fun createInventoryTagTemplate() = Template(
        id = "inventory_tag",
        name = "Inventory Tag",
        description = "Track inventory items",
        icon = Icons.Default.Inventory,
        category = TemplateCategory.PRODUCT,
        defaultFormat = BarcodeFormat.CODE_128,
        allowedFormats = listOf(
            BarcodeFormat.CODE_128,
            BarcodeFormat.CODE_39,
            BarcodeFormat.QR_CODE,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "item_id",
                label = "Item ID",
                type = FieldType.TEXT,
                required = true,
                placeholder = "INV-12345",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "location",
                label = "Location",
                type = FieldType.TEXT,
                placeholder = "Warehouse A, Shelf 3"
            ),
            FieldDefinition(
                key = "category",
                label = "Category",
                type = FieldType.TEXT,
                placeholder = "Electronics"
            )
        ),
        formatContentProvider = { fields ->
            buildString {
                append(fields["item_id"])
                fields["location"]?.let { if (it.isNotBlank()) append("|$it") }
                fields["category"]?.let { if (it.isNotBlank()) append("|$it") }
            }
        }
    )

    private fun createPDFLinkTemplate() = Template(
        id = "pdf_link",
        name = "PDF Document Link",
        description = "Link to PDF document",
        icon = Icons.Default.PictureAsPdf,
        category = TemplateCategory.DOCUMENTS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "title",
                label = "Document Title",
                type = FieldType.TEXT,
                required = true,
                placeholder = "User Manual",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "url",
                label = "Download URL",
                type = FieldType.URL,
                required = true,
                placeholder = "https://example.com/document.pdf",
                validation = ValidationRule.Url
            )
        ),
        formatContentProvider = { fields ->
            val url = fields["url"] ?: ""
            if (url.startsWith("http")) url else "https://$url"
        }
    )

    private fun createAppDownloadTemplate() = Template(
        id = "app_download",
        name = "App Download Link",
        description = "Link to app store listing",
        icon = Icons.Default.Apps,
        category = TemplateCategory.DOCUMENTS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "platform",
                label = "Platform",
                type = FieldType.DROPDOWN,
                required = true,
                options = listOf("Play Store", "App Store", "Both/Custom URL"),
                defaultValue = "Play Store"
            ),
            FieldDefinition(
                key = "app_id",
                label = "Package/App ID or URL",
                type = FieldType.TEXT,
                required = true,
                placeholder = "com.example.app or full URL",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields ->
            val platform = fields["platform"] ?: "Play Store"
            val appId = fields["app_id"] ?: ""

            when (platform) {
                "Play Store" -> {
                    if (appId.startsWith("http")) appId
                    else "https://play.google.com/store/apps/details?id=$appId"
                }
                "App Store" -> {
                    if (appId.startsWith("http")) appId
                    else "https://apps.apple.com/app/id$appId"
                }
                else -> appId
            }
        }
    )

    private fun createFileShareTemplate() = Template(
        id = "file_share",
        name = "File Share (Cloud)",
        description = "Share cloud storage link",
        icon = Icons.Default.CloudUpload,
        category = TemplateCategory.DOCUMENTS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC,
            BarcodeFormat.DATA_MATRIX
        ),
        fields = listOf(
            FieldDefinition(
                key = "service",
                label = "Service",
                type = FieldType.DROPDOWN,
                required = true,
                options = listOf("Google Drive", "Dropbox", "OneDrive", "Other"),
                defaultValue = "Google Drive"
            ),
            FieldDefinition(
                key = "link",
                label = "Share Link",
                type = FieldType.URL,
                required = true,
                placeholder = "https://drive.google.com/file/...",
                validation = ValidationRule.Url
            )
        ),
        formatContentProvider = { fields ->
            val link = fields["link"] ?: ""
            if (link.startsWith("http")) link else "https://$link"
        }
    )

    private fun createEventTicketTemplate() = Template(
        id = "event_ticket",
        name = "Event Ticket",
        description = "Event ticket with details",
        icon = Icons.Default.ConfirmationNumber,
        category = TemplateCategory.TICKETS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.PDF417,
            BarcodeFormat.AZTEC
        ),
        fields = listOf(
            FieldDefinition(
                key = "event",
                label = "Event Name",
                type = FieldType.TEXT,
                required = true,
                placeholder = "Concert 2024",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "ticket_id",
                label = "Ticket ID",
                type = FieldType.TEXT,
                required = true,
                placeholder = "TKT-123456",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "date",
                label = "Event Date",
                type = FieldType.DATE,
                required = true,
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "venue",
                label = "Venue",
                type = FieldType.TEXT,
                placeholder = "Madison Square Garden"
            ),
            FieldDefinition(
                key = "seat",
                label = "Seat Number",
                type = FieldType.TEXT,
                placeholder = "Section A, Row 5, Seat 12"
            )
        ),
        formatContentProvider = { fields ->
            buildString {
                appendLine("Event: ${fields["event"]}")
                appendLine("Ticket: ${fields["ticket_id"]}")
                appendLine("Date: ${fields["date"]}")
                fields["venue"]?.let { if (it.isNotBlank()) appendLine("Venue: $it") }
                fields["seat"]?.let { if (it.isNotBlank()) appendLine("Seat: $it") }
            }
        }
    )

    private fun createBoardingPassTemplate() = Template(
        id = "boarding_pass",
        name = "Boarding Pass",
        description = "Flight/train boarding pass",
        icon = Icons.Default.Flight,
        category = TemplateCategory.TICKETS,
        defaultFormat = BarcodeFormat.PDF417,
        allowedFormats = listOf(
            BarcodeFormat.PDF417,
            BarcodeFormat.QR_CODE,
            BarcodeFormat.AZTEC
        ),
        fields = listOf(
            FieldDefinition(
                key = "passenger",
                label = "Passenger Name",
                type = FieldType.TEXT,
                required = true,
                placeholder = "John Doe",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "flight",
                label = "Flight/Train Number",
                type = FieldType.TEXT,
                required = true,
                placeholder = "AA123",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "seat",
                label = "Seat",
                type = FieldType.TEXT,
                required = true,
                placeholder = "12A",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "pnr",
                label = "PNR/Booking Reference",
                type = FieldType.TEXT,
                required = true,
                placeholder = "ABC123",
                validation = ValidationRule.Required()
            )
        ),
        formatContentProvider = { fields ->
            buildString {
                appendLine("Passenger: ${fields["passenger"]}")
                appendLine("Flight: ${fields["flight"]}")
                appendLine("Seat: ${fields["seat"]}")
                appendLine("PNR: ${fields["pnr"]}")
            }
        }
    )

    private fun createCouponTemplate() = Template(
        id = "coupon",
        name = "Coupon/Voucher",
        description = "Discount coupon or voucher",
        icon = Icons.Default.LocalOffer,
        category = TemplateCategory.TICKETS,
        defaultFormat = BarcodeFormat.QR_CODE,
        allowedFormats = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.CODE_128,
            BarcodeFormat.EAN_13
        ),
        fields = listOf(
            FieldDefinition(
                key = "code",
                label = "Coupon Code",
                type = FieldType.TEXT,
                required = true,
                placeholder = "SAVE20",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "description",
                label = "Description",
                type = FieldType.TEXT,
                required = true,
                placeholder = "20% off all items",
                validation = ValidationRule.Required()
            ),
            FieldDefinition(
                key = "expiry",
                label = "Expiry Date",
                type = FieldType.DATE,
                placeholder = "2024-12-31"
            ),
            FieldDefinition(
                key = "terms",
                label = "Terms & Conditions",
                type = FieldType.MULTILINE_TEXT,
                placeholder = "Valid for one-time use only"
            )
        ),
        formatContentProvider = { fields ->
            buildString {
                append(fields["code"])
                append("|${fields["description"]}")
                fields["expiry"]?.let { if (it.isNotBlank()) append("|Expires: $it") }
            }
        }
    )
}
