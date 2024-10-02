package com.mn.mncompanion.bluetooth

import androidx.lifecycle.MutableLiveData
import com.mn.mncompanion.logd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

const val MN_PACKET_SIZE = 36

class MnMessage(
    val volume: UInt,
    val buttonUpPressed: Boolean,
    val buttonDownPressed: Boolean,
    val buttonPlayPressed: Boolean,
    val nfcData: ByteArray) {
    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromBytes(bytes: ByteArray): MnMessage {
            if (bytes.size != MN_PACKET_SIZE)
                throw IllegalArgumentException("Raw bytearray must be $MN_PACKET_SIZE bytes")

            return MnMessage(
                volume = bytes[0].toUByte().toUInt(),
                buttonUpPressed = bytes[1] == 0x01.toByte(),
                buttonDownPressed = bytes[2] == 0x01.toByte(),
                buttonPlayPressed = bytes[3] == 0x01.toByte(),
                nfcData = bytes.copyOfRange(4, MN_PACKET_SIZE)
            ).also {
                logd("New MnMessage, nfc: ${it.nfcData.toHexString()}")
            }
        }
    }
}

/**
 * Looks like a raw package from Bluetooth channel is "shifted" sometimes,
 * so a Volume value is somewhere inside nfcData, and all other nfcData bytes are 0x00
 */
fun MnMessage.isCorrupted(): Boolean {
    val isNfcDataEmpty = nfcData.all { it == 0.toByte() }
    if (isNfcDataEmpty) return false // nfcData may be all-zero

    val hasTooManyZeros = nfcData.count { it == 0.toByte() } > 16

    return hasTooManyZeros
}

fun Boolean.pressedJustNow(oldValue: Boolean?): Boolean {
    val isOldPressed = oldValue ?: false
    return this && !isOldPressed
}

fun ByteArray.hasNewValidNfcData(oldValue: ByteArray?): Boolean {
    val emptyData = ByteArray(32)
    val oldValidData = oldValue ?: emptyData

    return !this.contentEquals(oldValidData)
}

class MnMessageSkippedUpdater(
    private val scope: CoroutineScope,
    private val liveData: MutableLiveData<MnMessage?>
) {
    private val skipDelayMs = 1300L
    private val noNfcDataArray = ByteArray(32)
    private var currentValidValue = AtomicReference(noNfcDataArray)
    private var delayedUpdaterJob = AtomicReference<Job?>(null)

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getSkippedMnMessage(newMnMessage: MnMessage): MnMessage {
        if (!newMnMessage.nfcData.contentEquals(noNfcDataArray)) { // newMnMessage contains valid/error-array nfc data
            delayedUpdaterJob.getAndSet(null)?.cancel()
            with (newMnMessage.nfcData.copyOf()) {
                currentValidValue.set(this)
                logd("getSkippedMnMessage: return as is, currentValidValue = ${this.toHexString()}")
            }
            return newMnMessage

        } else { // newMnMessage contains empty nfc data
            if (currentValidValue.get().contentEquals(noNfcDataArray)) {
                logd("getSkippedMnMessage: return as is (zeros), not starting delayed zeroing")
                return newMnMessage
            } else {
                logd(
                    "getSkippedMnMessage: starting delayed zeroing, return new MnMessage " +
                            "with last currentValidValue = ${currentValidValue.get().toHexString()}"
                )
                delayedUpdaterJob.set(
                    scope.launch(Dispatchers.Main) {
                        delay(skipDelayMs)
                        currentValidValue.set(noNfcDataArray)
                        delayedUpdaterJob.set(null)
                        liveData.value?.let {
                            val mnMessage = MnMessage(
                                volume = it.volume,
                                buttonUpPressed = it.buttonUpPressed,
                                buttonDownPressed = it.buttonDownPressed,
                                buttonPlayPressed = it.buttonPlayPressed,
                                nfcData = noNfcDataArray.copyOf()
                            )
                            liveData.value = mnMessage
                        }
                    })
                return MnMessage(
                    volume = newMnMessage.volume,
                    buttonUpPressed = newMnMessage.buttonUpPressed,
                    buttonDownPressed = newMnMessage.buttonDownPressed,
                    buttonPlayPressed = newMnMessage.buttonPlayPressed,
                    nfcData = currentValidValue.get().copyOf()
                )
            }
        }
    }
}
