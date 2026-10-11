package com.bluetoothkbmouse

object HidDescriptors {

    const val REPORT_ID_KEYBOARD = 1
    const val REPORT_ID_MOUSE = 2

    val COMBINED_DESCRIPTOR = byteArrayOf(
        // ===== 键盘 =====
        0x05.toByte(), 0x01.toByte(),
        0x09.toByte(), 0x06.toByte(),
        0xA1.toByte(), 0x01.toByte(),

        0x05.toByte(), 0x07.toByte(),
        0x19.toByte(), 0xE0.toByte(),
        0x29.toByte(), 0xE7.toByte(),
        0x15.toByte(), 0x00.toByte(),
        0x25.toByte(), 0x01.toByte(),
        0x75.toByte(), 0x01.toByte(),
        0x95.toByte(), 0x08.toByte(),
        0x81.toByte(), 0x02.toByte(),

        0x95.toByte(), 0x01.toByte(),
        0x75.toByte(), 0x08.toByte(),
        0x81.toByte(), 0x01.toByte(),

        0x05.toByte(), 0x07.toByte(),
        0x19.toByte(), 0x00.toByte(),
        0x29.toByte(), 0x65.toByte(),
        0x15.toByte(), 0x00.toByte(),
        0x25.toByte(), 0x65.toByte(),
        0x75.toByte(), 0x08.toByte(),
        0x95.toByte(), 0x06.toByte(),
        0x81.toByte(), 0x00.toByte(),

        0xC0.toByte(),

        // ===== 鼠标 =====
        0x05.toByte(), 0x01.toByte(),
        0x09.toByte(), 0x02.toByte(),
        0xA1.toByte(), 0x01.toByte(),
        0x85.toByte(), REPORT_ID_MOUSE.toByte(),

        0x05.toByte(), 0x09.toByte(),
        0x19.toByte(), 0x01.toByte(),
        0x29.toByte(), 0x03.toByte(),
        0x15.toByte(), 0x00.toByte(),
        0x25.toByte(), 0x01.toByte(),
        0x75.toByte(), 0x01.toByte(),
        0x95.toByte(), 0x03.toByte(),
        0x81.toByte(), 0x02.toByte(),
        0x75.toByte(), 0x05.toByte(),
        0x95.toByte(), 0x01.toByte(),
        0x81.toByte(), 0x01.toByte(),

        0x05.toByte(), 0x01.toByte(),
        0x09.toByte(), 0x30.toByte(),
        0x09.toByte(), 0x31.toByte(),
        0x15.toByte(), 0x81.toByte(),
        0x25.toByte(), 0x7F.toByte(),
        0x75.toByte(), 0x08.toByte(),
        0x95.toByte(), 0x02.toByte(),
        0x81.toByte(), 0x06.toByte(),

        0x09.toByte(), 0x38.toByte(),
        0x15.toByte(), 0x81.toByte(),
        0x25.toByte(), 0x7F.toByte(),
        0x75.toByte(), 0x08.toByte(),
        0x95.toByte(), 0x01.toByte(),
        0x81.toByte(), 0x06.toByte(),

        0xC0.toByte()
    )
}
