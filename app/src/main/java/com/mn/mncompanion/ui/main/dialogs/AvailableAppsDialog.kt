package com.mn.mncompanion.ui.main.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mn.mncompanion.R
import com.mn.mncompanion.ui.components.SingleChoiceDialog
import com.mn.mncompanion.ui.main.MusicApp
import com.mn.mncompanion.ui.musicappcontrol.supportedApps
import com.mn.mncompanion.ui.theme.MNCompanionTheme

@Composable
fun AvailableAppsDialog(
    supportedApps: List<String>,
    availableApps: List<MusicApp>,
    onAppChosen: (MusicApp) -> Unit,
    onDismiss: () -> Unit
) {
    if (availableApps.isEmpty()) {
        val supportedAppsList = supportedApps.joinToString("\n") { "- $it" }
        AlertDialog(
            title = { Text(stringResource(R.string.no_apps_installed_title)) },
            text = { Text(stringResource(R.string.no_apps_installed_text, supportedAppsList)) },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
        )
    } else {
        SingleChoiceDialog(
            elements = availableApps,
            title = { Text(stringResource(R.string.choose_app_dialog_title)) },
            onElementChosen = onAppChosen,
            onDismiss = onDismiss
        )
    }
}


@Preview(showBackground = true, locale = "ru")
@Composable
fun AvailableAppsDialogPreview() {
    MNCompanionTheme {
        AvailableAppsDialog(
            supportedApps = listOf("Poweramp"),
            availableApps = supportedApps,
            onDismiss = {},
            onAppChosen = {}
        )
    }
}