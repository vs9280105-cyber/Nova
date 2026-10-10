package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.NovaState
import com.example.ui.NovaViewModel
import com.example.ui.components.NovaAvatar3D
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.VoidDark

@Composable
fun VoiceChatScreen(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val novaState by viewModel.novaState.collectAsState()
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val partialText by viewModel.speechManager.partialText.collectAsState()
    val isContinuousVoice by viewModel.isContinuousVoiceEnabled.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val lastUserMessage = messages.lastOrNull { it.role == "user" }
    val lastAssistantMessage = messages.lastOrNull { it.role == "assistant" }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startVoiceListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for Voice Assistant", Toast.LENGTH_LONG).show()
        }
    }

    val quickCommands = listOf(
        "Open YouTube",
        "Open Chrome",
        "Turn on Flashlight",
        "Check Battery",
        "Wi-Fi Settings",
        "Explain Quantum Computing in Hindi",
        "Help me write a Python script"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("voice_chat_screen"),
        color = VoidDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Controls Bar (Continuous Loop toggle & TTS stop)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = isContinuousVoice,
                    onClick = { viewModel.isContinuousVoiceEnabled.value = !isContinuousVoice },
                    label = {
                        Text(
                            text = if (isContinuousVoice) "Continuous Dialogue ON" else "Continuous Dialogue OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Continuous Mode",
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CyberCyan,
                        selectedLeadingIconColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("continuous_voice_toggle")
                )

                if (novaState == NovaState.SPEAKING) {
                    IconButton(
                        onClick = { viewModel.stopSpeaking() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CyberMagenta.copy(alpha = 0.2f))
                            .testTag("stop_speaking_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Mute Nova",
                            tint = CyberMagenta
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3D Avatar Centerpiece (Full body anime cyber model)
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                NovaAvatar3D(
                    state = novaState,
                    size = 250.dp,
                    compact = false,
                    onClick = {
                        if (novaState == NovaState.SPEAKING) {
                            viewModel.stopSpeaking()
                        } else {
                            viewModel.speakText("Hi, I am Nova! How can I assist you right now?")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live Transcript Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberSurfaceDark)
                    .border(1.dp, Color(0xFF1D2E49), RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("transcript_card")
            ) {
                // User Transcript
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "YOU:",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isListening && partialText.isNotEmpty()) partialText
                        else lastUserMessage?.content ?: "Say a command or tap the microphone...",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.5.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Nova Response
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "NOVA:",
                        color = CyberTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (novaState) {
                            NovaState.THINKING -> "Analyzing neural circuits & processing..."
                            NovaState.LISTENING -> "Listening to your voice..."
                            else -> lastAssistantMessage?.content ?: "Awaiting your command, Commander."
                        },
                        color = Color(0xFFBFD3EC),
                        fontSize = 13.sp,
                        maxLines = 4,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Voice Command Chips (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickCommands.forEach { cmd ->
                    SuggestionChip(
                        onClick = { viewModel.sendMessage(cmd) },
                        label = { Text(cmd, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = CyberSurfaceVariantDark,
                            labelColor = Color(0xFFB4CDED)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color(0xFF203556)
                        ),
                        modifier = Modifier.testTag("quick_command_$cmd")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Giant Glowing Microphone Action Button
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isListening) {
                                viewModel.stopVoiceListening()
                                if (partialText.isNotBlank()) {
                                    viewModel.sendMessage(partialText)
                                }
                            } else {
                                viewModel.startVoiceListening()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(
                            elevation = if (isListening) 24.dp else 12.dp,
                            shape = CircleShape,
                            spotColor = if (isListening) CyberMagenta else CyberCyan
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isListening) listOf(CyberMagenta, Color(0xFFC70039))
                                else listOf(CyberCyan, CyberTeal)
                            )
                        )
                        .border(
                            2.dp,
                            if (isListening) CyberMagenta else Color.White,
                            CircleShape
                        )
                        .testTag("voice_fab")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice input toggle",
                        tint = VoidDark,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = if (isListening) "Tap to Finish & Submit" else "Tap Microphone to Speak",
                color = if (isListening) CyberMagenta else Color(0xFF8FA7C4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
