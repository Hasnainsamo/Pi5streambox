package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ActivityLog
import com.example.data.RfidTag
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PiStreamerDashboard(
    viewModel: PiStreamerViewModel,
    modifier: Modifier = Modifier
) {
    // Collect States
    val piConnected by viewModel.piConnected.collectAsStateWithLifecycle()
    val cpuTemp by viewModel.cpuTemp.collectAsStateWithLifecycle()
    val cpuLoad by viewModel.cpuLoad.collectAsStateWithLifecycle()
    val ramUsage by viewModel.ramUsage.collectAsStateWithLifecycle()
    val wifiSsid by viewModel.wifiSsid.collectAsStateWithLifecycle()
    val ipAddress by viewModel.ipAddress.collectAsStateWithLifecycle()

    val activePlayingTag by viewModel.activePlayingTag.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val volume by viewModel.volume.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSecondsRemaining.collectAsStateWithLifecycle()
    val currentPosMs by viewModel.currentPositionMs.collectAsStateWithLifecycle()
    val totalDurationMs by viewModel.totalDurationMs.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val outputRouting by viewModel.outputRouting.collectAsStateWithLifecycle()
    val receivingDevice by viewModel.receivingDevice.collectAsStateWithLifecycle()
    val accessDeniedTag by viewModel.accessDeniedTag.collectAsStateWithLifecycle()
    val lastCrashInfo by viewModel.lastCrashInfo.collectAsStateWithLifecycle()

    val isWritingMode by viewModel.isWritingMode.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()

    // Screen Tabs: "Active Remote", "RFID Manager", "WiFi Setup"
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Media Remote", "RFID Database", "Wi-Fi Config")

    // Custom background gradient for the Sleek Interface
    val bgGradient = Brush.verticalGradient(
        colors = listOf(SleekBackground, SleekBackground)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = SleekSecondary,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeveloperBoard,
                                    contentDescription = "App Logo",
                                    tint = SleekDeepDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "PiStream Node 5",
                                fontWeight = FontWeight.Bold,
                                color = SleekText,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (piConnected) Color(0xFF2E7D32) else Color(0xFFC62828))
                                )
                                Text(
                                    text = if (piConnected) "Online • HDMI Out" else "Offline",
                                    fontSize = 11.sp,
                                    color = SleekTextMedium
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Connection Clickable Badge
                    Surface(
                        onClick = { viewModel.togglePiConnection() },
                        shape = RoundedCornerShape(12.dp),
                        color = if (piConnected) Color(0xFFE2F6EA) else Color(0xFFFFEBEE),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .border(
                                width = 1.dp,
                                color = if (piConnected) Color(0xFF81C784) else Color(0xFFEF9A9A),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (piConnected) Color(0xFF2E7D32) else Color(0xFFC62828))
                            )
                            Text(
                                text = if (piConnected) "Connected" else "Reconnect",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (piConnected) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekBackground,
                    titleContentColor = SleekText
                )
            )
        },
        containerColor = SleekBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgGradient)
        ) {
            // Live hardware monitoring strip
            if (piConnected) {
                HardwareMonitorStrip(
                    cpuTemp = cpuTemp,
                    cpuLoad = cpuLoad,
                    ramUsage = ramUsage,
                    wifiSsid = wifiSsid,
                    ipAddress = ipAddress
                )
            } else {
                OfflineWarningStrip()
            }

            // Tabs Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SleekTabBg,
                contentColor = SleekPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SleekPrimary
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) SleekText else SleekTextMedium,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Screen Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Access Denied Alert banner overlay if present
                accessDeniedTag?.let { tagInfo ->
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Access",
                                tint = Color(0xFFC62828)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ACCESS SYSTEM LOCKED",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Chip authorization failed: $tagInfo is currently restricted from streaming 4K URL content.",
                                    color = SleekTextMedium,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(onClick = { viewModel.clearAccessDenied() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Warning",
                                    tint = SleekTextMedium
                                )
                            }
                        }
                    }
                }

                // App Safe Mode Recovery Dialog popup
                lastCrashInfo?.let { exceptionTrace ->
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissCrashReport() },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "System Error Detail",
                                    tint = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = "System Recovery Report",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 18.sp
                                )
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "An unexpected exception was intercepted and handled during the last boot cycle:",
                                    fontSize = 13.sp,
                                    color = SleekText
                                )
                                Surface(
                                    color = SleekDeepDark,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .border(1.dp, SleekTabBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Box(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = exceptionTrace,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = SleekProgressActive,
                                            modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                                        )
                                    }
                                }
                                Text(
                                    text = "All pipelines have been fully reinforced. State continuity preserved.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SleekTextMedium
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.dismissCrashReport() },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                            ) {
                                Text("Acknowledge & Clean", color = Color.White)
                            }
                        },
                        containerColor = SleekBackground,
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Selected Tab Screens
                when (selectedTab) {
                    0 -> RemoteControlTab(
                        viewModel = viewModel,
                        piConnected = piConnected,
                        activeTag = activePlayingTag,
                        isPlaying = isPlaying,
                        vol = volume,
                        timerSec = timerSeconds,
                        currentPos = currentPosMs,
                        totalDuration = totalDurationMs,
                        speed = playbackSpeed,
                        outRoute = outputRouting,
                        device = receivingDevice,
                        tagsList = allTags
                    )
                    1 -> RfidDatabaseTab(
                        viewModel = viewModel,
                        tagsList = allTags,
                        isWritingMode = isWritingMode
                    )
                    2 -> WifiConfigTab(
                        viewModel = viewModel,
                        currentSsid = wifiSsid,
                        ipAddress = ipAddress
                    )
                }
            }

            // Bottom Real-time Logs Console Widget
            TerminalLogsConsole(
                logs = allLogs,
                onClearLogs = { viewModel.clearAllLogs() }
            )
        }
    }
}

