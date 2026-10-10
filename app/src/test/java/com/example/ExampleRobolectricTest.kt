package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ChatMessageEntity
import com.example.model.ConversationEntity
import com.example.model.TaskItemEntity
import com.example.system.AndroidActionDispatcher
import com.example.system.DeviceAction
import com.example.ui.components.parseMarkdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NOVA AI", appName)
    }

    @Test
    fun `test action dispatcher detects allowlist actions`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dispatcher = AndroidActionDispatcher(context)

        val actionYouTube = dispatcher.detectActionFromQuery("Please open youtube for me")
        assertTrue(actionYouTube is DeviceAction.OpenYouTube)

        val actionChrome = dispatcher.detectActionFromQuery("launch chrome browser")
        assertTrue(actionChrome is DeviceAction.OpenChrome)

        val actionWifi = dispatcher.detectActionFromQuery("open wifi settings")
        assertTrue(actionWifi is DeviceAction.OpenWifiSettings)

        val actionTorch = dispatcher.detectActionFromQuery("turn on flashlight")
        assertTrue(actionTorch is DeviceAction.ToggleFlashlight)

        val actionBattery = dispatcher.detectActionFromQuery("check battery level")
        assertTrue(actionBattery is DeviceAction.CheckBattery)
    }

    @Test
    fun `test markdown parser extracts code blocks`() {
        val sample = "Here is some code:\n```kotlin\nval x = 42\n```\nAll done."
        val parts = parseMarkdown(sample)
        assertTrue(parts.size >= 2)
    }

    @Test
    fun `test conversation and task models`() {
        val conv = ConversationEntity(title = "Test Dialogue")
        assertNotNull(conv.id)
        assertEquals("Test Dialogue", conv.title)

        val task = TaskItemEntity(title = "Review codebase")
        assertEquals(false, task.isCompleted)
        val completedTask = task.copy(isCompleted = true)
        assertEquals(true, completedTask.isCompleted)
    }
}
