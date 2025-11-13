package com.rejown.qrcraft.data.mappers

import com.rejown.qrcraft.data.local.database.entities.ScanHistoryEntity
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ContentType
import com.rejown.qrcraft.domain.models.ScanHistory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ScanHistoryMapper
 * Tests entity to domain and domain to entity conversions
 */
class ScanHistoryMapperTest {

    @Test
    fun `toDomain converts entity to domain model correctly`() {
        val entity = ScanHistoryEntity(
            id = 1L,
            content = "https://example.com",
            rawValue = "https://example.com",
            format = "QR_CODE",
            contentType = "URL",
            timestamp = 1000000L,
            isFavorite = true,
            metadata = """{"source":"camera"}"""
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("https://example.com", domain.content)
        assertEquals("https://example.com", domain.rawValue)
        assertEquals(BarcodeFormat.QR_CODE, domain.format)
        assertEquals(ContentType.URL, domain.contentType)
        assertEquals(1000000L, domain.timestamp)
        assertTrue(domain.isFavorite)
        assertNotNull(domain.metadata)
        assertEquals("camera", domain.metadata?.get("source"))
    }

    @Test
    fun `toDomain handles null metadata`() {
        val entity = ScanHistoryEntity(
            id = 1L,
            content = "Test",
            rawValue = "Test",
            format = "QR_CODE",
            contentType = "TEXT",
            timestamp = 1000000L,
            isFavorite = false,
            metadata = null
        )

        val domain = entity.toDomain()

        assertNull(domain.metadata)
    }

    @Test
    fun `toDomain handles invalid JSON in metadata`() {
        val entity = ScanHistoryEntity(
            id = 1L,
            content = "Test",
            rawValue = "Test",
            format = "QR_CODE",
            contentType = "TEXT",
            timestamp = 1000000L,
            isFavorite = false,
            metadata = "invalid json"
        )

        val domain = entity.toDomain()

        assertNull(domain.metadata)
    }

    @Test
    fun `toEntity converts domain to entity correctly`() {
        val domain = ScanHistory(
            id = 2L,
            content = "test@example.com",
            rawValue = "mailto:test@example.com",
            format = BarcodeFormat.QR_CODE,
            contentType = ContentType.EMAIL,
            timestamp = 2000000L,
            isFavorite = false,
            metadata = mapOf("scannedBy" to "user1")
        )

        val entity = domain.toEntity()

        assertEquals(2L, entity.id)
        assertEquals("test@example.com", entity.content)
        assertEquals("mailto:test@example.com", entity.rawValue)
        assertEquals("QR_CODE", entity.format)
        assertEquals("EMAIL", entity.contentType)
        assertEquals(2000000L, entity.timestamp)
        assertFalse(entity.isFavorite)
        assertNotNull(entity.metadata)
        assertTrue(entity.metadata!!.contains("scannedBy"))
        assertTrue(entity.metadata!!.contains("user1"))
    }

    @Test
    fun `toEntity handles null metadata`() {
        val domain = ScanHistory(
            id = 1L,
            content = "Test",
            rawValue = "Test",
            format = BarcodeFormat.CODE_128,
            contentType = ContentType.TEXT,
            timestamp = 1000000L,
            isFavorite = false,
            metadata = null
        )

        val entity = domain.toEntity()

        assertNull(entity.metadata)
    }

    @Test
    fun `toDomain and toEntity are reversible`() {
        val originalEntity = ScanHistoryEntity(
            id = 3L,
            content = "+1234567890",
            rawValue = "tel:+1234567890",
            format = "QR_CODE",
            contentType = "PHONE",
            timestamp = 3000000L,
            isFavorite = true,
            metadata = """{"location":"office"}"""
        )

        val domain = originalEntity.toDomain()
        val entity = domain.toEntity()

        assertEquals(originalEntity.id, entity.id)
        assertEquals(originalEntity.content, entity.content)
        assertEquals(originalEntity.rawValue, entity.rawValue)
        assertEquals(originalEntity.format, entity.format)
        assertEquals(originalEntity.contentType, entity.contentType)
        assertEquals(originalEntity.timestamp, entity.timestamp)
        assertEquals(originalEntity.isFavorite, entity.isFavorite)
    }

    @Test
    fun `toDomainList converts list of entities to domain models`() {
        val entities = listOf(
            ScanHistoryEntity(
                id = 1L,
                content = "Test 1",
                rawValue = "Test 1",
                format = "QR_CODE",
                contentType = "TEXT",
                timestamp = 1000000L,
                isFavorite = false,
                metadata = null
            ),
            ScanHistoryEntity(
                id = 2L,
                content = "Test 2",
                rawValue = "Test 2",
                format = "CODE_128",
                contentType = "TEXT",
                timestamp = 2000000L,
                isFavorite = true,
                metadata = null
            )
        )

        val domainList = entities.toDomainList()

        assertEquals(2, domainList.size)
        assertEquals(1L, domainList[0].id)
        assertEquals(2L, domainList[1].id)
        assertEquals("Test 1", domainList[0].content)
        assertEquals("Test 2", domainList[1].content)
        assertFalse(domainList[0].isFavorite)
        assertTrue(domainList[1].isFavorite)
    }

    @Test
    fun `toDomainList handles empty list`() {
        val entities = emptyList<ScanHistoryEntity>()
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
            val entity = ScanHistoryEntity(
                id = 1L,
                content = "Test",
                rawValue = "Test",
                format = stringFormat,
                contentType = "TEXT",
                timestamp = 1000000L,
                isFavorite = false,
                metadata = null
            )

            val domain = entity.toDomain()
            assertEquals(enumFormat, domain.format)
        }
    }

