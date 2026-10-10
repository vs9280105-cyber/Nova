package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConversationHistoryDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.LauncherScreen
import com.example.ui.screens.MemoryVaultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.VoiceChatScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.VoidDark

enum class NovaNavDestination(
    val title: String,
    val icon: ImageVector,
    val tag: String
) {
    VOICE("Voice", Icons.Default.Mic, "nav_voice"),
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat, "nav_chat"),
    LAUNCHER("Launcher", Icons.Default.Apps, "nav_launcher"),
    TASKS("Tasks", Icons.Default.CheckCircle, "nav_tasks"),
    MEMORY("Memory", Icons.Default.Psychology, "nav_memory"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@Composable
fun NovaMainApp(
    viewModel: NovaViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(NovaNavDestination.VOICE) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("nova_main_app"),
        containerColor = VoidDark,
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurfaceDark,
                contentColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = Color(0xFF1B2942))
                    .testTag("bottom_navigation_bar")
            ) {
                NovaNavDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VoidDark,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedIconColor = Color(0xFF7E97B8),
                            unselectedTextColor = Color(0xFF7E97B8)
                        ),
                        modifier = Modifier.testTag(destination.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NovaNavDestination.VOICE -> {
                    VoiceChatScreen(viewModel = viewModel)
                }
                NovaNavDestination.CHAT -> {
                    ChatScreen(
                        viewModel = viewModel,
                        onOpenHistory = { showHistoryDialog = true }
                    )
                }
                NovaNavDestination.LAUNCHER -> {
                    LauncherScreen(
                        viewModel = viewModel,
                        onNavigateToVoice = { currentDestination = NovaNavDestination.VOICE }
                    )
                }
                NovaNavDestination.TASKS -> {
                    TasksScreen(viewModel = viewModel)
                }
                NovaNavDestination.MEMORY -> {
                    MemoryVaultScreen(viewModel = viewModel)
                }
                NovaNavDestination.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }

            if (showHistoryDialog) {
                ConversationHistoryDialog(
                    viewModel = viewModel,
                    onDismiss = { showHistoryDialog = false }
                )
            }
        }
    }
}
