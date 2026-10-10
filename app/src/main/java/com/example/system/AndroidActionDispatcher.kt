package com.example.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.Settings
import com.example.model.AppInfo

class AndroidActionDispatcher(private val context: Context) {

    private var isTorchOn = false

    /**
     * Parses user command and detects if it matches an approved system action.
     * Guaranteed safe via strict allowlist pattern matching.
     */
    fun detectActionFromQuery(query: String): DeviceAction? {
        val lower = query.trim().lowercase()

        return when {
            lower.contains("open youtube") || lower.contains("launch youtube") -> DeviceAction.OpenYouTube
            lower.contains("open chrome") || lower.contains("launch chrome") || lower.contains("open browser") -> DeviceAction.OpenChrome
            lower.contains("open maps") || lower.contains("launch maps") || lower.contains("google maps") -> DeviceAction.OpenMaps
            lower.contains("open camera") || lower.contains("take photo") || lower.contains("launch camera") -> DeviceAction.OpenCamera
            lower.contains("open calculator") || lower.contains("calculator") -> DeviceAction.OpenCalculator
            lower.contains("open clock") || lower.contains("set alarm") || lower.contains("open alarm") -> DeviceAction.OpenClock
            lower.contains("open whatsapp") || lower.contains("launch whatsapp") -> DeviceAction.OpenWhatsApp
            lower.contains("open play store") || lower.contains("open playstore") -> DeviceAction.OpenPlayStore
            lower.contains("open dialer") || lower.contains("open phone") || lower.contains("make a call") -> DeviceAction.OpenDialer

            lower.contains("wifi setting") || lower.contains("wi-fi setting") || lower.contains("turn on wifi") || lower.contains("open wifi") -> DeviceAction.OpenWifiSettings
            lower.contains("bluetooth setting") || lower.contains("open bluetooth") || lower.contains("turn on bluetooth") -> DeviceAction.OpenBluetoothSettings
            lower.contains("display setting") || lower.contains("brightness") || lower.contains("screen setting") -> DeviceAction.OpenDisplaySettings
            lower.contains("sound setting") || lower.contains("volume setting") || lower.contains("audio setting") -> DeviceAction.OpenSoundSettings
            lower.contains("app setting") || lower.contains("application setting") || lower.contains("manage apps") -> DeviceAction.OpenAppSettings
            lower.contains("accessibility") -> DeviceAction.OpenAccessibilitySettings
            lower.contains("open setting") || lower.contains("phone setting") || lower.contains("system setting") -> DeviceAction.OpenGeneralSettings

            lower.contains("flashlight") || lower.contains("torch") -> DeviceAction.ToggleFlashlight
            lower.contains("battery") -> DeviceAction.CheckBattery

            lower.startsWith("search google for") || lower.startsWith("google ") -> {
                val term = lower.replace("search google for", "").replace("google", "").trim()
                if (term.isNotEmpty()) DeviceAction.SearchWeb(term) else null
            }
            else -> null
        }
    }

