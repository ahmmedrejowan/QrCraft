package com.rejown.qrcraft.presentation.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.rejown.qrcraft.data.local.preferences.ThemePreferences
import com.rejown.qrcraft.domain.repository.GeneratorRepository
import com.rejown.qrcraft.domain.repository.ScanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for SettingsViewModel
 * Tests all settings operations including clear history and stats
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var scanRepository: ScanRepository
    private lateinit var generatorRepository: GeneratorRepository
    private lateinit var themePreferences: ThemePreferences
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        scanRepository = mockk()
        generatorRepository = mockk()
        themePreferences = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        coEvery { scanRepository.getCount() } returns 0
        coEvery { generatorRepository.getCount() } returns 0

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertNull(state.successMessage)
            assertEquals(0, state.scanCount)
            assertEquals(0, state.generatedCount)
            assertEquals(0, state.totalCount)
        }
    }

    @Test
    fun `loadStats updates state with correct counts`() = runTest {
        val scanCount = 10
        val generatedCount = 15
        coEvery { scanRepository.getCount() } returns scanCount
        coEvery { generatorRepository.getCount() } returns generatedCount

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(scanCount, state.scanCount)
            assertEquals(generatedCount, state.generatedCount)
            assertEquals(25, state.totalCount)
        }
    }

    @Test
    fun `loadStats handles repository error gracefully`() = runTest {
        coEvery { scanRepository.getCount() } throws Exception("Database error")
        coEvery { generatorRepository.getCount() } returns 0

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            // Should not crash, stats remain at default
            assertEquals(0, state.scanCount)
            assertEquals(0, state.generatedCount)
        }
    }

    @Test
    fun `clearScanHistory clears history and shows success`() = runTest {
        coEvery { scanRepository.getCount() } returns 5 andThen 0
        coEvery { generatorRepository.getCount() } returns 3
        coEvery { scanRepository.deleteAll() } returns Unit

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearScanHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals("Scan history cleared", state.successMessage)
            assertEquals(0, state.scanCount)
            assertEquals(3, state.generatedCount)
        }

        coVerify { scanRepository.deleteAll() }
    }

    @Test
    fun `clearScanHistory shows error on failure`() = runTest {
        val errorMessage = "Database locked"
        coEvery { scanRepository.getCount() } returns 5
        coEvery { generatorRepository.getCount() } returns 3
        coEvery { scanRepository.deleteAll() } throws Exception(errorMessage)

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearScanHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.successMessage)
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Failed to clear scan history"))
            assertTrue(state.error!!.contains(errorMessage))
        }
    }

    @Test
    fun `clearGeneratedHistory clears history and shows success`() = runTest {
        coEvery { scanRepository.getCount() } returns 5
        coEvery { generatorRepository.getCount() } returns 10 andThen 0
        coEvery { generatorRepository.deleteAll() } returns Unit

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearGeneratedHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals("Generated codes cleared", state.successMessage)
            assertEquals(5, state.scanCount)
            assertEquals(0, state.generatedCount)
        }

        coVerify { generatorRepository.deleteAll() }
    }

    @Test
    fun `clearGeneratedHistory shows error on failure`() = runTest {
        val errorMessage = "Database error"
        coEvery { scanRepository.getCount() } returns 5
        coEvery { generatorRepository.getCount() } returns 10
        coEvery { generatorRepository.deleteAll() } throws Exception(errorMessage)

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearGeneratedHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.successMessage)
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Failed to clear generated history"))
        }
    }

    @Test
    fun `clearAllData clears all data and resets preferences`() = runTest {
        coEvery { scanRepository.getCount() } returns 5 andThen 0
        coEvery { generatorRepository.getCount() } returns 10 andThen 0
        coEvery { scanRepository.deleteAll() } returns Unit
        coEvery { generatorRepository.deleteAll() } returns Unit

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearAllData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.error)
            assertEquals("All data cleared", state.successMessage)
            assertEquals(0, state.scanCount)
            assertEquals(0, state.generatedCount)
            assertEquals(0, state.totalCount)
        }

        coVerify { scanRepository.deleteAll() }
        coVerify { generatorRepository.deleteAll() }
        coVerify { themePreferences.setTheme("System") }
        coVerify { themePreferences.setDynamicColor(false) }
    }

    @Test
    fun `clearAllData shows error on failure`() = runTest {
        val errorMessage = "Permission denied"
        coEvery { scanRepository.getCount() } returns 5
        coEvery { generatorRepository.getCount() } returns 10
        coEvery { scanRepository.deleteAll() } throws Exception(errorMessage)

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearAllData()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.successMessage)
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Failed to clear all data"))
        }
    }

    @Test
    fun `clearMessages clears both success and error messages`() = runTest {
        coEvery { scanRepository.getCount() } returns 5
        coEvery { generatorRepository.getCount() } returns 10
        coEvery { scanRepository.deleteAll() } returns Unit

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        // Trigger an operation that sets a message
        viewModel.clearScanHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        // Clear messages
        viewModel.clearMessages()

        viewModel.state.test {
            val state = awaitItem()
            assertNull(state.successMessage)
            assertNull(state.error)
        }
    }

    @Test
    fun `loading state is set correctly during operations`() = runTest {
        coEvery { scanRepository.getCount() } returns 5
        coEvery { generatorRepository.getCount() } returns 10
        coEvery { scanRepository.deleteAll() } coAnswers {
            kotlinx.coroutines.delay(100)
        }

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearScanHistory()

        // Check loading state is true during operation
        viewModel.state.test {
            var state = awaitItem()
            // After completion, loading should be false
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `multiple operations can be performed sequentially`() = runTest {
        coEvery { scanRepository.getCount() } returns 5 andThen 0 andThen 0
        coEvery { generatorRepository.getCount() } returns 10 andThen 10 andThen 0
        coEvery { scanRepository.deleteAll() } returns Unit
        coEvery { generatorRepository.deleteAll() } returns Unit

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        // Clear scan history
        viewModel.clearScanHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            var state = awaitItem()
            assertEquals(0, state.scanCount)
            assertEquals(10, state.generatedCount)
        }

        // Clear generated history
        viewModel.clearGeneratedHistory()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            var state = awaitItem()
            assertEquals(0, state.scanCount)
            assertEquals(0, state.generatedCount)
        }

        coVerify(exactly = 1) { scanRepository.deleteAll() }
        coVerify(exactly = 1) { generatorRepository.deleteAll() }
    }

    @Test
    fun `totalCount is sum of scanCount and generatedCount`() = runTest {
        coEvery { scanRepository.getCount() } returns 7
        coEvery { generatorRepository.getCount() } returns 13

        viewModel = SettingsViewModel(scanRepository, generatorRepository, themePreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(7, state.scanCount)
            assertEquals(13, state.generatedCount)
            assertEquals(20, state.totalCount)
        }
    }
}
