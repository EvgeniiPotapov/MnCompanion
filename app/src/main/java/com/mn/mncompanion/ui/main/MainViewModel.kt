package com.mn.mncompanion.ui.main

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mn.mncompanion.R
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

    private val _showProgress: MutableLiveData<Boolean> = MutableLiveData(false)
    val showProgress: LiveData<Boolean> get() = _showProgress

    private val _chosenApp: MutableLiveData<MusicApp?> = MutableLiveData(null)
    val chosenApp: LiveData<MusicApp?> get() = _chosenApp

    private val _chosenDevice: MutableLiveData<MnBluetoothDevice?> = MutableLiveData(null)
    val chosenDevice: LiveData<MnBluetoothDevice?> get() = _chosenDevice

    private val _deviceStatus: MutableLiveData<String?> = MutableLiveData(null)
    val deviceStatus: LiveData<String?> get() = _deviceStatus

    private val _availableApps: MutableLiveData<List<MusicApp>?> = MutableLiveData(null)
    val availableApps: LiveData<List<MusicApp>?> get() = _availableApps

    private val _availableMnDevices: MutableLiveData<List<MnBluetoothDevice>?> = MutableLiveData(null)
    val availableMnDevices: LiveData<List<MnBluetoothDevice>?> get() = _availableMnDevices

    private val _showInputCheck: MutableLiveData<Boolean> = MutableLiveData(false)
    val showInputCheck: LiveData<Boolean> get() = _showInputCheck

    private val _mnMessage: MutableLiveData<MnMessage?> = MutableLiveData(null)
    val mnMessage: LiveData<MnMessage?> get() = _mnMessage

    private val _trackInfo: MutableLiveData<TrackInfo?> = MutableLiveData(null)
    val trackInfo: LiveData<TrackInfo?> get() = _trackInfo

    private val _showBurnNfcScreen: MutableLiveData<Boolean> = MutableLiveData(false)
    val showBurnNfcScreen: LiveData<Boolean> get() = _showBurnNfcScreen

    private val musicControl: AdvancedMusicControl = Poweramp(appContext)

    @MainThread
    fun onChooseAppClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            _availableApps.postValue(
                appContext.getInstalledSupportedApps()
            )
        }
    }

    @MainThread
    fun onAppChosen(app: MusicApp) {
        _chosenApp.value = app
        closeAppChooser()
    }

    @MainThread
    fun closeAppChooser() {
        _availableApps.value = null
    }

    @MainThread
    fun onChooseMnDeviceClicked() {
        viewModelScope.launch(Dispatchers.IO) {
            _availableMnDevices.postValue(
                btConnectionManager.getBondedMNs().map { MnBluetoothDevice(it) }
            )
        }
    }

    @MainThread
    fun onMnDeviceChosen(device: MnBluetoothDevice) {
        _chosenDevice.value = device
        _deviceStatus.value = null
        closeMnDeviceChooser()
       viewModelScope.launch { connectMnDevice(device) }
    }

    @MainThread
    fun closeMnDeviceChooser() {
        _availableMnDevices.value = null
    }

    @MainThread
    fun onCheckInputClicked() {
        _showInputCheck.value = true
    }

    @MainThread
    fun closeInputCheck() {
        _showInputCheck.value = false
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
        _showBurnNfcScreen.value = true
    }

    @MainThread
    fun closeBurnNfcScreen() {
        _showBurnNfcScreen.value = false
    }

    @MainThread
    fun burnNfcCartridge(playlistType: TrackInfo.PlaylistType, isShuffleEnabled: Boolean, startFromTrack: Boolean) {
        val burnString =
            musicControl.getBurnString(BurnInfo(_trackInfo.value!!, playlistType, isShuffleEnabled, startFromTrack))
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
        _trackInfo.value = null
        musicControl.collectCurrentTrackInfo {
            if (it == null) {
                Toast.makeText(appContext, "Invalid data from ${_chosenApp.value!!.name}", Toast.LENGTH_SHORT).show()
            }
            _trackInfo.value = it
        }
    }

    private suspend fun connectMnDevice(device: MnBluetoothDevice) {
        withContext(Dispatchers.IO) {
            _showProgress.postValue(true)
            try {
                logd("connectMnDevice: before btConnectionManager.connectDevice")
                btConnectionManager.connectDevice(device.btDevice)
                logd("connectMnDevice: after btConnectionManager.connectDevice")
                _deviceStatus.postValue(appContext.getString(R.string.connected))

                collectMnData()

            } catch (e: Exception) {
                _deviceStatus.postValue(appContext.getString(R.string.connection_error))
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Bt error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _showProgress.postValue(false)
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
                val mnMessageUpdater = if (filterNfcData) MnMessageSkippedUpdater(viewModelScope, _mnMessage) else null
                while (isActive) {
                    val truePackage = MnMessage.fromBytes(btConnectionManager.readNextPackage())
                    if (truePackage.isCorrupted()) {
                        btConnectionManager.flushPackages()
                        continue
                    }

                    val nextPackage = mnMessageUpdater?.getSkippedMnMessage(truePackage) ?: truePackage

                    withContext(Dispatchers.Main) {
                        musicControl.controlAudio(nextPackage, _mnMessage.value)
                        if (nextPackage.nfcData.hasNewValidNfcData(_mnMessage.value?.nfcData))
                            playTestPlaylist()
                        _mnMessage.value = nextPackage
                    }
                }
            } catch (e: Exception) {
                logd("collectMnData: stopping")
                withContext(Dispatchers.Main) {
                    _deviceStatus.postValue(appContext.getString(R.string.connection_error))
                    Toast.makeText(appContext, "Bt error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                _mnMessage.postValue(null)
            }
        }
    }
}