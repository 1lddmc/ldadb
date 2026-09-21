package com.LDadb.ui.remote

import com.LDadb.R
import com.LDadb.i18n.AppText
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteKeyTest {
    @Test
    fun keyCodes_matchAndroidInputKeyEvents() {
        assertEquals("KEYCODE_BACK", RemoteKey.Back.keyCode)
        assertEquals("KEYCODE_DPAD_CENTER", RemoteKey.Center.keyCode)
        assertEquals("KEYCODE_MEDIA_PLAY_PAUSE", RemoteKey.PlayPause.keyCode)
    }

    @Test
    fun inputPermissionError_usesCompactSuggestion() {
        val error = "java.lang.SecurityException: Injecting input events requires INJECT_EVENTS permission"

        assertEquals(
            AppText.Res(R.string.remote_key_control_blocked),
            error.toRemoteInputSuggestion(),
        )
    }
}
