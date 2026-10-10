package com.example.system

sealed class DeviceAction(val description: String) {
    // App Launchers
    data object OpenYouTube : DeviceAction("Open YouTube")
    data object OpenChrome : DeviceAction("Open Chrome Browser")
    data object OpenMaps : DeviceAction("Open Google Maps")
    data object OpenCamera : DeviceAction("Open Camera")
    data object OpenCalculator : DeviceAction("Open Calculator")
    data object OpenClock : DeviceAction("Open Clock & Alarms")
    data object OpenWhatsApp : DeviceAction("Open WhatsApp")
    data object OpenPlayStore : DeviceAction("Open Google Play Store")
    data object OpenDialer : DeviceAction("Open Phone Dialer")
    data class LaunchPackage(val packageName: String, val appLabel: String) : DeviceAction("Open $appLabel")

    // System Settings Screens
    data object OpenWifiSettings : DeviceAction("Open Wi-Fi Settings")
    data object OpenBluetoothSettings : DeviceAction("Open Bluetooth Settings")
    data object OpenDisplaySettings : DeviceAction("Open Display Settings")
    data object OpenSoundSettings : DeviceAction("Open Sound Settings")
    data object OpenAppSettings : DeviceAction("Open App Settings")
    data object OpenAccessibilitySettings : DeviceAction("Open Accessibility")
    data object OpenGeneralSettings : DeviceAction("Open System Settings")

    // Hardware Actions
    data object ToggleFlashlight : DeviceAction("Toggle Flashlight")
    data object CheckBattery : DeviceAction("Check Battery Level")

    // Web & External
    data class SearchWeb(val query: String) : DeviceAction("Search the Web for '$query'")
}

data class ActionResult(
    val success: Boolean,
    val feedbackMessage: String
)
