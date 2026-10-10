package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfo
import com.example.system.DeviceAction
import com.example.ui.NovaViewModel
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMagenta
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.ui.theme.CyberTeal
import com.example.ui.theme.VoidDark

@Composable
fun LauncherScreen(
    viewModel: NovaViewModel,
    onNavigateToVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        installedApps = viewModel.actionDispatcher.getInstalledLauncherApps()
    }

    val filteredApps = if (searchQuery.isBlank()) {
        installedApps
    } else {
        installedApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("launcher_screen"),
        color = VoidDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Search & Voice Launch Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search apps or commands...", color = Color(0xFF6B829E), fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search apps",
                        tint = CyberCyan
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = Color(0xFF1E2F4C),
                    focusedContainerColor = CyberSurfaceDark,
                    unfocusedContainerColor = CyberSurfaceDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("launcher_search_bar")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section: System Quick Shortcuts
                item(span = { GridItemSpan(4) }) {
                    Text(
                        text = "SYSTEM CONTROLS & SHORTCUTS",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                item(span = { GridItemSpan(4) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickSystemTile(
                            title = "Wi-Fi",
                            icon = Icons.Default.Wifi,
                            color = CyberCyan,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val res = viewModel.actionDispatcher.executeAction(DeviceAction.OpenWifiSettings)
                                Toast.makeText(context, res.feedbackMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                        QuickSystemTile(
                            title = "Bluetooth",
                            icon = Icons.Default.Bluetooth,
                            color = CyberPurple,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val res = viewModel.actionDispatcher.executeAction(DeviceAction.OpenBluetoothSettings)
                                Toast.makeText(context, res.feedbackMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                        QuickSystemTile(
                            title = "Torch",
                            icon = Icons.Default.FlashlightOn,
                            color = CyberMagenta,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val res = viewModel.actionDispatcher.executeAction(DeviceAction.ToggleFlashlight)
                                Toast.makeText(context, res.feedbackMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                        QuickSystemTile(
                            title = "Battery",
                            icon = Icons.Default.BatteryStd,
                            color = CyberTeal,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val res = viewModel.actionDispatcher.executeAction(DeviceAction.CheckBattery)
                                Toast.makeText(context, res.feedbackMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // Section: Primary Applications Grid
                item(span = { GridItemSpan(4) }) {
                    Text(
                        text = "PRIMARY APPS",
                        color = CyberTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }

                item {
                    AppTile(
                        title = "YouTube",
                        iconVector = Icons.Default.Apps,
                        accentColor = Color(0xFFFF0033),
                        onClick = { viewModel.actionDispatcher.executeAction(DeviceAction.OpenYouTube) }
                    )
                }
                item {
                    AppTile(
                        title = "Chrome",
                        iconVector = Icons.Default.Search,
                        accentColor = Color(0xFF4285F4),
                        onClick = { viewModel.actionDispatcher.executeAction(DeviceAction.OpenChrome) }
                    )
                }
                item {
                    AppTile(
                        title = "Maps",
                        iconVector = Icons.Default.Map,
                        accentColor = Color(0xFF34A853),
                        onClick = { viewModel.actionDispatcher.executeAction(DeviceAction.OpenMaps) }
                    )
                }
                item {
                    AppTile(
                        title = "Camera",
                        iconVector = Icons.Default.CameraAlt,
                        accentColor = CyberCyan,
                        onClick = { viewModel.actionDispatcher.executeAction(DeviceAction.OpenCamera) }
                    )
                }
                item {
                    AppTile(
                        title = "Calculator",
                        iconVector = Icons.Default.Calculate,
                        accentColor = CyberTeal,
                        onClick = { viewModel.actionDispatcher.executeAction(DeviceAction.OpenCalculator) }
                    )
                }
                item {
                    AppTile(
                        title = "Settings",
                        iconVector = Icons.Default.Settings,
                        accentColor = Color(0xFF9AA0A6),
                        onClick = { viewModel.actionDispatcher.executeAction(DeviceAction.OpenGeneralSettings) }
                    )
                }
                item {
                    AppTile(
                        title = "Display",
                        iconVector = Icons.Default.BrightnessMedium,
                        accentColor = Color(0xFFFFB703),
                        onClick = { viewModel.actionDispatcher.executeAction(DeviceAction.OpenDisplaySettings) }
                    )
                }
                item {
                    AppTile(
                        title = "Volume",
                        iconVector = Icons.AutoMirrored.Filled.VolumeUp,
                        accentColor = CyberPurple,
                        onClick = { viewModel.actionDispatcher.executeAction(DeviceAction.OpenSoundSettings) }
                    )
                }

                // Section: Installed Device Applications
                if (filteredApps.isNotEmpty()) {
                    item(span = { GridItemSpan(4) }) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "SEARCH RESULTS (${filteredApps.size})" else "ALL INSTALLED APPS (${filteredApps.size})",
                            color = Color(0xFF8FA7C4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    items(filteredApps, key = { it.packageName }) { app ->
                        InstalledAppTile(
                            app = app,
                            onClick = {
                                val res = viewModel.actionDispatcher.executeAction(
                                    DeviceAction.LaunchPackage(app.packageName, app.appName)
                                )
                                Toast.makeText(context, res.feedbackMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickSystemTile(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberSurfaceDark)
            .border(1.dp, Color(0xFF1E2E49), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = Color(0xFFDFEAF7),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AppTile(
    title: String,
    iconVector: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(listOf(CyberSurfaceVariantDark, CyberSurfaceDark))
                )
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = Color(0xFFD4E3F5),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun InstalledAppTile(
    app: AppInfo,
    onClick: () -> Unit
) {
    val bitmap = remember(app.icon) {
        app.icon?.let { drawableToBitmap(it) }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSurfaceDark)
                .border(1.dp, Color(0xFF1D2F4D), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = app.appName,
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.appName,
            color = Color(0xFFCADCF2),
            fontSize = 10.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }
    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 72
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 72
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
