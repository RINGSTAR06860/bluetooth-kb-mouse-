package com.bluetoothkbmouse

class MouseSender(private val hidManager: HidDeviceManager) {

    companion object {
        const val BUTTON_LEFT = 0x01
        const val BUTTON_RIGHT = 0x02
        const val BUTTON_MIDDLE = 0x04
    }

    fun move(dx: Int, dy: Int, buttons: Int = 0) {
        val report = byteArrayOf(
            buttons.toByte(),
            dx.coerceIn(-127, 127).toByte(),
            dy.coerceIn(-127, 127).toByte(),
            0x00
        )
        hidManager.sendReport(HidDescriptors.REPORT_ID_MOUSE, report)
    }

    fun scroll(amount: Int) {
        val report = byteArrayOf(
            0x00, 0x00, 0x00,
            amount.coerceIn(-127, 127).toByte()
        )
        hidManager.sendReport(HidDescriptors.REPORT_ID_MOUSE, report)
    }

    fun press(button: Int) = move(0, 0, button)
    fun release() = move(0, 0, 0)

    fun click() {
        press(BUTTON_LEFT)
        release()
    }

    fun rightClick() {
        press(BUTTON_RIGHT)
        release()
    }
}
