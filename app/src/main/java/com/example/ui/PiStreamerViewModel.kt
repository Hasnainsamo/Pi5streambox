package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.RfidRepository
import com.example.data.RfidTag
import com.example.data.ActivityLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class PiStreamerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RfidRepository

    // Database Flows
    val allTags: StateFlow<List<RfidTag>>
    val allLogs: StateFlow<List<ActivityLog>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RfidRepository(database.rfidDao())
        
        allTags = repository.allTags
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            
        allLogs = repository.allLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Ensure database is prepopulated
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }

        // Start Raspberry Pi 5 fake telemetry metrics loop
        startTelemetryLoop()
        
        // Start playback and timer loop
        startPlaybackTimerLoop()
    }

    // Raspberry Pi 5 Telemetry States
    private val _piConnected = MutableStateFlow(true)
    val piConnected = _piConnected.asStateFlow()

    private val _cpuTemp = MutableStateFlow(42.5)
    val cpuTemp = _cpuTemp.asStateFlow()

    private val _cpuLoad = MutableStateFlow(12.0)
    val cpuLoad = _cpuLoad.asStateFlow()

    private val _ramUsage = MutableStateFlow("1.3 GB / 8.0 GB")
    val ramUsage = _ramUsage.asStateFlow()

    private val _ipAddress = MutableStateFlow("192.168.1.150")
    val ipAddress = _ipAddress.asStateFlow()

    private val _wifiSsid = MutableStateFlow("Pi5_HyperNet_5G")
    val wifiSsid = _wifiSsid.asStateFlow()

    // Active Playback State
    private val _activePlayingTag = MutableStateFlow<RfidTag?>(null)
    val activePlayingTag = _activePlayingTag.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(75) // 0 - 100
    val volume = _volume.asStateFlow()

    private val _timerSecondsRemaining = MutableStateFlow(0)
    val timerSecondsRemaining = _timerSecondsRemaining.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(180000L) // Default 3 mins
    val totalDurationMs = _totalDurationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1) // 1x, 2x, 4x, 8x Fast-Forward
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _outputRouting = MutableStateFlow("HDMI") // HDMI, Wi-Fi Cast
    val outputRouting = _outputRouting.asStateFlow()

    private val _receivingDevice = MutableStateFlow("Samsung 4K Living Room TV")
    val receivingDevice = _receivingDevice.asStateFlow()

    // Access authorization result feedback
    private val _accessDeniedTag = MutableStateFlow<String?>(null)
    val accessDeniedTag = _accessDeniedTag.asStateFlow()

    // Scanning/Writing Status
    private val _isWritingMode = MutableStateFlow(false)
    val isWritingMode = _isWritingMode.asStateFlow()

    // Wi-Fi Candidate Networks
    val wifiNetworks = listOf("Pi5_HyperNet_5G", "Home_WiFi_2.4G", "Office_Corporate", "Guest_Broadband")

    private var telemetryJob: Job? = null
    private var playbackJob: Job? = null

    private fun startTelemetryLoop() {
        telemetryJob = viewModelScope.launch {
            while (true) {
                delay(4000)
                if (_piConnected.value) {
                    // Jitter telemetry variables to make the screen dynamic & alive
                    _cpuTemp.value = 40.0 + (0..15).random() + (0..9).random() / 10.0
                    _cpuLoad.value = 5.0 + (0..25).random() + (0..9).random() / 10.0
                    // RAM variation
                    val ramVal = 1.1 + (0..4).random() / 10.0
                    _ramUsage.value = String.format("%.1f GB / 8.0 GB", ramVal)
                }
            }
        }
    }

    private fun startPlaybackTimerLoop() {
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                // Handle Video Playback progress simulation
                if (_isPlaying.value && _activePlayingTag.value != null) {
                    val nextPos = _currentPositionMs.value + (1000 * _playbackSpeed.value)
                    if (nextPos >= _totalDurationMs.value) {
                        _currentPositionMs.value = 0
                        // Loop video playback
                        repository.insertLog("INFO", "Stream reached end of play. Looping video segment...")
                    } else {
                        _currentPositionMs.value = nextPos
                    }
                }

                // Handle Timer Countdown
                if (_timerSecondsRemaining.value > 0) {
                    _timerSecondsRemaining.value -= 1
                    if (_timerSecondsRemaining.value == 0) {
                        // Stop streaming when timer ends
                        _isPlaying.value = false
                        _activePlayingTag.value = null
                        _currentPositionMs.value = 0L
                        repository.insertLog("WARNING", "Playback timer expired. Automatic streaming cutoff applied.")
                    }
                }
            }
        }
    }

    // Wi-Fi Connections Control
    fun configureWifi(ssid: String, secret: String) {
        viewModelScope.launch {
            repository.insertLog("INFO", "Pushing Wi-Fi credential pack to Raspberry Pi 5...")
            delay(1500)
            _wifiSsid.value = ssid
            repository.insertLog("SUCCESS", "Raspberry Pi 5 Wi-Fi successfully changed to: $ssid")
        }
    }

    // Pi Connection Control Toggle
    fun togglePiConnection() {
        _piConnected.value = !_piConnected.value
        viewModelScope.launch {
            if (_piConnected.value) {
                repository.insertLog("SUCCESS", "App successfully connected to Raspberry Pi 5 IP: ${_ipAddress.value}")
            } else {
                repository.insertLog("WARNING", "App disconnected from Raspberry Pi 5.")
                _isPlaying.value = false
            }
        }
    }

    // Simulated Scanning (Taps simulated card from hardware or custom slider)
    fun simulateRfidScan(uid: String) {
        viewModelScope.launch {
            _accessDeniedTag.value = null
            repository.insertLog("INFO", "Raspberry Pi RFID Scan Event: UID [$uid]")
            delay(1000)
            val tag = repository.getTag(uid)
            if (tag == null) {
                repository.insertLog("WARNING", "Unknown RFID UID scan attempt: $uid. Card payload unregistered.")
                _accessDeniedTag.value = uid
            } else {
                // Check Access Control authorization
                if (tag.isAuthorized) {
                    // Update tag scanning telemetry
                    val updatedTag = tag.copy(lastScanned = System.currentTimeMillis())
                    repository.insertTag(updatedTag)
                    
                    _activePlayingTag.value = updatedTag
                    _isPlaying.value = true
                    _currentPositionMs.value = 0L
                    _playbackSpeed.value = 1
                    _totalDurationMs.value = if (tag.title.contains("Sintel")) 320000L else 240000L // mock lengths

                    repository.insertLog(
                        "SUCCESS", 
                        "ACCESS GRANTED - RFID Tag Authorized: ${tag.title} in ${tag.streamResolution}"
                    )
                    repository.insertLog(
                        "INFO", 
                        "Cloud-sync: Loaded Stream URL: ${tag.streamUrl} via ${_outputRouting.value}"
                    )
                } else {
                    _accessDeniedTag.value = tag.title
                    _activePlayingTag.value = null
                    _isPlaying.value = false
                    repository.insertLog(
                        "ERROR", 
                        "ACCESS DENIED - RFID UID [$uid] is unauthorized/revoked from streaming."
                    )
                }
            }
        }
    }

    // Playback Remote Actions
    fun pressPlayPause() {
        if (!_piConnected.value) return
        _isPlaying.value = !_isPlaying.value
        viewModelScope.launch {
            val status = if (_isPlaying.value) "Resumed" else "Paused"
            repository.insertLog("INFO", "Remote Playback Event: $status stream playlist")
        }
    }

    fun pressFastForward() {
        if (!_piConnected.value) return
        val currentSpeed = _playbackSpeed.value
        val nextSpeed = when (currentSpeed) {
            1 -> 2
            2 -> 4
            4 -> 8
            else -> 1
        }
        _playbackSpeed.value = nextSpeed
        viewModelScope.launch {
            val msg = if (nextSpeed == 1) "Normal Speed (1x)" else "Fast-forward Mode enabled: ${nextSpeed}x"
            repository.insertLog("INFO", "Remote Fast-Forward speed updated: $msg")
        }
    }

    fun pressSkipForward() {
        if (!_piConnected.value) return
        viewModelScope.launch {
            val nextPos = _currentPositionMs.value + 15000L
            _currentPositionMs.value = if (nextPos >= _totalDurationMs.value) 0 else nextPos
            repository.insertLog("INFO", "Remote Skip: Forwarded stream by 15s")
        }
    }

    fun pressSkipBackward() {
        if (!_piConnected.value) return
        viewModelScope.launch {
            val nextPos = _currentPositionMs.value - 15000L
            _currentPositionMs.value = if (nextPos < 0) 0 else nextPos
            repository.insertLog("INFO", "Remote Skip: Rewound stream by 15s")
        }
    }

    // Volume controllers with buttons
    fun volumeUp() {
        if (!_piConnected.value) return
        _volume.value = (_volume.value + 5).coerceAtMost(100)
        viewModelScope.launch {
            repository.insertLog("INFO", "Volume raised to ${_volume.value}%")
        }
    }

    fun volumeDown() {
        if (!_piConnected.value) return
        _volume.value = (_volume.value - 5).coerceAtLeast(0)
        viewModelScope.launch {
            repository.insertLog("INFO", "Volume lowered to ${_volume.value}%")
        }
    }
    
    fun setVolumeSlider(newVal: Int) {
        if (!_piConnected.value) return
        _volume.value = newVal.coerceIn(0, 100)
    }

    // 15-Minute Playback Timer Function per button click
    fun addFifteenMinuteTimer() {
        if (!_piConnected.value) return
        _timerSecondsRemaining.value += 15 * 60 // adds 15 minutes (900 seconds)
        viewModelScope.launch {
            val totalMins = _timerSecondsRemaining.value / 60
            repository.insertLog("SUCCESS", "Timer updated: Added 15 minutes. Stream cutoff in $totalMins mins.")
        }
    }

    fun clearTimer() {
        _timerSecondsRemaining.value = 0
        viewModelScope.launch {
            repository.insertLog("INFO", "Active stream cutoff timer canceled.")
        }
    }

    // Output route change (HDMI or WiFi Receiver)
    fun setOutputRoute(route: String, device: String = "") {
        if (!_piConnected.value) return
        _outputRouting.value = route
        if (device.isNotEmpty()) {
            _receivingDevice.value = device
        }
        viewModelScope.launch {
            repository.insertLog("INFO", "Stream output routed to $route: ${if (route == "HDMI") "Direct TV Connection" else _receivingDevice.value}")
        }
    }

    // RFID Writer Functionality (Write via app / stream box)
    fun writeRfidTag(uid: String, title: String, url: String, category: String, isAuth: Boolean, resolution: String) {
        viewModelScope.launch {
            _isWritingMode.value = true
            repository.insertLog("INFO", "RFID Writer activated: Waiting for physical tag placement on Raspberry Pi...")
            delay(1500) // simulated write duration
            
            val cleanUid = uid.ifBlank { UUID.randomUUID().toString().take(8).uppercase() }
            val newTag = RfidTag(
                uid = cleanUid,
                title = title.ifBlank { "Written Tag Custom" },
                streamUrl = url.ifBlank { "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4" },
                isAuthorized = isAuth,
                streamResolution = resolution,
                category = category.ifBlank { "Custom Media" },
                lastScanned = System.currentTimeMillis()
            )
            repository.insertTag(newTag)
            _isWritingMode.value = false
            repository.insertLog("SUCCESS", "RFID WRITE COMPLETE! Saved UID [$cleanUid] titled '${newTag.title}' to cloud indexes.")
            
            // Immediately load to playback if authorized
            if (isAuth) {
                _activePlayingTag.value = newTag
                _isPlaying.value = true
                _currentPositionMs.value = 0
            }
        }
    }

    fun insertTag(tag: RfidTag) {
        viewModelScope.launch {
            repository.insertTag(tag)
        }
    }

    fun deleteRfidTag(tag: RfidTag) {
        viewModelScope.launch {
            repository.deleteTag(tag)
            repository.insertLog("WARNING", "Removed RFID database linkage for tag UID: ${tag.uid}")
            if (_activePlayingTag.value?.uid == tag.uid) {
                _activePlayingTag.value = null
                _isPlaying.value = false
            }
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun clearAccessDenied() {
        _accessDeniedTag.value = null
    }
}
