package com.rejown.qrcraft.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.rejown.qrcraft.data.local.preferences.ThemePreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * Utility class for providing haptic feedback throughout the app
 */
class HapticFeedback(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val themePreferences by lazy { ThemePreferences(context) }

    private fun isEnabled(): Boolean {
        return runBlocking {
            themePreferences.isHapticFeedbackEnabled().first()
        }
    }

    /**
     * Light click feedback - for button presses, chip selections
     */
    fun lightClick(view: View? = null) {
        if (!isEnabled()) return
        view?.performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        ) ?: vibrate(10)
    }

    /**
     * Medium click feedback - for switches, toggles
     */
    fun mediumClick(view: View? = null) {
        if (!isEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(
                HapticFeedbackConstants.CONFIRM,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            ) ?: vibrate(20)
        } else {
            vibrate(20)
        }
    }

    /**
     * Strong impact - for long press, delete confirmations
     */
    fun strongImpact(view: View? = null) {
        if (!isEnabled()) return
        view?.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        ) ?: vibrate(50)
    }

    /**
     * Success feedback - short single pulse for successful operations
     * Used for: scan success, save success, generate success
     */
    fun success() {
        if (!isEnabled()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vibrator?.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to provide success haptic feedback")
        }
    }

    /**
     * Error feedback - double pulse pattern for errors
     * Used for: scan errors, generation errors, validation errors
     */
    fun error() {
        if (!isEnabled()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 50, 50)
                val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 50, 50, 50), -1)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to provide error haptic feedback")
        }
    }

    /**
     * Selection feedback - for item selection in lists
     * Used for: history item selection, multi-select mode
     */
    fun selection(view: View? = null) {
        if (!isEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.performHapticFeedback(
                HapticFeedbackConstants.GESTURE_START,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            ) ?: vibrate(15)
        } else {
            vibrate(15)
        }
    }

    /**
     * Reject feedback - for invalid actions
     * Used for: validation errors, unavailable features
     */
    fun reject() {
        if (!isEnabled()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vibrator?.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to provide reject haptic feedback")
        }
    }

    /**
     * Generic vibration helper
     */
    private fun vibrate(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to vibrate")
        }
    }
}

/**
 * Composable function to remember HapticFeedback instance
 */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val context = LocalContext.current
    return remember(context) {
        HapticFeedback(context)
    }
}

/**
 * Extension function to get haptic feedback helper from View
 */
fun View.hapticFeedback(): HapticFeedback {
    return HapticFeedback(context)
}
