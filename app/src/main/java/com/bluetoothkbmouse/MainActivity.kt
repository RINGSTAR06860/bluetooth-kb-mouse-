package com.bluetoothkbmouse

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var hidManager: HidDeviceManager
    private lateinit var keyboardSender: KeyboardSender
    private lateinit var mouseSender: MouseSender
    private lateinit var clipboardHelper: ClipboardHelper

    private lateinit var tvStatus: TextView
    private lateinit var etText: EditText
    private lateinit var tvProgress: TextView

    private var sendJob: Job? = null
    private var lastX = 0f
    private var lastY = 0f

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            initBluetooth()
        } else {
            Toast.makeText(this, "需要蓝牙权限才能使用", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        etText = findViewById(R.id.etText)
        tvProgress = findViewById(R.id.tvProgress)

        hidManager = HidDeviceManager(this)
        keyboardSender = KeyboardSender(hidManager)
        mouseSender = MouseSender(hidManager)
        clipboardHelper = ClipboardHelper(this)

        requestPermissions()
        setupButtons()
        observeConnection()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            initBluetooth()
        }
    }

    private fun initBluetooth() {
        hidManager.register()
    }

    private fun observeConnection() {
        lifecycleScope.launch {
            hidManager.connectionState.collect { state ->
                tvStatus.text = when (state) {
                    is HidDeviceManager.ConnectionState.Disconnected -> "状态：未连接"
                    is HidDeviceManager.ConnectionState.Registering -> "状态：正在注册 HID 设备…"
                    is HidDeviceManager.ConnectionState.Connected ->
                        "状态：已连接 ${state.device.name ?: "未知设备"}"
                    is HidDeviceManager.ConnectionState.Error ->
                        "状态：${state.message}"
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupButtons() {
        findViewById<Button>(R.id.btnReadClipboard).setOnClickListener {
            val text = clipboardHelper.getText()
            if (text.isNullOrEmpty()) {
                Toast.makeText(this, "剪切板为空", Toast.LENGTH_SHORT).show()
            } else {
                etText.setText(text)
                Toast.makeText(this, "已读取 ${text.length} 个字符", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val text = etText.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendJob?.cancel()
            sendJob = lifecycleScope.launch {
                tvProgress.text = "发送中…"
                val sent = keyboardSender.sendText(text)
                tvProgress.text = "已发送 $sent / ${text.length} 个字符"
            }
        }

        findViewById<Button>(R.id.btnPaste).setOnClickListener {
            keyboardSender.sendPaste()
            Toast.makeText(this, "已发送 Ctrl+V", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnLeftClick).setOnClickListener {
            mouseSender.click()
        }

        findViewById<Button>(R.id.btnRightClick).setOnClickListener {
            mouseSender.rightClick()
        }

        val trackpad = findViewById<View>(R.id.trackpad)
        trackpad.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    mouseSender.move(dx.toInt(), dy.toInt())
                    lastX = event.x
                    lastY = event.y
                }
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - lastX) < 10 &&
                        abs(event.y - lastY) < 10 &&
                        event.eventTime - event.downTime < 200
                    ) {
                        mouseSender.click()
                    }
                }
            }
            true
        }
    }

    override fun onDestroy() {
        sendJob?.cancel()
        hidManager.unregister()
        super.onDestroy()
    }
}
