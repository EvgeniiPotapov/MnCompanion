package com.mn.mncompanion.nfc

import android.nfc.tech.MifareUltralight
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NfcWriterViewModel : ViewModel() {
    private val _messageText = MutableLiveData("")
    val messageText: LiveData<String> get() = _messageText

    @MainThread
    fun writeTextHashToNfcTag(text: String, ultralight: MifareUltralight?) {
        _messageText.value = ""

        if (ultralight == null) {
            _messageText.value = "NFC tag not found"
            return
        }

        viewModelScope.launch {
            try {
                ultralight.writeTextHash(text)
            } catch (e: Exception) {
                _messageText.value = e.message
            }
        }
    }
}