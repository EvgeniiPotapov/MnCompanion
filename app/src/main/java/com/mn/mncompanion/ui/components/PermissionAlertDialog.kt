package com.mn.mncompanion.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mn.mncompanion.R

@Composable
fun PermissionAlertDialog(onDismissOrConfirm: () -> Unit) {
    AlertDialog(
        title = { Text(stringResource(R.string.request_permission_title)) },
        text = { Text(stringResource(R.string.request_permission_text)) },
        onDismissRequest = onDismissOrConfirm,
        confirmButton = {
            TextButton(onClick = onDismissOrConfirm) { Text(stringResource(R.string.close)) }
        }
    )
}


@Preview(showBackground = true, locale = "ru")
@Composable
private fun PermissionAlertDialogPreview() {
    PermissionAlertDialog { }
}