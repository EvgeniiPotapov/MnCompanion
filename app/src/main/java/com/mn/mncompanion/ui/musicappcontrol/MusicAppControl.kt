package com.mn.mncompanion.ui.musicappcontrol

import android.content.Context
import androidx.annotation.MainThread
import com.mn.mncompanion.R
import com.mn.mncompanion.bluetooth.MnMessage
import com.mn.mncompanion.bluetooth.hasNewValidNfcData
import com.mn.mncompanion.bluetooth.pressedJustNow
import com.mn.mncompanion.isApplicationInstalled
import com.mn.mncompanion.ui.main.MusicApp
import com.mn.mncompanion.ui.musicappcontrol.poweramp.POWERAMP_PACKAGE
import java.io.Closeable

class TrackInfo(
    val title: String, // "title
    val artist: String, // "artist"
    val album: String, // "album"
    val playlistType: PlaylistType, // via cat: 30(All), 200(Album), 100(Playlist), 500(Artist) [Poweramp]
    val customPlaylistUri: String? = null // TODO: work with playlist (not "all songs" or album/artist auto list)
) {
    enum class PlaylistType(private val resourceId: Int) {
        UNKNOWN(R.string.unknown_playlist),
        ALL_SONGS(R.string.all_songs),
        ALBUM(R.string.album),
        ARTIST(R.string.all_artist_songs),
        CUSTOM(R.string.custom_playlist); // User-created

        fun asString(context: Context) = context.getString(resourceId)
    }
}

class BurnInfo(
    val trackInfo: TrackInfo,
    val playlistToBurn: TrackInfo.PlaylistType,
    val isShuffleEnabled: Boolean,
    val startFromTrack: Boolean
)

val supportedApps = listOf(
    MusicApp("Poweramp", POWERAMP_PACKAGE)
)

fun Context.getInstalledSupportedApps() = supportedApps.filter { packageManager.isApplicationInstalled(it.packageName) }

@MainThread
interface BasicMusicControl {
    fun setVolume(volume: UInt)

    fun sendPlayPauseCommand()

    fun sendNextCommand()

    fun sendPreviousCommand()

    fun controlAudio(newMnMessage: MnMessage, oldMnMessage: MnMessage?) {
        with(newMnMessage) {
            if (volume != oldMnMessage?.volume)
                setVolume(volume)

            if (buttonUpPressed.pressedJustNow(oldMnMessage?.buttonUpPressed))
                sendPreviousCommand()

            if (buttonDownPressed.pressedJustNow(oldMnMessage?.buttonDownPressed))
                sendNextCommand()

            if (buttonPlayPressed.pressedJustNow(oldMnMessage?.buttonPlayPressed))
                sendPlayPauseCommand()
        }
    }
}

@MainThread
interface AdvancedMusicControl : BasicMusicControl, Closeable {
    fun collectCurrentTrackInfo(onInfoCollected: (TrackInfo?) -> Unit)

    fun getBurnString(burnInfo: BurnInfo): String

    fun playMusic(burnString: String)
}