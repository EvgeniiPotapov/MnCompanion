package com.mn.mncompanion.ui.main.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mn.mncompanion.R
import com.mn.mncompanion.ui.components.SingleChoiceDialog
import com.mn.mncompanion.ui.main.MnBluetoothDevice

@Composable
fun AvailableMnDevicesDialog(
    mnDevices: List<MnBluetoothDevice>,
    onMnDeviceChosen: (MnBluetoothDevice) -> Unit,
    onDismiss: () -> Unit
) {
    if (mnDevices.isEmpty()) {
        AlertDialog(
            title = { Text(stringResource(R.string.mn_devices_not_found_title)) },
            text = { Text(stringResource(R.string.mn_devices_not_found_text)) },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
        )
    } else {
        SingleChoiceDialog(
            elements = mnDevices,
            title = { Text(stringResource(R.string.choose_device)) },
            onElementChosen = onMnDeviceChosen,
            onDismiss = onDismiss
        )
    }
}