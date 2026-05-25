package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.PiStreamerDashboard
import com.example.ui.PiStreamerViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PiStreamerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Intercept uncaughtexceptions and record to local filesDir before exiting
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("RFID_STREAM_CRASH", "UNCAUGHT RUNTIME EXCEPTION IN ${thread.name}", throwable)
            try {
                val file = java.io.File(filesDir, "crash.txt")
                file.writeText(
                    "Thread: ${thread.name}\n" +
                    "Exception: ${throwable.javaClass.name}\n" +
                    "Message: ${throwable.localizedMessage}\n\n" +
                    "StackTrace:\n${throwable.stackTraceToString()}"
                )
            } catch (e: Exception) {
                // Secondary fallback
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        super.onCreate(savedInstanceState)
        
        // Check if a prior crash occurred and load report gracefully
        viewModel.checkForCrashReports(filesDir)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PiStreamerDashboard(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
