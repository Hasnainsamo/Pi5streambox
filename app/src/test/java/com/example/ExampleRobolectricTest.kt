package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.PiStreamerDashboard
import com.example.ui.PiStreamerViewModel
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RFID Stream Controller", appName)
  }

  @Test
  fun testViewModelAndDashboardRendering() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = PiStreamerViewModel(application)
    composeTestRule.setContent {
      MyApplicationTheme {
        PiStreamerDashboard(viewModel = viewModel)
      }
    }
  }
}
