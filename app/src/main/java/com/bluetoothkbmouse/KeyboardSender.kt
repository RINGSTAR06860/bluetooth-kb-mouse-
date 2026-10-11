package com.bluetoothkbmouse

import kotlinx.coroutines.delay

class KeyboardSender(private val hidManager: HidDeviceManager) {

    companion object {
        var charDelayMs: Long = 10

        const val MOD_NONE = 0x00
        const val MOD_LEFT_SHIFT = 0x02
        const val MOD_LEFT_CTRL = 0x01
        const val MOD_LEFT_ALT = 0x04

        private val KEY_MAP = mapOf(
            'a' to 0x04, 'b' to 0x05, 'c' to 0x06, 'd' to 0x07,
            'e' to 0x08, 'f' to 0x09, 'g' to 0x0A, 'h' to 0x0B,
            'i' to 0x0C, 'j' to 0x0D, 'k' to 0x0E, 'l' to 0x0F,
            'm' to 0x10, 'n' to 0x11, 'o' to 0x12, 'p' to 0x13,
            'q' to 0x14, 'r' to 0x15, 's' to 0x16, 't' to 0x17,
            'u' to 0x18, 'v' to 0x19, 'w' to 0x1A, 'x' to 0x1B,
            'y' to 0x1C, 'z' to 0x1D,
            '1' to 0x1E, '2' to 0x1F, '3' to 0x20, '4' to 0x21,
            '5' to 0x22, '6' to 0x23, '7' to 0x24, '8' to 0x25,
            '9' to 0x26, '0' to 0x27,
            '\n' to 0x28, '\r' to 0x28,
            '\u001B' to 0x29, '\b' to 0x2A, '\t' to 0x2B,
            ' ' to 0x2C,
            '-' to 0x2D, '_' to 0x2D,
            '=' to 0x2E, '+' to 0x2E,
            '[' to 0x2F, '{' to 0x2F,
            ']' to 0x30, '}' to 0x30,
            '\\' to 0x31, '|' to 0x31,
            ';' to 0x33, ':' to 0x33,
            '\'' to 0x34, '"' to 0x34,
            '`' to 0x35, '~' to 0x35,
            ',' to 0x36, '<' to 0x36,
            '.' to 0x37, '>' to 0x37,
            '/' to 0x38, '?' to 0x38
        )

        private val SHIFT_CHARS = setOf(
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')',
            '_', '+', '{', '}', '|', ':', '"', '~', '<', '>', '?'
        )
    }

    suspend fun sendText(text: String): Int {
        var sent = 0
        for (char in text) {
            if (sendChar(char)) sent++
            delay(charDelayMs)
        }
        return sent
    }

    private fun sendChar(char: Char): Boolean {
        val lower = char.lowercaseChar()
        val keyCode = KEY_MAP[lower] ?: return false

        val modifier = when {
            char.isUpperCase() -> MOD_LEFT_SHIFT
            char in SHIFT_CHARS -> MOD_LEFT_SHIFT
            else -> MOD_NONE
        }

        val press = byteArrayOf(
            modifier.toByte(), 0x00, keyCode.toByte(),
            0x00, 0x00, 0x00, 0x00, 0x00
        )
        hidManager.sendReport(HidDescriptors.REPORT_ID_KEYBOARD, press)

        val release = ByteArray(8) { 0x00 }
        hidManager.sendReport(HidDescriptors.REPORT_ID_KEYBOARD, release)
        return true
    }

    fun sendShortcut(modifier: Int, keyCode: Int) {
        val press = byteArrayOf(
            modifier.toByte(), 0x00, keyCode.toByte(),
            0x00, 0x00, 0x00, 0x00, 0x00
        )
        hidManager.sendReport(HidDescriptors.REPORT_ID_KEYBOARD, press)
        val release = ByteArray(8) { 0x00 }
        hidManager.sendReport(HidDescriptors.REPORT_ID_KEYBOARD, release)
    }

    fun sendPaste() = sendShortcut(MOD_LEFT_CTRL, 0x19)
}
