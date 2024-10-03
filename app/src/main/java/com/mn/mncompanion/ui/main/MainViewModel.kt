package com.mn.mncompanion.ui.main

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mn.mncompanion.R
import com.mn.mncompanion.StateHolder
import com.mn.mncompanion.bluetooth.BtConnectionManager
import com.mn.mncompanion.bluetooth.MnMessage
import com.mn.mncompanion.bluetooth.MnMessageSkippedUpdater
import com.mn.mncompanion.bluetooth.hasNewValidNfcData
import com.mn.mncompanion.bluetooth.isCorrupted
import com.mn.mncompanion.logd
import com.mn.mncompanion.ui.components.ReadableString
import com.mn.mncompanion.ui.musicappcontrol.AdvancedMusicControl
import com.mn.mncompanion.ui.musicappcontrol.BurnInfo
import com.mn.mncompanion.ui.musicappcontrol.poweramp.Poweramp
import com.mn.mncompanion.ui.musicappcontrol.TrackInfo
import com.mn.mncompanion.ui.musicappcontrol.getInstalledSupportedApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicApp(val name: String, val packageName: String) : ReadableString {
    override fun asReadableString(): String {
        return name
    }
}

class MnBluetoothDevice(val btDevice: BluetoothDevice) : ReadableString {
    @SuppressLint("MissingPermission")
    override fun asReadableString(): String {
        return btDevice.name
    }
}

class MainViewModel(
    private val appContext: Context,
    private val btConnectionManager: BtConnectionManager
) : ViewModel() {
    private var collectMnDataJob: Job? = null
    private val state = StateHolder()

    val showProgress: LiveData<Boolean> = state.register(false)
    val chosenApp: LiveData<MusicApp?> = state.register(null)
    val chosenDevice: LiveData<MnBluetoothDevice?> = state.register(null)
    val deviceStatus: LiveData<String?> = state.register(null)
    val availableApps: LiveData<List<MusicApp>?> = state.register(null)
    val availableMnDevices: LiveData<List<MnBluetoothDevice>?> = state.register(null)
    val showInputCheck: LiveData<Boolean> = state.register(false)
    val mnMessage: LiveData<MnMessage?> = state.register(null)
    val trackInfo: LiveData<TrackInfo?> = state.register(null)
    val showBurnNfcScreen: LiveData<Boolean> = state.register(false)

    private val musicControl: AdvancedMusicControl = Poweramp(appContext)

    @MainThread
    fun onChooseAppClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            stateFor(availableApps).postValue(
                appContext.getInstalledSupportedApps()
            )
        }
    }

    @MainThread
    fun onAppChosen(app: MusicApp) {
        stateFor(chosenApp).value = app
        closeAppChooser()
    }

    @MainThread
    fun closeAppChooser() {
        stateFor(availableApps).value = null
    }

    @MainThread
    fun onChooseMnDeviceClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            stateFor(availableMnDevices).postValue(
                btConnectionManager.getBondedMNs().map { MnBluetoothDevice(it) }
            )
        }
    }

    @MainThread
    fun onMnDeviceChosen(device: MnBluetoothDevice) {
        stateFor(chosenDevice).value = device
        stateFor(deviceStatus).value = null
        closeMnDeviceChooser()
        viewModelScope.launch { connectMnDevice(device) }
    }

    @MainThread
    fun closeMnDeviceChooser() {
        stateFor(availableMnDevices).value = null
    }

    @MainThread
    fun onCheckInputClicked() {
        stateFor(showInputCheck).value = true
    }

    @MainThread
    fun closeInputCheck() {
        stateFor(showInputCheck).value = false
    }

    @MainThread
    fun onFlushConnectionClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            btConnectionManager.flushPackages()
        }
    }

    @MainThread
    fun onShowBurnNfcScreen() {
        chooseTrack()
        stateFor(showBurnNfcScreen).value = true
    }

    @MainThread
    fun closeBurnNfcScreen() {
        stateFor(showBurnNfcScreen).value = false
    }

    @MainThread
    fun burnNfcCartridge(playlistType: TrackInfo.PlaylistType, isShuffleEnabled: Boolean, startFromTrack: Boolean) {
        val burnString = musicControl.getBurnString(
            BurnInfo(stateFor(trackInfo).value!!, playlistType, isShuffleEnabled, startFromTrack)
        )
        with(appContext.getSharedPreferences("com.mn.mncompanion.cartridges", Context.MODE_PRIVATE).edit()) {
            putString("cart1", burnString)
            apply()
        }
    }

    @MainThread
    fun playTestPlaylist() {
        appContext.getSharedPreferences("com.mn.mncompanion.cartridges", Context.MODE_PRIVATE)
            .getString("cart1", null)?.let {
                musicControl.playMusic(it)
            }
    }

    @MainThread
    fun chooseTrack() {
        stateFor(trackInfo).value = null
        musicControl.collectCurrentTrackInfo {
            if (it == null) {
                Toast.makeText(appContext, "Invalid data from ${stateFor(chosenApp).value!!.name}", Toast.LENGTH_SHORT)
                    .show()
            }
            stateFor(trackInfo).value = it
        }
    }

    private fun <T> stateFor(publicData: LiveData<T>) = state.stateFor(publicData)

    private suspend fun connectMnDevice(device: MnBluetoothDevice) {
        withContext(Dispatchers.IO) {
            stateFor(showProgress).postValue(true)
            try {
                logd("connectMnDevice: before btConnectionManager.connectDevice")
                btConnectionManager.connectDevice(device.btDevice)
                logd("connectMnDevice: after btConnectionManager.connectDevice")
                stateFor(deviceStatus).postValue(appContext.getString(R.string.connected))

                collectMnData()

            } catch (e: Exception) {
                stateFor(deviceStatus).postValue(appContext.getString(R.string.connection_error))
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Bt error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                stateFor(showProgress).postValue(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        musicControl.close()

        collectMnDataJob?.cancel()
        collectMnDataJob = null

        btConnectionManager.finishDevice()
    }

    /**
     * We need to filter incoming data due to NFC reader behavior - even if NFC tag is not removed, it is "lost"
     * after a successful read for a ~second.
     */
    private suspend fun collectMnData(filterNfcData: Boolean = true) {
        collectMnDataJob?.cancel()
        collectMnDataJob = viewModelScope.launch(Dispatchers.IO) {
            logd("collectMnData: starting")
            try {
                val mnMessageUpdater = if (filterNfcData)
                    MnMessageSkippedUpdater(viewModelScope, stateFor(mnMessage))
                else
                    null
                while (isActive) {
                    val truePackage = MnMessage.fromBytes(btConnectionManager.readNextPackage())
                    if (truePackage.isCorrupted()) {
                        btConnectionManager.flushPackages()
                        continue
                    }

                    val nextPackage = mnMessageUpdater?.getSkippedMnMessage(truePackage) ?: truePackage

                    withContext(Dispatchers.Main) {
                        musicControl.controlAudio(nextPackage, stateFor(mnMessage).value)
                        if (nextPackage.nfcData.hasNewValidNfcData(stateFor(mnMessage).value?.nfcData))
                            playTestPlaylist()
                        stateFor(mnMessage).value = nextPackage
                    }
                }
            } catch (e: Exception) {
                logd("collectMnData: stopping")
                withContext(Dispatchers.Main) {
                    stateFor(deviceStatus).postValue(appContext.getString(R.string.connection_error))
                    Toast.makeText(appContext, "Bt error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                stateFor(mnMessage).postValue(null)
            }
        }
    }
}