package com.mn.mncompanion.ui.main.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mn.mncompanion.R

@Composable
fun AttachNfcDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text(stringResource(R.string.attach_nfc_title)) },
        text = { Text(stringResource(R.string.attach_nfc_text)) },
        onDismissRequest = { /* restrict to dismiss other than with button click */ },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}