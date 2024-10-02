package com.mn.mncompanion.ui.main

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mn.mncompanion.nfc.enableUltralightForegroundDispatch
import com.mn.mncompanion.nfc.getUltralightTag
import com.mn.mncompanion.ui.theme.MNCompanionTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private var nfcTag: MifareUltralight? = null

    public override fun onPause() {
        super.onPause()
        nfcTag = null
        nfcAdapter.disableForegroundDispatch(this)
    }

    public override fun onResume() {
        super.onResume()
        nfcAdapter = NfcAdapter.getDefaultAdapter(this).also {
            it.enableUltralightForegroundDispatch(this)
        }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nfcTag = intent.getUltralightTag()
        if (nfcTag != null)
            Toast.makeText(this, "New NFC Tag", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        enableEdgeToEdge()
        setContent {
            MNCompanionTheme {
                val viewModel = koinViewModel<MainViewModel>()

                MainScreen(viewModel = viewModel)
            }
        }
    }
}