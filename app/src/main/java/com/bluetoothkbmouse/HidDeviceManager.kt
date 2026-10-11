package com.bluetoothkbmouse

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.Executors

class HidDeviceManager(private val context: Context) {

    companion object {
        private const val TAG = "HidDeviceManager"
        const val DEVICE_NAME = "蓝牙键鼠"
        const val DEVICE_DESCRIPTION = "Android 蓝牙键盘鼠标"
        const val DEVICE_PROVIDER = "BluetoothKbMouse"
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter = bluetoothManager.adapter

    private var hidDevice: BluetoothHidDevice? = null
    private var targetDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Registering : ConnectionState()
        data class Connected(val device: BluetoothDevice) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    private val executor = Executors.newSingleThreadExecutor()

    private val callback = object : BluetoothHidDevice.Callback() {

        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            Log.d(TAG, "onAppStatusChanged: registered=$registered")
            if (registered) {
                _connectionState.value = ConnectionState.Disconnected
            } else {
                _connectionState.value =
                    ConnectionState.Error("HID 应用注册失败，设备可能不支持")
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
            Log.d(TAG, "onConnectionStateChanged: ${device.name}, state=$state")
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    targetDevice = device
                    _connectionState.value = ConnectionState.Connected(device)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    targetDevice = null
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun register() {
        _connectionState.value = ConnectionState.Registering

        adapter.getProfileProxy(
            context,
            object : BluetoothProfile.ServiceListener {
                @SuppressLint("MissingPermission")
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (profile == BluetoothProfile.HID_DEVICE) {
                        hidDevice = proxy as BluetoothHidDevice

                        val sdpSettings = BluetoothHidDeviceAppSdpSettings(
                            DEVICE_NAME,
                            DEVICE_DESCRIPTION,
                            DEVICE_PROVIDER,
                            BluetoothHidDevice.SUBCLASS1_COMBO,
                            HidDescriptors.COMBINED_DESCRIPTOR
                        )

                        val ok = hidDevice?.registerApp(
                            sdpSettings,
                            null,
                            null,
                            executor,
                            callback
                        )
                        Log.d(TAG, "registerApp result: $ok")
                        if (ok != true) {
                            _connectionState.value = ConnectionState.Error(
                                "注册 HID 应用失败，请检查设备是否支持 Bluetooth HID Device"
                            )
                        }
                    }
                }

                override fun onServiceDisconnected(profile: Int) {
                    hidDevice = null
                }
            },
            BluetoothProfile.HID_DEVICE
        )
    }

    @SuppressLint("MissingPermission")
    fun sendReport(reportId: Int, data: ByteArray): Boolean {
        val device = targetDevice ?: return false
        return hidDevice?.sendReport(device, reportId, data) ?: false
    }

    fun unregister() {
        hidDevice?.unregisterApp()
        hidDevice = null
        targetDevice = null
        executor.shutdown()
    }
}