    @Test
    fun `ContentType conversion handles all types`() {
        val types = listOf(
            "TEXT" to ContentType.TEXT,
            "URL" to ContentType.URL,
            "EMAIL" to ContentType.EMAIL,
            "PHONE" to ContentType.PHONE,
            "SMS" to ContentType.SMS,
            "WIFI" to ContentType.WIFI,
            "CONTACT" to ContentType.CONTACT,
            "CALENDAR" to ContentType.CALENDAR,
            "GEO" to ContentType.GEO,
            "PRODUCT" to ContentType.PRODUCT
        )

        types.forEach { (stringType, enumType) ->
            val entity = ScanHistoryEntity(
                id = 1L,
                content = "Test",
                rawValue = "Test",
                format = "QR_CODE",
                contentType = stringType,
                timestamp = 1000000L,
                isFavorite = false,
                metadata = null
            )

            val domain = entity.toDomain()
            assertEquals(enumType, domain.contentType)
        }
    }

    @Test
    fun `mapping handles complex metadata`() {
        val complexMetadata = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3"
        )

        val domain = ScanHistory(
            id = 1L,
            content = "Test",
            rawValue = "Test",
            format = BarcodeFormat.QR_CODE,
            contentType = ContentType.TEXT,
            timestamp = 1000000L,
            isFavorite = false,
            metadata = complexMetadata
        )

        val entity = domain.toEntity()
        val convertedDomain = entity.toDomain()

        assertEquals(complexMetadata.size, convertedDomain.metadata?.size)
        assertEquals("value1", convertedDomain.metadata?.get("key1"))
        assertEquals("value2", convertedDomain.metadata?.get("key2"))
        assertEquals("value3", convertedDomain.metadata?.get("key3"))
    }

    @Test
    fun `mapping handles special characters in content`() {
        val specialContent = "Test with special chars: !@#$%^&*()_+-=[]{}|;:',.<>?/~`"

        val domain = ScanHistory(
            id = 1L,
            content = specialContent,
            rawValue = specialContent,
            format = BarcodeFormat.QR_CODE,
            contentType = ContentType.TEXT,
            timestamp = 1000000L,
            isFavorite = false,
            metadata = null
        )

        val entity = domain.toEntity()
        val convertedDomain = entity.toDomain()

        assertEquals(specialContent, convertedDomain.content)
        assertEquals(specialContent, convertedDomain.rawValue)
    }

    @Test
    fun `mapping handles unicode characters`() {
        val unicodeContent = "Hello 世界 🌍 مرحبا"

        val domain = ScanHistory(
            id = 1L,
            content = unicodeContent,
            rawValue = unicodeContent,
            format = BarcodeFormat.QR_CODE,
            contentType = ContentType.TEXT,
            timestamp = 1000000L,
            isFavorite = false,
            metadata = null
        )

        val entity = domain.toEntity()
        val convertedDomain = entity.toDomain()

        assertEquals(unicodeContent, convertedDomain.content)
        assertEquals(unicodeContent, convertedDomain.rawValue)
    }
}
