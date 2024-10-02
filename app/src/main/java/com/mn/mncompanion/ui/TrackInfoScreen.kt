package com.mn.mncompanion.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mn.mncompanion.R
import com.mn.mncompanion.ui.components.ClickableKeyValueTextBox
import com.mn.mncompanion.ui.components.OutlinedActionButton
import com.mn.mncompanion.ui.musicappcontrol.TrackInfo
import com.mn.mncompanion.ui.musicappcontrol.TrackInfo.PlaylistType.*
import com.mn.mncompanion.ui.theme.MNCompanionTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackInfoSheet(
    onDismiss: () -> Unit,
    onBurnNfc: (playlistType: TrackInfo.PlaylistType, isShuffleEnabled: Boolean, startFromTrack: Boolean) -> Unit,
    onChooseTrack: () -> Unit,
    sheetState: SheetState,
    trackInfo: TrackInfo?
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        TrackInfoScreen(trackInfo = trackInfo, onBurnNfc = onBurnNfc, onChooseTrack = onChooseTrack)
    }

    if (trackInfo == null) {
        AlertDialog(
            title = { Text(stringResource(R.string.no_track_info_title)) },
            text = { Text(stringResource(R.string.no_track_info_text)) },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TrackInfoScreen(
    trackInfo: TrackInfo?,
    onBurnNfc: (playlistType: TrackInfo.PlaylistType, isShuffleEnabled: Boolean, startFromTrack: Boolean) -> Unit,
    onChooseTrack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { MediumTopAppBar(title = { Text(stringResource(R.string.burn_cartridge)) }) }
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
                Text(
                    stringResource(R.string.burn_nfc_help_text),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge
                )

                var isShuffleEnabled by remember { mutableStateOf(true) }
                var isStartFromTrack by remember { mutableStateOf(false) }

                ClickableKeyValueTextBox(stringResource(R.string.title), trackInfo?.title.toRenderString()) {
                    if (trackInfo != null) onBurnNfc(ALL_SONGS, isShuffleEnabled, isStartFromTrack)
                }
                ClickableKeyValueTextBox(stringResource(R.string.artist), trackInfo?.artist.toRenderString()) {
                    if (trackInfo != null) onBurnNfc(ARTIST, isShuffleEnabled, isStartFromTrack)
                }
                ClickableKeyValueTextBox(stringResource(R.string.album), trackInfo?.album.toRenderString()) {
                    if (trackInfo != null) onBurnNfc(ALBUM, isShuffleEnabled, isStartFromTrack)
                }
                ClickableKeyValueTextBox(
                    stringResource(R.string.playlist),
                    trackInfo?.playlistType?.asString(LocalContext.current).toRenderString()
                ) {
                    if (trackInfo != null) onBurnNfc(CUSTOM, isShuffleEnabled, isStartFromTrack)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.shuffle_tracks),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Switch(checked = isShuffleEnabled, onCheckedChange = { isShuffleEnabled = it })
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.start_from_track),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Switch(checked = isStartFromTrack, onCheckedChange = { isStartFromTrack = it })
                }

                Spacer(Modifier.weight(1f))

                OutlinedActionButton(
                    onClick = onChooseTrack,
                    text = stringResource(R.string.choose_another_track))
            }
        }
    }
}


@Composable
private fun String?.toRenderString() = this ?: stringResource(R.string.no_data)

@Preview(showSystemUi = true, showBackground = true, locale = "ru")
@Composable
fun TrackInfoScreenPreview() {
    MNCompanionTheme {
        TrackInfoScreen(null, {_,_,_ -> }, {})
    }
}

@Preview(showSystemUi = true, showBackground = true, locale = "ru")
@Composable
fun TrackInfoScreenPreview2() {
    MNCompanionTheme {
        TrackInfoScreen(
            TrackInfo(
                title = "I want to break free",
                artist = "Queen",
                album = "The Works",
                playlistType = TrackInfo.PlaylistType.ALL_SONGS
            ),
            {_,_,_ -> },
            {}
        )
    }
}