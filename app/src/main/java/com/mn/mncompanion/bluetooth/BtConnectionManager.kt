package com.mn.mncompanion.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.annotation.WorkerThread
import com.mn.mncompanion.logd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private const val namePrefix = "MN-"
private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

class BtConnectionManager(context: Context) {
    private val btAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private var bondedDevices: List<BluetoothDevice> = emptyList()
    private var activeDevice: BluetoothDevice? = null
    private var socket: BluetoothSocket? = null

    @WorkerThread
    suspend fun flushPackages() {
        withContext(Dispatchers.IO) {
            if (socket == null)
                throw IllegalStateException("There is no connected socket")

            val inputStream = socket!!.inputStream
            val buffer = ByteArray(32)
            while (inputStream.available() > 0) {
                inputStream.read(buffer)
            }
        }
    }

    @WorkerThread
    suspend fun readNextPackage(): ByteArray {
        return withContext(Dispatchers.IO) {
            if (socket == null)
                throw IllegalStateException("There is no connected socket")

            val inputStream = socket!!.inputStream
            while (inputStream.available() < MN_PACKET_SIZE) {
                delay(50)
            }
            val buffer = ByteArray(MN_PACKET_SIZE)
            val bytesRead = inputStream.read(buffer)
            if (bytesRead != MN_PACKET_SIZE)
                throw IllegalStateException("Input stream cannot give full package, but has it")

            return@withContext buffer
        }
    }

    @SuppressLint("MissingPermission")
    fun getBondedMNs(): List<BluetoothDevice> {
        bondedDevices = btAdapter.bondedDevices.filter { it.name.startsWith(namePrefix) }
        return bondedDevices
    }

    @WorkerThread
    @SuppressLint("MissingPermission")
    suspend fun connectDevice(btDevice: BluetoothDevice) {
        withContext(Dispatchers.IO) {
            logd("BtConnectionManager::connectDevice")
            if (!bondedDevices.contains(btDevice))
                throw IllegalStateException("Unknown Bluetooth Device")

            btAdapter.cancelDiscovery()
            logd("BtConnectionManager::connectDevice cancelled discovery")
            try {
                val btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(uuid)
                logd("BtConnectionManager::connectDevice created socket, connecting...")
                val stopConnectionJob = launch(Dispatchers.Main) {
                    delay(10000)
                    if (!btSocket.isConnected) {
                        logd("BtConnectionManager::connectDevice MANUAL CLOSING not yet socket after timeout")
                        btSocket.close()
                    }
                }

                btSocket.connect()
                logd("BtConnectionManager::connectDevice: after btSocket.connect()")
                if (btSocket.isConnected) {
                    stopConnectionJob.cancel()
                    activeDevice = btDevice
                    socket = btSocket
                }
            } catch (e: Exception) {
                logd("BtConnectionManager::connectDevice - exception: ${e.message}")
                activeDevice = null
                socket = null
                throw e
            }
        }

    }

    @WorkerThread
    fun finishDevice() {
        try {
            socket?.close()
            socket = null
            activeDevice = null
        } catch (_: Exception) {
        }
    }
}
