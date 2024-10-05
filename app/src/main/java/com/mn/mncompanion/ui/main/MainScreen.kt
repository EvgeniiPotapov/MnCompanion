package com.mn.mncompanion.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.mn.mncompanion.ui.checkInput.CheckInputSheet
import com.mn.mncompanion.ui.TrackInfoSheet
import com.mn.mncompanion.ui.components.KeyValueTextBox
import com.mn.mncompanion.ui.components.OutlinedActionButton
import com.mn.mncompanion.ui.components.ProgressDialog
import com.mn.mncompanion.ui.main.dialogs.AttachNfcDialog
import com.mn.mncompanion.ui.main.dialogs.AvailableAppsDialog
import com.mn.mncompanion.ui.main.dialogs.AvailableMnDevicesDialog
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
            supportedApps = supportedApps.map { it.name },
            availableApps = availableApps!!,
            onAppChosen = viewModel::onAppChosen,
            onDismiss = viewModel::closeAppChooser
        )

    val availableMnDevices by viewModel.availableMnDevices.observeAsState(null)
    if (availableMnDevices != null)
        AvailableMnDevicesDialog(
            mnDevices = availableMnDevices!!,
            onMnDeviceChosen = viewModel::onMnDeviceChosen,
            onDismiss = viewModel::closeMnDeviceChooser)

    AttachNfcDialog(viewModel = viewModel)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputCheckBottomSheet(viewModel: MainViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val mnMessage by viewModel.mnMessage.observeAsState(null)
    val showBottomSheet by viewModel.showInputCheck.observeAsState(false)

    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        CheckInputSheet(
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

@Composable
private fun AttachNfcDialog(viewModel: MainViewModel) {
    val showDialog by viewModel.showAttachNfcDialog.observeAsState(false)
    if (showDialog)
        AttachNfcDialog(onDismiss = viewModel::closeAttachNfcDialog)
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