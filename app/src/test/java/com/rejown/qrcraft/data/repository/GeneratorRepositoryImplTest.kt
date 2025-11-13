package com.rejown.qrcraft.data.repository

import app.cash.turbine.test
import com.rejown.qrcraft.data.local.database.dao.GeneratedCodeDao
import com.rejown.qrcraft.data.local.database.entities.GeneratedCodeEntity
import com.rejown.qrcraft.domain.models.BarcodeFormat
import com.rejown.qrcraft.domain.models.ErrorCorrectionLevel
import com.rejown.qrcraft.domain.models.GeneratedCodeData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GeneratorRepositoryImpl
 * Tests repository operations with mocked DAO
 */
class GeneratorRepositoryImplTest {

    private lateinit var generatedCodeDao: GeneratedCodeDao
    private lateinit var repository: GeneratorRepositoryImpl

    private val testEntity = GeneratedCodeEntity(
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
        isFavorite = false,
        scanCount = 0
    )

    private val testDomain = GeneratedCodeData(
        id = 1L,
        templateId = "url",
        templateName = "Website URL",
        barcodeFormat = BarcodeFormat.QR_CODE,
        barcodeType = "QR Code",
        title = "My Website",
        note = "Company website",
        contentFields = mapOf("url" to "https://example.com"),
        formattedContent = "https://example.com",
        foregroundColor = 0xFF000000.toInt(),
        backgroundColor = 0xFFFFFFFF.toInt(),
        size = 512,
        errorCorrection = ErrorCorrectionLevel.HIGH,
        margin = 4,
        createdAt = 1000000L,
        updatedAt = 1000000L,
        isFavorite = false,
        scanCount = 0
    )

    @Before
    fun setup() {
        generatedCodeDao = mockk()
        repository = GeneratorRepositoryImpl(generatedCodeDao)
    }

    @Test
    fun `getAllGenerated returns flow of domain models`() = runTest {
        val entities = listOf(testEntity)
        coEvery { generatedCodeDao.getAllGenerated() } returns flowOf(entities)

        repository.getAllGenerated().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(1L, result[0].id)
            assertEquals("url", result[0].templateId)
            assertEquals("My Website", result[0].title)
            awaitComplete()
        }
    }

    @Test
    fun `getAllGenerated returns empty list when no data`() = runTest {
        coEvery { generatedCodeDao.getAllGenerated() } returns flowOf(emptyList())

        repository.getAllGenerated().test {
            val result = awaitItem()
            assertEquals(0, result.size)
            awaitComplete()
        }
    }

    @Test
    fun `getFavorites returns only favorite items`() = runTest {
        val favoriteEntity = testEntity.copy(id = 2L, isFavorite = true)
        coEvery { generatedCodeDao.getFavorites() } returns flowOf(listOf(favoriteEntity))

        repository.getFavorites().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(true, result[0].isFavorite)
            awaitComplete()
        }
    }

    @Test
    fun `searchGenerated returns matching items`() = runTest {
        val query = "website"
        coEvery { generatedCodeDao.searchGenerated(query) } returns flowOf(listOf(testEntity))

        repository.searchGenerated(query).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("My Website", result[0].title)
            awaitComplete()
        }

        coVerify { generatedCodeDao.searchGenerated(query) }
    }

    @Test
    fun `getGeneratedByType returns items of specific type`() = runTest {
        val type = "QR Code"
        coEvery { generatedCodeDao.getGeneratedByType(type) } returns flowOf(listOf(testEntity))

        repository.getGeneratedByType(type).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("QR Code", result[0].barcodeType)
            awaitComplete()
        }

        coVerify { generatedCodeDao.getGeneratedByType(type) }
    }

    @Test
    fun `getGeneratedById returns domain model when found`() = runTest {
        val id = 1L
        coEvery { generatedCodeDao.getGeneratedById(id) } returns testEntity

        val result = repository.getGeneratedById(id)

        assertNotNull(result)
        assertEquals(id, result?.id)
        assertEquals("url", result?.templateId)
        coVerify { generatedCodeDao.getGeneratedById(id) }
    }

    @Test
    fun `getGeneratedById returns null when not found`() = runTest {
        val id = 999L
        coEvery { generatedCodeDao.getGeneratedById(id) } returns null

        val result = repository.getGeneratedById(id)

        assertNull(result)
        coVerify { generatedCodeDao.getGeneratedById(id) }
    }

    @Test
    fun `insertGenerated returns inserted id`() = runTest {
        val insertedId = 5L
        coEvery { generatedCodeDao.insert(any()) } returns insertedId

        val result = repository.insertGenerated(testDomain)

        assertEquals(insertedId, result)
        coVerify { generatedCodeDao.insert(any()) }
    }

    @Test
    fun `updateGenerated calls dao update`() = runTest {
        coEvery { generatedCodeDao.update(any()) } returns Unit

        repository.updateGenerated(testDomain)

        coVerify { generatedCodeDao.update(any()) }
    }

    @Test
    fun `deleteGenerated calls dao delete`() = runTest {
        coEvery { generatedCodeDao.delete(any()) } returns Unit

        repository.deleteGenerated(testDomain)

        coVerify { generatedCodeDao.delete(any()) }
    }

    @Test
    fun `deleteByIds calls dao deleteByIds`() = runTest {
        val ids = listOf(1L, 2L, 3L)
        coEvery { generatedCodeDao.deleteByIds(ids) } returns Unit

        repository.deleteByIds(ids)

        coVerify { generatedCodeDao.deleteByIds(ids) }
    }

    @Test
    fun `deleteAll calls dao deleteAll`() = runTest {
        coEvery { generatedCodeDao.deleteAll() } returns Unit

        repository.deleteAll()

        coVerify { generatedCodeDao.deleteAll() }
    }

    @Test
    fun `getCount returns correct count`() = runTest {
        val count = 10
        coEvery { generatedCodeDao.getCount() } returns count

        val result = repository.getCount()

        assertEquals(count, result)
        coVerify { generatedCodeDao.getCount() }
    }

    @Test
    fun `getAllGenerated handles multiple items`() = runTest {
        val entities = listOf(
            testEntity,
            testEntity.copy(id = 2L, title = "Second Item"),
            testEntity.copy(id = 3L, title = "Third Item")
        )
        coEvery { generatedCodeDao.getAllGenerated() } returns flowOf(entities)

        repository.getAllGenerated().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertEquals("My Website", result[0].title)
            assertEquals("Second Item", result[1].title)
            assertEquals("Third Item", result[2].title)
            awaitComplete()
        }
    }

    @Test
    fun `repository correctly maps entity fields to domain`() = runTest {
        coEvery { generatedCodeDao.getGeneratedById(1L) } returns testEntity

        val result = repository.getGeneratedById(1L)

        assertNotNull(result)
        assertEquals(BarcodeFormat.QR_CODE, result?.barcodeFormat)
        assertEquals(ErrorCorrectionLevel.HIGH, result?.errorCorrection)
        assertEquals(512, result?.size)
        assertEquals(4, result?.margin)
        assertEquals(0xFF000000.toInt(), result?.foregroundColor)
        assertEquals(0xFFFFFFFF.toInt(), result?.backgroundColor)
    }
}