// ------------------------------------------------------------------------
// HARDWARE STRIPS & TELEMETRY
// ------------------------------------------------------------------------

@Composable
fun HardwareMonitorStrip(
    cpuTemp: Double,
    cpuLoad: Double,
    ramUsage: String,
    wifiSsid: String,
    ipAddress: String
) {
    Surface(
        color = SleekTabBg,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = (0.5).dp, color = SleekTabBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CPU Temp
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CPU TEMP", color = SleekTextMedium, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Temp icon",
                        tint = if (cpuTemp > 50.0) SleekPrimary else Color(0xFF2E7D32),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = String.format("%.1f°C", cpuTemp),
                        color = if (cpuTemp > 50.0) SleekPrimary else Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // CPU Load
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CPU LOAD", color = SleekTextMedium, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Load icon",
                        tint = SleekPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = String.format("%.1f%%", cpuLoad),
                        color = SleekText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // RAM
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("RAM USAGE", color = SleekTextMedium, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = ramUsage,
                    color = SleekText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // IP & Network
            Column(horizontalAlignment = Alignment.End) {
                Text("RASPBERRY IP", color = SleekTextMedium, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wifi Network Indicator",
                        tint = SleekPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = ipAddress,
                        color = SleekText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OfflineWarningStrip() {
    Surface(
        color = Color(0xFFFFEBEE),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = "Disconnect icon",
                tint = Color(0xFFC62828),
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = "DISCONNECTED: Commands queued until Raspberry Pi is back online",
                color = Color(0xFFC62828),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ------------------------------------------------------------------------
// TAB 1: MEDIA REMOTE CONTROL & SIMULATION PANEL
// ------------------------------------------------------------------------

@Composable
fun RemoteControlTab(
    viewModel: PiStreamerViewModel,
    piConnected: Boolean,
    activeTag: RfidTag?,
    isPlaying: Boolean,
    vol: Int,
    timerSec: Int,
    currentPos: Long,
    totalDuration: Long,
    speed: Int,
    outRoute: String,
    device: String,
    tagsList: List<RfidTag>
) {
    var expandedSimulateScan by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Quick Simulator Action
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = SleekCardBorder,
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Contactless,
                                contentDescription = "RFID hardware simulator",
                                tint = SleekPrimary
                            )
                            Column {
                                Text(
                                    text = "Raspberry Pi RFID Simulation",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekText
                                )
                                Text(
                                    text = "Tap to simulate a physical RFID chip scan on the Pi 5 box",
                                    fontSize = 10.sp,
                                    color = SleekTextMedium
                                )
                            }
                        }
                        IconButton(onClick = { expandedSimulateScan = !expandedSimulateScan }) {
                            Icon(
                                imageVector = if (expandedSimulateScan) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand scan menu",
                                tint = SleekTextMedium
                            )
                        }
                    }

                    if (expandedSimulateScan) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Configured RFID Chips:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekText,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        if (tagsList.isEmpty()) {
                            Text(
                                "No registered tags found in DB. Go to RFID Database tab to save tags.",
                                color = SleekTextMedium,
                                fontSize = 11.sp
                            )
                        } else {
                            // Flow of target RFID simulator tags
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_scan_box"),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tagsList.forEach { tag ->
                                    Button(
                                        onClick = { viewModel.simulateRfidScan(tag.uid) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (tag.isAuthorized) SleekSecondary else Color(0xFFFFEBEE)
                                        ),
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .border(
                                                width = 1.dp,
                                                color = if (tag.isAuthorized) SleekPrimary else Color(0xFFC62828),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = tag.uid,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (tag.isAuthorized) SleekDeepDark else Color(0xFFC62828)
                                            )
                                            Text(
                                                text = tag.title.take(10) + "...",
                                                fontSize = 8.sp,
                                                maxLines = 1,
                                                color = SleekTextMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4K Video Screen Canvas Simulation Component
        item {
            VideoMonitorCanvas(
                activeTag = activeTag,
                isPlaying = isPlaying,
                currentPosMs = currentPos,
                totalDurationMs = totalDuration,
                playbackSpeed = speed,
                outputRouting = outRoute
            )
        }

        // Active State & Remote Controller Player Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SleekCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Playback Timer + Routing state row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 15-Min Timer countdown & trigger
                        Column {
                            Text(
                                text = "PLAYBACK TIMEOUT TIMER",
                                color = SleekTextMedium,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer icon",
                                    tint = if (timerSec > 0) SleekPrimary else SleekTextMedium,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = formatSecondsToTime(timerSec),
                                    fontWeight = FontWeight.Bold,
                                    color = if (timerSec > 0) SleekPrimary else SleekTextMedium,
                                    fontSize = 15.sp
                                )
                                if (timerSec > 0) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Clear Timer",
                                        tint = SleekTextMedium,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.clearTimer() }
                                    )
                                }
                            }
                        }
 
                        // Add 15 Min Button
                        Button(
                            onClick = { viewModel.addFifteenMinuteTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekSecondary,
                                contentColor = SleekDeepDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("add_15m_timer_button")
                                .height(38.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "+15 MINS",
                                color = SleekDeepDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
 
                    HorizontalDivider(color = SleekCardBorder, modifier = Modifier.padding(vertical = 12.dp))
 
                    // Stream output routing details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STREAM ACCESS OUTPUT TARGET",
                                color = SleekTextMedium,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (outRoute == "HDMI") Icons.Default.Tv else Icons.Default.Cast,
                                    contentDescription = "Router output icon",
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (outRoute == "HDMI") "HDMI-0 (Direct UHD TV)" else "Wi-Fi Cast: $device",
                                    color = SleekText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
 
                        // Button trigger group for routes
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { viewModel.setOutputRoute("HDMI") },
                                modifier = Modifier
                                    .testTag("output_hdmi_mode")
                                    .size(36.dp)
                                    .background(
                                        if (outRoute == "HDMI") SleekSecondary else SleekButtonBg,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (outRoute == "HDMI") SleekPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = "Set Direct TV HDMI output",
                                    tint = if (outRoute == "HDMI") SleekDeepDark else SleekTextMedium,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
 
                            IconButton(
                                onClick = { viewModel.setOutputRoute("WIFI", "Bedroom Receiver Projector") },
                                modifier = Modifier
                                    .testTag("output_wifi_mode")
                                    .size(36.dp)
                                    .background(
                                        if (outRoute == "WIFI") SleekSecondary else SleekButtonBg,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (outRoute == "WIFI") SleekPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cast,
                                    contentDescription = "Set Wifi Cast to bedroom receiver",
                                    tint = if (outRoute == "WIFI") SleekDeepDark else SleekTextMedium,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
 
                    Spacer(modifier = Modifier.height(18.dp))
 
                    // Core Media playback remote buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skip Backward button
                        IconButton(
                            onClick = { viewModel.pressSkipBackward() },
                            modifier = Modifier
                                .testTag("skip_backward_button")
                                .size(48.dp)
                                .background(SleekButtonBg, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Skip back 10s",
                                tint = SleekText
                            )
                        }
 
                        // Play/Pause button
                        IconButton(
                            onClick = { viewModel.pressPlayPause() },
                            modifier = Modifier
                                .testTag("play_pause_button")
                                .size(64.dp)
                                .background(SleekPrimary, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
 
                        // Skip Forward button
                        IconButton(
                            onClick = { viewModel.pressSkipForward() },
                            modifier = Modifier
                                .testTag("skip_forward_button")
                                .size(48.dp)
                                .background(SleekButtonBg, shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Skip forward 10s",
                                tint = SleekText
                            )
                        }
 
                        // Fast Forward button (Speed modifier)
                        IconButton(
                            onClick = { viewModel.pressFastForward() },
                            modifier = Modifier
                                .testTag("fast_forward_button")
                                .size(48.dp)
                                .background(
                                    if (speed > 1) SleekSecondary else SleekButtonBg,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (speed > 1) SleekPrimary else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "Fast forward toggle",
                                    tint = if (speed > 1) SleekPrimary else SleekText,
                                    modifier = Modifier.size(16.dp)
                                )
                                if (speed > 1) {
                                    Text(
                                        text = "${speed}x",
                                        color = SleekPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
 
                    Spacer(modifier = Modifier.height(18.dp))
 
                    // Volume controller row (Buttons + Slider)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VOLUME LEVEL: $vol%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextMedium
                            )
                        }
 
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Decrement button
                            IconButton(
                                onClick = { viewModel.volumeDown() },
                                modifier = Modifier
                                    .testTag("volume_down_button")
                                    .size(48.dp)
                                    .background(SleekButtonBg, shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeDown,
                                    contentDescription = "Volume down button",
                                    tint = SleekText
                                )
                            }
 
                            // Slider
                            Slider(
                                value = vol.toFloat(),
                                onValueChange = { viewModel.setVolumeSlider(it.toInt()) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = SleekPrimary,
                                    activeTrackColor = SleekPrimary,
                                    inactiveTrackColor = SleekTabBg
                                ),
                                modifier = Modifier.weight(1f)
                            )
 
                            // Increment button
                            IconButton(
                                onClick = { viewModel.volumeUp() },
                                modifier = Modifier
                                    .testTag("volume_up_button")
                                    .size(48.dp)
                                    .background(SleekButtonBg, shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Volume up button",
                                    tint = SleekText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoMonitorCanvas(
    activeTag: RfidTag?,
    isPlaying: Boolean,
    currentPosMs: Long,
    totalDurationMs: Long,
    playbackSpeed: Int,
    outputRouting: String
) {
    Surface(
        color = SleekBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Draw visual abstract canvas showing signal streams or playback active artwork
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Draw cool background grid mapping
                val cols = 8
                val colGap = canvasWidth / cols
                for (i in 1..cols) {
                    drawLine(
                        color = SleekTabBorder,
                        start = Offset(i * colGap, 0f),
                        end = Offset(i * colGap, canvasHeight),
                        strokeWidth = 1f
                    )
                }
                val rows = 5
                val rowGap = canvasHeight / rows
                for (i in 1..rows) {
                    drawLine(
                        color = SleekTabBorder,
                        start = Offset(0f, i * rowGap),
                        end = Offset(canvasWidth, i * rowGap),
                        strokeWidth = 1f
                    )
                }

                // If stream is authorized & active, draw beautiful abstract soundwaves or signal bars
                if (activeTag != null && isPlaying) {
                    // Moving sine waves
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    val points = 60
                    val pointGap = canvasWidth / points
                    val speedFactor = (System.currentTimeMillis() % 10000000) / 100f
                    
                    for (waveIndex in 0..1) {
                        val strokeColor = if (waveIndex == 0) SleekPrimary.copy(alpha = 0.5f) else SleekProgressActive.copy(alpha = 0.3f)
                        val strokeW = if (waveIndex == 0) 3f else 1.5f
                        val waveOffset = if (waveIndex == 0) 0f else 45f
                        
                        var prevX = 0f
                        var prevY = canvasHeight / 2f
                        
                        for (i in 0..points) {
                            val x = i * pointGap
                            val angle = (i * 0.15) + (speedFactor * 0.05) + waveOffset
                            val y = (canvasHeight / 2f) + (Math.sin(angle) * (30f + waveIndex * 15f)).toFloat()
                            
                            drawLine(
                                color = strokeColor,
                                start = Offset(prevX, prevY),
                                end = Offset(x, y),
                                strokeWidth = strokeW,
                                pathEffect = if (waveIndex == 1) pathEffect else null
                            )
                            prevX = x
                            prevY = y
                        }
                    }
                }
            }

            // UI HUD Overlay info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top HUD row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (activeTag != null) SleekPrimary else SleekButtonBg,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (activeTag != null && isPlaying) "STREAM LIVE" else "PLAYER IDLE",
                                color = if (activeTag != null) Color.White else SleekTextMedium,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (activeTag != null) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.border(1.dp, SleekCardBorder, RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = activeTag.streamResolution,
                                    color = SleekPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (activeTag != null) {
                        Surface(
                            color = SleekDeepDark,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (outputRouting == "HDMI") Icons.Default.Tv else Icons.Default.Cast,
                                    contentDescription = "Output state tag icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = if (outputRouting == "HDMI") "HDMI OUT" else "WIFI CAST",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Middle HUD Content
                if (activeTag != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = activeTag.title,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = SleekText,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Cloud cloud database icon",
                                tint = SleekTextMedium,
                                modifier = Modifier.size(10.dp)
                              )
                            Text(
                                text = "URL Source: " + activeTag.streamUrl,
                                color = SleekTextMedium,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contactless,
                            contentDescription = "Place RFID chip",
                            tint = SleekTextMedium,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "NO LIVE STREAM CONNECTED",
                            color = SleekText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Scan or write an authorized RFID card to fetch its cloud streaming media path",
                            color = SleekTextMedium,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                // Bottom Timeline HUD Content
                if (activeTag != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentPosSec = currentPosMs / 1000
                            val totalDurationSec = totalDurationMs / 1000
                            
                            Text(
                                text = String.format("%02d:%02d", currentPosSec / 60, currentPosSec % 60),
                                color = SleekText,
                                fontSize = 10.sp
                            )
                            
                            if (playbackSpeed > 1) {
                                Text(
                                    text = "FAST-FORWARD ${playbackSpeed}X",
                                    color = SleekPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = String.format("%02d:%02d", totalDurationSec / 60, totalDurationSec % 60),
                                color = SleekTextMedium,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Custom Visual Progress Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(SleekProgressTrack, shape = RoundedCornerShape(2.dp))
                        ) {
                            val fraction = if (totalDurationMs > 0) currentPosMs.toFloat() / totalDurationMs else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                    .background(SleekProgressActive, shape = RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// TAB 2: RFID CHIP WRITER AND DATABASE MANAGEMENT
// ------------------------------------------------------------------------

@Composable
fun RfidDatabaseTab(
    viewModel: PiStreamerViewModel,
    tagsList: List<RfidTag>,
    isWritingMode: Boolean
) {
    var writeOptionTab by remember { mutableStateOf(0) }
    val writeSubTabs = listOf("Card Indexes", "App RFID Programmer")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = writeOptionTab,
            containerColor = SleekTabBg,
            contentColor = SleekPrimary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(width = 1.dp, color = SleekTabBorder, shape = RoundedCornerShape(8.dp))
        ) {
            writeSubTabs.forEachIndexed { idx, subTitle ->
                Tab(
                    selected = writeOptionTab == idx,
                    onClick = { writeOptionTab = idx },
                    text = {
                        Text(
                            subTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (writeOptionTab == idx) SleekText else SleekTextMedium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (writeOptionTab == 0) {
            // Card Index List view
            if (tagsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty indices",
                            tint = SleekTextMedium,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "CRITICAL: RFID chip table currently has zero records",
                            color = SleekText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tagsList) { tag ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SleekCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = tag.title,
                                                fontWeight = FontWeight.Bold,
                                                color = SleekText,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Surface(
                                                color = SleekSecondary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = tag.category,
                                                    color = SleekPrimary,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "UID: ${tag.uid} | Resolution: ${tag.streamResolution}",
                                            color = SleekTextMedium,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Authorize toggle (Access Control verification flags)
                                    IconButton(
                                        onClick = {
                                            viewModel.insertTag(tag.copy(isAuthorized = !tag.isAuthorized))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (tag.isAuthorized) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = "Toggle Authorization Status",
                                            tint = if (tag.isAuthorized) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Cloud Target: ${tag.streamUrl.take(24)}...",
                                        color = SleekTextMedium,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Quick Simulate scan button
                                        Button(
                                            onClick = { viewModel.simulateRfidScan(tag.uid) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                            modifier = Modifier.height(28.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("SIMULATE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Delete tag record
                                        IconButton(
                                            onClick = { viewModel.deleteRfidTag(tag) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete RFID chip record",
                                                tint = SleekTextMedium,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Interactive App RFID writing & creation programmer form
            RfidProgrammerForm(
                viewModel = viewModel,
                isWritingMode = isWritingMode
            )
        }
    }
}

@Composable
fun RfidProgrammerForm(
    viewModel: PiStreamerViewModel,
    isWritingMode: Boolean
) {
    var inputUid by remember { mutableStateOf("") }
    var inputTitle by remember { mutableStateOf("") }
    var inputStreamUrl by remember { mutableStateOf("") }
    var inputCategory by remember { mutableStateOf("Video Stream") }
    var inputRes by remember { mutableStateOf("4K UHD") }
    var inputAuthorized by remember { mutableStateOf(true) }

    val resolutions = listOf("4K UHD", "1080p FHD", "720p HD")
    val categories = listOf("Cinema", "Sci-Fi", "Music Loop", "Live Feed", "Custom Media")

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "WRITE & REGISTER RFID TARGET",
                fontWeight = FontWeight.Bold,
                color = SleekText,
                fontSize = 13.sp
            )

            // UID row with helper generator
            Text(
                "UID Identifier Hex (e.g. 4A:C1:F2:78)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputUid,
                    onValueChange = { inputUid = it },
                    placeholder = { Text("Leave blank for random", color = SleekTextMedium) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SleekText,
                        unfocusedTextColor = SleekText,
                        unfocusedPlaceholderColor = SleekTextMedium,
                        unfocusedBorderColor = SleekCardBorder,
                        focusedBorderColor = SleekPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val chars = "0123456789ABCDEF"
                        inputUid = (1..8).map { chars.random() }.chunked(2).joinToString(":") { it.joinToString("") }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekSecondary,
                        contentColor = SleekDeepDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("GENERATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekDeepDark)
                }
            }

            // Stream Title mapping to url data stored
            Text(
                "Media/Stream Title",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextMedium
            )
            OutlinedTextField(
                value = inputTitle,
                onValueChange = { inputTitle = it },
                placeholder = { Text("e.g. Space Odyssey 4K HDR", color = SleekTextMedium) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SleekText,
                    unfocusedTextColor = SleekText,
                    unfocusedBorderColor = SleekCardBorder,
                    focusedBorderColor = SleekPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // URL data stored in cloud
            Text(
                "Cloud Storage URL data stream",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextMedium
            )
            OutlinedTextField(
                value = inputStreamUrl,
                onValueChange = { inputStreamUrl = it },
                placeholder = { Text("https://my-cloud-storage.com/stream.mp4", color = SleekTextMedium) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SleekText,
                    unfocusedTextColor = SleekText,
                    unfocusedBorderColor = SleekCardBorder,
                    focusedBorderColor = SleekPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Stream Specifications
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Resolution Capabilities",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        resolutions.forEach { res ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { inputRes = res }
                                    .border(
                                        width = 1.dp,
                                        color = if (inputRes == res) SleekPrimary else SleekCardBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    ),
                                shape = RoundedCornerShape(6.dp),
                                color = if (inputRes == res) SleekSecondary else SleekButtonBg
                            ) {
                                Text(
                                    text = res,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (inputRes == res) SleekPrimary else SleekTextMedium,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Access Verification rule
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Access Control Rule",
                        fontWeight = FontWeight.Bold,
                        color = SleekText,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Authorizes the streaming box to stream data from this RFID chip",
                        color = SleekTextMedium,
                        fontSize = 10.sp
                    )
                }

                Switch(
                    checked = inputAuthorized,
                    onCheckedChange = { inputAuthorized = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SleekPrimary,
                        checkedTrackColor = SleekSecondary
                    )
                )
            }

            // Execute buttons
            Button(
                onClick = {
                    viewModel.writeRfidTag(
                        uid = inputUid,
                        title = inputTitle,
                        url = inputStreamUrl,
                        category = inputCategory,
                        isAuth = inputAuthorized,
                        resolution = inputRes
                    )
                    // Reset fields on success
                    inputUid = ""
                    inputTitle = ""
                    inputStreamUrl = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("write_tag_button")
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(10.dp),
                enabled = !isWritingMode
            ) {
                if (isWritingMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        Text(
                            text = "WRITING RFID PHYSICAL CELL...",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = "Upload/write RFID", tint = Color.White)
                        Text(
                            text = "WRITE & SAVE CLOUD DATA",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// TAB 3: WI-FI SETUP SECTOR
// ------------------------------------------------------------------------

@Composable
fun WifiConfigTab(
    viewModel: PiStreamerViewModel,
    currentSsid: String,
    ipAddress: String
) {
    var selectedSsid by remember { mutableStateOf(viewModel.wifiNetworks[0]) }
    var wifiPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SleekCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.NetworkWifi,
                    contentDescription = "Wifi pairing logo",
                    tint = SleekPrimary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Provision Pi 5 Wi-Fi Link",
                        fontWeight = FontWeight.Bold,
                        color = SleekText,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Configure WLAN configuration nodes via App BLE or local socket connection",
                        color = SleekTextMedium,
                        fontSize = 10.sp
                    )
                }
            }

            HorizontalDivider(color = SleekCardBorder)

            // Connected details
            Column {
                Text(
                    text = "CURRENTLY REGISTERED NETWORK",
                    color = SleekTextMedium,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = SleekButtonBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Wifi, contentDescription = "active wifi logo", tint = SleekPrimary)
                            Column {
                                Text(currentSsid, color = SleekText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Signal Strength: Excellent (90dBm)", color = SleekTextMedium, fontSize = 10.sp)
                            }
                        }
                        Surface(
                            color = SleekSecondary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "CONNECTED IP: $ipAddress",
                                color = SleekPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Wifi SSID selection dropdown simulator
            Text(
                text = "SELECT AIRPORT / ROUTER NETWORKS IN RANGE",
                color = SleekTextMedium,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )

            // Dynamic SSID Buttons listing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wifi_ssid_input"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                viewModel.wifiNetworks.forEach { network ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSsid = network }
                            .border(
                                width = 1.dp,
                                color = if (selectedSsid == network) SleekPrimary else SleekCardBorder,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedSsid == network) SleekSecondary else SleekButtonBg
                    ) {
                        Text(
                            text = network,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedSsid == network) SleekPrimary else SleekTextMedium,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Input field password
            Text(
                text = "NETWORK WPA/WPA2 PASSPHRASE",
                color = SleekTextMedium,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = wifiPassword,
                onValueChange = { wifiPassword = it },
                placeholder = { Text("WPA Passphrase payload target", color = SleekTextMedium) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SleekText,
                    unfocusedTextColor = SleekText,
                    unfocusedPlaceholderColor = SleekTextMedium,
                    unfocusedBorderColor = SleekCardBorder,
                    focusedBorderColor = SleekPrimary
                ),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password view",
                            tint = SleekTextMedium
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Save network button
            Button(
                onClick = {
                    viewModel.configureWifi(selectedSsid, wifiPassword)
                    wifiPassword = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Configure wifi icon", tint = Color.White)
                    Text("PROVISION NETWORK CREDENTIAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// BOTTOM: LIVE TERMINAL / CONSOLE LOG SYSTEM PANEL
// ------------------------------------------------------------------------

@Composable
fun TerminalLogsConsole(
    logs: List<ActivityLog>,
    onClearLogs: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekDeepDark),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(
                width = 1.dp,
                color = SleekPrimary.copy(alpha = 0.4f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header control row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SleekSecondary)
                    )
                    Text(
                        text = "RASPBERRY PI 5 LIVE CONSOLE LOGS",
                        color = SleekSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier
                        .testTag("clear_logs_button")
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Live System Terminal Logs",
                        tint = SleekSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Logs feed lists
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1E0A0F), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "~ console trace logs empty. Wait or trigger actions.",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(logs) { log ->
                            val timeStr = remember(log.timestamp) {
                                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                            }
                            val colorLogSet = when (log.type) {
                                "SUCCESS" -> Color(0xFF81C784)
                                "ERROR" -> Color(0xFFE57373)
                                "WARNING" -> Color(0xFFFFB74D)
                                else -> SleekSecondary
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "[$timeStr]",
                                    color = Color.Gray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "[${log.type}]",
                                    color = colorLogSet,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = log.message,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// HELPER TRANSLATORS
// ------------------------------------------------------------------------

fun formatSecondsToTime(seconds: Int): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