    /**
     * Executes the validated action against system APIs and Intents.
     */
    fun executeAction(action: DeviceAction): ActionResult {
        return try {
            when (action) {
                is DeviceAction.OpenYouTube -> launchAppOrUrl("com.google.android.youtube", "https://www.youtube.com", "YouTube")
                is DeviceAction.OpenChrome -> launchAppOrUrl("com.android.chrome", "https://www.google.com", "Chrome")
                is DeviceAction.OpenMaps -> launchAppOrUrl("com.google.android.apps.maps", "https://maps.google.com", "Google Maps")
                is DeviceAction.OpenCamera -> {
                    val intent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        ActionResult(true, "Launching Camera...")
                    } else {
                        ActionResult(false, "Camera app is not available on this device.")
                    }
                }
                is DeviceAction.OpenCalculator -> launchAppOrIntent("com.google.android.calculator", "Calculator", Intent.CATEGORY_APP_CALCULATOR)
                is DeviceAction.OpenClock -> {
                    val clockIntent = Intent(Settings.ACTION_DATE_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(clockIntent)
                    ActionResult(true, "Opening Clock and Time settings...")
                }
                is DeviceAction.OpenWhatsApp -> launchPackageDirect("com.whatsapp", "WhatsApp")
                is DeviceAction.OpenPlayStore -> launchPackageDirect("com.android.vending", "Google Play Store")
                is DeviceAction.OpenDialer -> {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(dialIntent)
                    ActionResult(true, "Opening Phone dialer...")
                }
                is DeviceAction.LaunchPackage -> launchPackageDirect(action.packageName, action.appLabel)

                is DeviceAction.OpenWifiSettings -> openSystemSettings(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings")
                is DeviceAction.OpenBluetoothSettings -> openSystemSettings(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings")
                is DeviceAction.OpenDisplaySettings -> openSystemSettings(Settings.ACTION_DISPLAY_SETTINGS, "Display & Brightness Settings")
                is DeviceAction.OpenSoundSettings -> openSystemSettings(Settings.ACTION_SOUND_SETTINGS, "Sound & Volume Settings")
                is DeviceAction.OpenAppSettings -> openSystemSettings(Settings.ACTION_APPLICATION_SETTINGS, "App Management Settings")
                is DeviceAction.OpenAccessibilitySettings -> openSystemSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility Settings")
                is DeviceAction.OpenGeneralSettings -> openSystemSettings(Settings.ACTION_SETTINGS, "System Settings")

                is DeviceAction.ToggleFlashlight -> toggleTorch()
                is DeviceAction.CheckBattery -> checkBatteryLevel()
                is DeviceAction.SearchWeb -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(action.query)}")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Searching the web for \"${action.query}\"...")
                }
            }
        } catch (e: Exception) {
            ActionResult(false, "Unable to execute ${action.description}: ${e.localizedMessage}")
        }
    }

    private fun launchPackageDirect(packageName: String, label: String): ActionResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult(true, "Opening $label...")
        } else {
            ActionResult(false, "$label is not installed on this device.")
        }
    }

    private fun launchAppOrUrl(packageName: String, fallbackUrl: String, label: String): ActionResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult(true, "Launching $label...")
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
            ActionResult(true, "$label app not detected. Opening in browser...")
        }
    }

    private fun launchAppOrIntent(packageName: String, label: String, category: String): ActionResult {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            ActionResult(true, "Opening $label...")
        } else {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(category)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionResult(true, "Opening $label...")
            } else {
                ActionResult(false, "$label app is not installed.")
            }
        }
    }

    private fun openSystemSettings(action: String, title: String): ActionResult {
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return ActionResult(true, "Navigating to $title...")
    }

    private fun toggleTorch(): ActionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraManager != null && cameraId != null) {
                isTorchOn = !isTorchOn
                cameraManager.setTorchMode(cameraId, isTorchOn)
                val status = if (isTorchOn) "Flashlight turned ON" else "Flashlight turned OFF"
                ActionResult(true, status)
            } else {
                ActionResult(false, "Flashlight hardware not available.")
            }
        } catch (e: Exception) {
            ActionResult(false, "Cannot toggle flashlight: ${e.localizedMessage}")
        }
    }

    private fun checkBatteryLevel(): ActionResult {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryPct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        return if (batteryPct >= 0) {
            ActionResult(true, "Current battery level is $batteryPct%.")
        } else {
            ActionResult(false, "Battery status currently unavailable.")
        }
    }

    /**
     * Lists user launchable apps for Launcher Hub.
     */
    fun getInstalledLauncherApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return resolveInfos.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            // Exclude our own app from the launcher list
            if (packageName == context.packageName) null
            else {
                val appName = resolveInfo.loadLabel(pm).toString()
                val icon = resolveInfo.loadIcon(pm)
                AppInfo(packageName, appName, icon)
            }
        }.sortedBy { it.appName.lowercase() }
    }
}
