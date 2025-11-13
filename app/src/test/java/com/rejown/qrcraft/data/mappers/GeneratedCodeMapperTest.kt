package com.rejown.qrcraft.data.mappers

import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ErrorCorrectionLevel
import com.rejown.qrcraft.domain.models.GeneratedCodeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for GeneratedCodeMapper
 * Tests entity to domain and domain to entity conversions
 */
class GeneratedCodeMapperTest {

    @Test
    fun `toDomain converts entity to domain model correctly`() {
        val entity = GeneratedCodeEntity(
            id = 1L,
            templateId = "url",
            templateName = "Website URL",
            barcodeFormat = "QR_CODE",
            barcodeType = "QR Code",
            title = "My Website",
            note = "Company website",
            contentFields = """{"url":"https://example.com"}""",
            formattedContent = "https://example.com",
            foregroundColor = 0xFF000000.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt(),
            size = 512,
            errorCorrection = "HIGH",
            margin = 4,
            createdAt = 1000000L,
            updatedAt = 1000000L,
            isFavorite = true,
            scanCount = 5
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("url", domain.templateId)
        assertEquals("Website URL", domain.templateName)
        assertEquals(BarcodeFormat.QR_CODE, domain.barcodeFormat)
        assertEquals("QR Code", domain.barcodeType)
        assertEquals("My Website", domain.title)
        assertEquals("Company website", domain.note)
        assertEquals(mapOf("url" to "https://example.com"), domain.contentFields)
        assertEquals("https://example.com", domain.formattedContent)
        assertEquals(0xFF000000.toInt(), domain.foregroundColor)
        assertEquals(0xFFFFFFFF.toInt(), domain.backgroundColor)
        assertEquals(512, domain.size)
        assertEquals(ErrorCorrectionLevel.HIGH, domain.errorCorrection)
        assertEquals(4, domain.margin)
        assertEquals(1000000L, domain.createdAt)
        assertEquals(1000000L, domain.updatedAt)
        assertTrue(domain.isFavorite)
        assertEquals(5, domain.scanCount)
    }

    @Test
    fun `toDomain handles null errorCorrection`() {
        val entity = GeneratedCodeEntity(
            id = 1L,
            templateId = "text",
            templateName = "Plain Text",
            barcodeFormat = "CODE_128",
            barcodeType = "CODE 128",
            title = "Test",
            note = null,
            contentFields = """{"text":"Hello"}""",
            formattedContent = "Hello",
            foregroundColor = 0xFF000000.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt(),
            size = 256,
            errorCorrection = null,
            margin = 0,
            createdAt = 1000000L,
            updatedAt = 1000000L,
            isFavorite = false,
            scanCount = 0
        )

        val domain = entity.toDomain()

        assertNull(domain.errorCorrection)
    }

    @Test
    fun `toDomain handles invalid errorCorrection string`() {
        val entity = GeneratedCodeEntity(
            id = 1L,
            templateId = "text",
            templateName = "Plain Text",
            barcodeFormat = "QR_CODE",
            barcodeType = "QR Code",
            title = "Test",
            note = null,
            contentFields = """{}""",
            formattedContent = "Test",
            foregroundColor = 0xFF000000.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt(),
            size = 256,
            errorCorrection = "INVALID",
            margin = 0,
            createdAt = 1000000L,
            updatedAt = 1000000L,
            isFavorite = false,
            scanCount = 0
        )

        val domain = entity.toDomain()

        assertNull(domain.errorCorrection)
    }

    @Test
    fun `toDomain handles invalid JSON in contentFields`() {
        val entity = GeneratedCodeEntity(
            id = 1L,
            templateId = "text",
            templateName = "Plain Text",
            barcodeFormat = "QR_CODE",
            barcodeType = "QR Code",
            title = "Test",
            note = null,
            contentFields = "invalid json",
            formattedContent = "Test",
            foregroundColor = 0xFF000000.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt(),
            size = 256,
            errorCorrection = null,
            margin = 0,
            createdAt = 1000000L,
            updatedAt = 1000000L,
            isFavorite = false,
            scanCount = 0
        )

        val domain = entity.toDomain()

        assertTrue(domain.contentFields.isEmpty())
    }

    @Test
    fun `toEntity converts domain to entity correctly`() {
        val domain = GeneratedCodeData(
            id = 2L,
            templateId = "email",
            templateName = "Email Address",
            barcodeFormat = BarcodeFormat.QR_CODE,
            barcodeType = "QR Code",
            title = "Contact Email",
            note = "Business email",
            contentFields = mapOf("email" to "test@example.com"),
            formattedContent = "mailto:test@example.com",
            foregroundColor = 0xFF0000FF.toInt(),
            backgroundColor = 0xFFFFFF00.toInt(),
            size = 1024,
            errorCorrection = ErrorCorrectionLevel.MEDIUM,
            margin = 2,
            createdAt = 2000000L,
            updatedAt = 2000000L,
            isFavorite = false,
            scanCount = 10
        )

        val entity = domain.toEntity()

        assertEquals(2L, entity.id)
        assertEquals("email", entity.templateId)
        assertEquals("Email Address", entity.templateName)
        assertEquals("QR_CODE", entity.barcodeFormat)
        assertEquals("QR Code", entity.barcodeType)
        assertEquals("Contact Email", entity.title)
        assertEquals("Business email", entity.note)
        assertTrue(entity.contentFields.contains("email"))
        assertTrue(entity.contentFields.contains("test@example.com"))
        assertEquals("mailto:test@example.com", entity.formattedContent)
        assertEquals(0xFF0000FF.toInt(), entity.foregroundColor)
        assertEquals(0xFFFFFF00.toInt(), entity.backgroundColor)
        assertEquals(1024, entity.size)
        assertEquals("MEDIUM", entity.errorCorrection)
        assertEquals(2, entity.margin)
        assertEquals(2000000L, entity.createdAt)
        assertEquals(2000000L, entity.updatedAt)
        assertEquals(false, entity.isFavorite)
        assertEquals(10, entity.scanCount)
    }

    @Test
    fun `toEntity handles null errorCorrection`() {
        val domain = GeneratedCodeData(
            id = 1L,
            templateId = "text",
            templateName = "Plain Text",
            barcodeFormat = BarcodeFormat.CODE_128,
            barcodeType = "CODE 128",
            title = "Test",
            note = null,
            contentFields = mapOf("text" to "Hello"),
            formattedContent = "Hello",
            foregroundColor = 0xFF000000.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt(),
            size = 256,
            errorCorrection = null,
            margin = 0,
            createdAt = 1000000L,
            updatedAt = 1000000L,
            isFavorite = false,
            scanCount = 0
        )

        val entity = domain.toEntity()

        assertNull(entity.errorCorrection)
    }

    @Test
    fun `toDomain and toEntity are reversible`() {
        val originalEntity = GeneratedCodeEntity(
            id = 3L,
            templateId = "phone",
            templateName = "Phone Number",
            barcodeFormat = "QR_CODE",
            barcodeType = "QR Code",
            title = "My Phone",
            note = "Personal",
            contentFields = """{"phone":"+1234567890"}""",
            formattedContent = "tel:+1234567890",
            foregroundColor = 0xFF000000.toInt(),
            backgroundColor = 0xFFFFFFFF.toInt(),
            size = 512,
            errorCorrection = "LOW",
            margin = 4,
            createdAt = 3000000L,
            updatedAt = 3000000L,
            isFavorite = true,
            scanCount = 3
        )

        val domain = originalEntity.toDomain()
        val entity = domain.toEntity()

        assertEquals(originalEntity.id, entity.id)
        assertEquals(originalEntity.templateId, entity.templateId)
        assertEquals(originalEntity.templateName, entity.templateName)
        assertEquals(originalEntity.barcodeFormat, entity.barcodeFormat)
        assertEquals(originalEntity.barcodeType, entity.barcodeType)
        assertEquals(originalEntity.title, entity.title)
        assertEquals(originalEntity.note, entity.note)
        assertEquals(originalEntity.formattedContent, entity.formattedContent)
        assertEquals(originalEntity.foregroundColor, entity.foregroundColor)
        assertEquals(originalEntity.backgroundColor, entity.backgroundColor)
        assertEquals(originalEntity.size, entity.size)
        assertEquals(originalEntity.errorCorrection, entity.errorCorrection)
        assertEquals(originalEntity.margin, entity.margin)
        assertEquals(originalEntity.createdAt, entity.createdAt)
        assertEquals(originalEntity.updatedAt, entity.updatedAt)
        assertEquals(originalEntity.isFavorite, entity.isFavorite)
        assertEquals(originalEntity.scanCount, entity.scanCount)
    }

    @Test
    fun `toDomainList converts list of entities to domain models`() {
        val entities = listOf(
            GeneratedCodeEntity(
                id = 1L,
                templateId = "url",
                templateName = "URL",
                barcodeFormat = "QR_CODE",
                barcodeType = "QR Code",
                title = "Test 1",
                note = null,
                contentFields = """{}""",
                formattedContent = "Test",
                foregroundColor = 0xFF000000.toInt(),
                backgroundColor = 0xFFFFFFFF.toInt(),
                size = 256,
                errorCorrection = null,
                margin = 0,
                createdAt = 1000000L,
                updatedAt = 1000000L,
                isFavorite = false,
                scanCount = 0
            ),
            GeneratedCodeEntity(
                id = 2L,
                templateId = "email",
                templateName = "Email",
                barcodeFormat = "QR_CODE",
                barcodeType = "QR Code",
                title = "Test 2",
                note = null,
                contentFields = """{}""",
                formattedContent = "Test",
                foregroundColor = 0xFF000000.toInt(),
                backgroundColor = 0xFFFFFFFF.toInt(),
                size = 256,
                errorCorrection = null,
                margin = 0,
                createdAt = 1000000L,
                updatedAt = 1000000L,
                isFavorite = false,
                scanCount = 0
            )
        )

        val domainList = entities.toDomainList()

        assertEquals(2, domainList.size)
        assertEquals(1L, domainList[0].id)
        assertEquals(2L, domainList[1].id)
        assertEquals("url", domainList[0].templateId)
        assertEquals("email", domainList[1].templateId)
    }

    @Test
    fun `toDomainList handles empty list`() {
        val entities = emptyList<GeneratedCodeEntity>()
        val domainList = entities.toDomainList()
        assertTrue(domainList.isEmpty())
    }

    @Test
    fun `BarcodeFormat conversion handles all formats`() {
        val formats = listOf(
            "QR_CODE" to BarcodeFormat.QR_CODE,
            "CODE_128" to BarcodeFormat.CODE_128,
            "CODE_39" to BarcodeFormat.CODE_39,
            "EAN_13" to BarcodeFormat.EAN_13,
            "EAN_8" to BarcodeFormat.EAN_8,
            "UPC_A" to BarcodeFormat.UPC_A,
            "UPC_E" to BarcodeFormat.UPC_E,
            "DATA_MATRIX" to BarcodeFormat.DATA_MATRIX,
            "PDF417" to BarcodeFormat.PDF417,
            "AZTEC" to BarcodeFormat.AZTEC
        )

        formats.forEach { (stringFormat, enumFormat) ->
            val entity = GeneratedCodeEntity(
                id = 1L,
                templateId = "test",
                templateName = "Test",
                barcodeFormat = stringFormat,
                barcodeType = "Test",
                title = "Test",
                note = null,
                contentFields = """{}""",
                formattedContent = "Test",
                foregroundColor = 0xFF000000.toInt(),
                backgroundColor = 0xFFFFFFFF.toInt(),
                size = 256,
                errorCorrection = null,
                margin = 0,
                createdAt = 1000000L,
                updatedAt = 1000000L,
                isFavorite = false,
                scanCount = 0
            )

            val domain = entity.toDomain()
            assertEquals(enumFormat, domain.barcodeFormat)
        }
    }
}
