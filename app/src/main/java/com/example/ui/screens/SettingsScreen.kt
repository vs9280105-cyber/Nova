package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.network.GeminiClient
import com.example.ui.NovaViewModel
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.VoidDark
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customKey by viewModel.customApiKey.collectAsState()
    val isSearchGrounding by viewModel.isSearchGroundingEnabled.collectAsState()
    val isAutoTts by viewModel.isAutoTtsEnabled.collectAsState()
    val isContinuousVoice by viewModel.isContinuousVoiceEnabled.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    var speechRate by remember { mutableFloatStateOf(1.0f) }
    val effectiveKey = GeminiClient.getEffectiveApiKey(customKey)
    val hasValidKey = effectiveKey.isNotBlank() && effectiveKey != "MY_GEMINI_API_KEY"

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        color = VoidDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SETTINGS & SYSTEM CONFIG",
                color = CyberCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Section 1: AI Provider & API Key Status
            SettingsCard(title = "AI Brain Configuration") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasValidKey) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (hasValidKey) CyberTeal else CyberMagenta,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasValidKey) "Gemini API Key Detected" else "API Key Not Set",
                        color = if (hasValidKey) CyberTeal else CyberMagenta,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configured via BuildConfig & Secrets Panel. You can also specify an optional custom API key override below:",
                    color = Color(0xFF8FA7C4),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = customKey,
                    onValueChange = { viewModel.customApiKey.value = it },
                    label = { Text("Custom API Key Override (Optional)", color = Color(0xFF8FA7C4)) },
                    placeholder = { Text("AIzaSy...", color = Color(0xFF4A5C72)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("custom_api_key_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Active AI Model: $selectedModel",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Section 2: Speech & TTS Settings
            SettingsCard(title = "Voice Synthesis & Speech") {
                Text(
                    text = "Speech Rate: ${"%.1f".format(speechRate)}x",
                    color = Color(0xFFDFEAF7),
                    fontSize = 13.sp
                )
                Slider(
                    value = speechRate,
                    onValueChange = {
                        speechRate = it
                        viewModel.ttsManager.setSpeechRate(it)
                    },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = CyberCyan,
                        inactiveTrackColor = Color(0xFF1E2F4C)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("speech_rate_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            viewModel.ttsManager.speak("Testing speech synthesis on Nova AI assistant.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = VoidDark),
                        modifier = Modifier.testTag("test_voice_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Voice", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.ttsManager.setLanguage(Locale.forLanguageTag("hi-IN"))
                            viewModel.ttsManager.speak("नमस्ते! मैं नोवा हूँ, आपकी व्यक्तिगत एआई सहायक।")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = VoidDark)
                    ) {
                        Text("Test Hindi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Section 3: Feature Toggles
            SettingsCard(title = "Feature Preferences") {
                ToggleRow(
                    title = "Automatic Voice Readout",
                    subtitle = "Read AI answers aloud using TextToSpeech",
                    checked = isAutoTts,
                    onCheckedChange = { viewModel.isAutoTtsEnabled.value = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ToggleRow(
                    title = "Continuous Voice Dialogue Loop",
                    subtitle = "Reactivate listening automatically after Nova speaks",
                    checked = isContinuousVoice,
                    onCheckedChange = { viewModel.isContinuousVoiceEnabled.value = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ToggleRow(
                    title = "Google Search Grounding",
                    subtitle = "Fetch live web information and display clickable citations",
                    checked = isSearchGrounding,
                    onCheckedChange = { viewModel.isSearchGroundingEnabled.value = it }
                )
            }

            // Section 4: Privacy & Security
            SettingsCard(title = "Privacy & Security Guarantee") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Zero-Cloud Tracking & Local SQLite Database", color = CyberCyan, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• All chat history, tasks, and memory entries are strictly stored on your device in local Room database.\n• System actions (launching apps, toggling flashlight, opening settings) use secure allowlists with explicit intent validation.\n• Zero arbitrary code execution.\n• Microphone is only accessed when you tap the mic button.",
                    color = Color(0xFF8FA7C4),
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp
                )
            }

            // Section 5: About Screen
            SettingsCard(title = "About NOVA AI") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NOVA AI — Personal Assistant & Launcher", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Target Platform: Android 13 (API 33+)\nUI Engine: Jetpack Compose + Material 3\nCompanion: 3D Holographic Cyber Anime Avatar\nVersion: 1.0.0", color = Color(0xFF8FA7C4), fontSize = 11.5.sp, lineHeight = 17.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurfaceDark)
            .border(1.dp, Color(0xFF1E2E49), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title.uppercase(),
            color = CyberTeal,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color(0xFFE2E8F0), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = Color(0xFF6B829E), fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VoidDark,
                checkedTrackColor = CyberCyan,
                uncheckedThumbColor = Color(0xFF8FA7C4),
                uncheckedTrackColor = Color(0xFF162035)
            )
        )
    }
}
