package com.mn.mncompanion.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mn.mncompanion.R
import com.mn.mncompanion.logd
import com.mn.mncompanion.ui.InputCheckSheet
import com.mn.mncompanion.ui.TrackInfoSheet
import com.mn.mncompanion.ui.components.KeyValueTextBox
import com.mn.mncompanion.ui.components.OutlinedActionButton
import com.mn.mncompanion.ui.components.ProgressDialog
import com.mn.mncompanion.ui.components.SingleChoiceDialog
import com.mn.mncompanion.ui.musicappcontrol.supportedApps
import com.mn.mncompanion.ui.theme.MNCompanionTheme
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val chosenApp by viewModel.chosenApp.observeAsState(null)
    val chosenMnDevice by viewModel.chosenDevice.observeAsState(null)
    val mnDeviceStatus by viewModel.deviceStatus.observeAsState(null)

    val showProgress by viewModel.showProgress.observeAsState(false)
    if (showProgress)
        ProgressDialog()

    val availableApps by viewModel.availableApps.observeAsState(null)
    if (availableApps != null)
        AvailableAppsDialog(
            apps = availableApps!!,
            onAppChosen = viewModel::onAppChosen,
            onDismiss = viewModel::closeAppChooser
        )

    val availableMnDevices by viewModel.availableMnDevices.observeAsState(null)
    if (availableMnDevices != null)
        AvailableMnDevicesDialog(
            mnDevices = availableMnDevices!!,
            onMnDeviceChosen = viewModel::onMnDeviceChosen,
            onDismiss = viewModel::closeMnDeviceChooser)

    InputCheckBottomSheet(viewModel = viewModel)
    TrackInfoBottomSheet(viewModel = viewModel)

    MainScreen(
        chosenApp = chosenApp?.name,
        chosenMnDevice = chosenMnDevice?.asReadableString(),
        mnDeviceStatus = mnDeviceStatus,
//        showPermissionAlert = false,
        onChooseAppClick = viewModel::onChooseAppClicked,
        onChooseDeviceClick = viewModel::onChooseMnDeviceClicked,
        onCheckInputClick = viewModel::onCheckInputClicked,
        onBurnNfcClick = viewModel::onShowBurnNfcScreen
//        onDismissPermissionAlert = { /*TODO*/ }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    chosenApp: String?,
    chosenMnDevice: String?,
    mnDeviceStatus: String?,
//    showPermissionAlert: Boolean,
    onChooseAppClick: () -> Unit,
    onChooseDeviceClick: () -> Unit,
    onCheckInputClick: () -> Unit,
    onBurnNfcClick: () -> Unit
//    onDismissPermissionAlert: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MediumTopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KeyValueTextBox(
                stringResource(R.string.app_to_control),
                chosenApp ?: stringResource(R.string.not_chosen)
            )

            KeyValueTextBox(
                stringResource(R.string.control_device),
                chosenMnDevice ?: stringResource(R.string.not_chosen)
            )
            Text(
                text = mnDeviceStatus ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.End
            )

            OutlinedActionButton(
                onClick = onChooseAppClick,
                text = stringResource(if (chosenApp == null) R.string.choose_app else R.string.change_app)
            )

            OutlinedActionButton(
                onClick = onChooseDeviceClick,
                text = stringResource(if (chosenMnDevice == null) R.string.choose_device else R.string.change_device)
            )

            Spacer(Modifier.weight(1f))

            OutlinedActionButton(
                onClick = onCheckInputClick,
                text = stringResource(R.string.check_device),
                enabled = !chosenMnDevice.isNullOrBlank()
            )

            OutlinedActionButton(
                onClick = onBurnNfcClick,
                text = stringResource(R.string.burn_cartridge),
                enabled = chosenApp != null
            )

//            if (showPermissionAlert)
//                PermissionAlertDialog(onDismissOrConfirm = onDismissPermissionAlert)
        }
    }
}

@Composable
private fun AvailableAppsDialog(
    apps: List<MusicApp>,
    onAppChosen: (MusicApp) -> Unit,
    onDismiss: () -> Unit
) {
    if (apps.isEmpty()) {
        val supportedAppsList = supportedApps.joinToString("\n") { "- ${it.name}" }
        AlertDialog(
            title = { Text(stringResource(R.string.no_apps_installed_title)) },
            text = {Text(stringResource(R.string.no_apps_installed_text, supportedAppsList)) },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
        )
    } else {
        SingleChoiceDialog(
            elements = apps,
            title = { Text(stringResource(R.string.choose_app_dialog_title)) },
            onElementChosen = onAppChosen,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun AvailableMnDevicesDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputCheckBottomSheet(viewModel: MainViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val mnMessage by viewModel.mnMessage.observeAsState(null)
    val showBottomSheet by viewModel.showInputCheck.observeAsState(false)

    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        InputCheckSheet(
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        viewModel.closeInputCheck()
                    }
                }
            },
            sheetState = sheetState,
            mnMessage = mnMessage,
            onFlushConnection = viewModel::onFlushConnectionClicked
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackInfoBottomSheet(viewModel: MainViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val trackInfo by viewModel.trackInfo.observeAsState(null)
    val showBottomSheet by viewModel.showBurnNfcScreen.observeAsState(false)

    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        TrackInfoSheet(
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        viewModel.closeBurnNfcScreen()
                    }
                }
            },
            onBurnNfc = viewModel::burnNfcCartridge,
            onChooseTrack = viewModel::chooseTrack,
            sheetState = sheetState,
            trackInfo = trackInfo)
    }
}

@Preview(showBackground = true, locale = "ru")
@Composable
fun AvailableAppsDialogPreview() {
    MNCompanionTheme {
        AvailableAppsDialog(
            apps = supportedApps,
            onDismiss = {},
            onAppChosen = {}
        )
    }
}

@Preview(showBackground = true, locale = "ru", showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    MNCompanionTheme {
        MainScreen (
            chosenApp = "Poweramp",
            chosenMnDevice = "MN-ICT-1",
            mnDeviceStatus = stringResource(id = R.string.not_connected),
//            showPermissionAlert = false,
            onChooseAppClick = { },
            onChooseDeviceClick = { },
            onCheckInputClick = { },
            onBurnNfcClick = { }
//            onDismissPermissionAlert = { }
        )
    }
}