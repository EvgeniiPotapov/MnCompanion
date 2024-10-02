package com.mn.mncompanion.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mn.mncompanion.R
import com.mn.mncompanion.bluetooth.MnMessage
import com.mn.mncompanion.ui.components.KeyValueTextBox
import com.mn.mncompanion.ui.components.OutlinedActionButton
import com.mn.mncompanion.ui.theme.MNCompanionTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputCheckSheet(
    onDismiss: () -> Unit,
    onFlushConnection: () -> Unit,
    sheetState: SheetState,
    mnMessage: MnMessage?
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
       InputCheckScreen(mnMessage = mnMessage, onFlushConnection = onFlushConnection)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class, ExperimentalFoundationApi::class)
@Composable
private fun InputCheckScreen(mnMessage: MnMessage?, onFlushConnection: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MediumTopAppBar(title = { Text(stringResource(R.string.input_data_check)) }) }
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalOverscrollConfiguration provides null
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                KeyValueTextBox(
                    stringResource(R.string.volume),
                    mnMessage?.volume?.toString() ?: stringResource(R.string.no_data)
                )
                KeyValueTextBox(stringResource(R.string.button_up), buttonStateToString(mnMessage?.buttonUpPressed))
                KeyValueTextBox(stringResource(R.string.button_down), buttonStateToString(mnMessage?.buttonDownPressed))
                KeyValueTextBox(stringResource(R.string.button_play), buttonStateToString(mnMessage?.buttonPlayPressed))
                KeyValueTextBox(
                    stringResource(R.string.cartridge),
                    mnMessage?.nfcData?.toHexString() ?: stringResource(R.string.no_data), 16
                )

                OutlinedActionButton(
                    onClick = onFlushConnection,
                    text = stringResource(R.string.flush_connection)
                )
            }
        }
    }
}

@Composable
private fun buttonStateToString(state: Boolean?): String {
    return when(state) {
        null -> stringResource(R.string.no_data)
        true -> stringResource(R.string.pressed)
        false -> stringResource(R.string.released)
    }
}

@Preview(showSystemUi = true, showBackground = true, locale = "ru")
@Composable
fun InputCheckScreenPreview() {
    MNCompanionTheme {
        InputCheckScreen(null, { })
    }
}

@Preview(showSystemUi = true, showBackground = true, locale = "ru")
@Composable
fun InputCheckScreenPreview2() {
    MNCompanionTheme {
        InputCheckScreen(
            MnMessage(
                volume = 22.toUInt(),
                buttonUpPressed = true,
                buttonDownPressed = false,
                buttonPlayPressed = false,
                nfcData = ByteArray(36).apply { fill(0x08) }

            ),
            { }
        )
    }
}