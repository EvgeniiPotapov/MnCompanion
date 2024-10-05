package com.mn.mncompanion.ui.main

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mn.mncompanion.logd
import com.mn.mncompanion.nfc.UltralightHolder
import com.mn.mncompanion.nfc.enableUltralightForegroundDispatch
import com.mn.mncompanion.nfc.getUltralightTag
import com.mn.mncompanion.ui.theme.MNCompanionTheme
import org.koin.android.ext.android.get
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private val ultralightHolder: UltralightHolder = get()

    public override fun onPause() {
        super.onPause()
        ultralightHolder.set(null)
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
        with(intent.getUltralightTag()) {
            logd("onNewIntent, getUltralightTag is $this")
            ultralightHolder.set(this)
        }
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