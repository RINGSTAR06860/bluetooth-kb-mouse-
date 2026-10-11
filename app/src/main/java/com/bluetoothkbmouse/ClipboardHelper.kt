package com.bluetoothkbmouse

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class ClipboardHelper(private val context: Context) {

    private val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun getText(): String? {
        return clipboard.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.text
            ?.toString()
    }

    fun setText(text: String) {
        clipboard.setPrimaryClip(ClipData.newPlainText("蓝牙键鼠", text))
    }
}
