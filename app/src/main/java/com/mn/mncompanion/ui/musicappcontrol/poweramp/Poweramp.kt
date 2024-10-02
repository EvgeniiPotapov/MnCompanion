package com.mn.mncompanion.ui.musicappcontrol.poweramp

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.core.content.ContextCompat
import com.mn.mncompanion.logd
import com.mn.mncompanion.ui.musicappcontrol.AdvancedMusicControl
import com.mn.mncompanion.ui.musicappcontrol.BurnInfo
import com.mn.mncompanion.ui.musicappcontrol.TrackInfo
import com.mn.mncompanion.ui.musicappcontrol.VolumeManager

const val POWERAMP_PACKAGE = "com.maxmpz.audioplayer"

private const val CMD_INTENT_ACTION = "$POWERAMP_PACKAGE.API_COMMAND"
private const val TRACK_INFO_INTENT_FILTER = "$POWERAMP_PACKAGE.TRACK_CHANGED"
private const val SHUFFLE_SONGS = "2"

class Poweramp(private val appContext: Context) : AdvancedMusicControl {
    private val volumeManager = VolumeManager(appContext)
    private var broadcastReceiver: BroadcastReceiver? = null

    override fun setVolume(volume: UInt) {
        volumeManager.setMusicVolume(volume)
    }

    override fun sendPlayPauseCommand() {
        val intent = makeCommonIntent().putExtra("cmd", "TOGGLE_PLAY_PAUSE")
        appContext.startService(intent)
    }

    override fun sendNextCommand() {
        val intent = makeCommonIntent().putExtra("cmd", "NEXT")
        appContext.startService(intent)
    }

    override fun sendPreviousCommand() {
        val intent = makeCommonIntent().putExtra("cmd", "PREV")
        appContext.startService(intent)
    }

    override fun collectCurrentTrackInfo(onInfoCollected: (TrackInfo?) -> Unit) {
        logd("collectCurrentTrackInfo")
        removeTrackInfoReceiver()

        broadcastReceiver = makeBroadcastReceiver(onInfoCollected)

        ContextCompat.registerReceiver(
            appContext,
            broadcastReceiver!!,
            IntentFilter(TRACK_INFO_INTENT_FILTER),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun getBurnString(burnInfo: BurnInfo): String {
        val playlistUri = when(burnInfo.playlistToBurn) {
            TrackInfo.PlaylistType.ARTIST -> appContext.getArtistSongsUri(burnInfo.trackInfo.artist)
            TrackInfo.PlaylistType.ALL_SONGS -> getAllSongsUri()
            else -> TODO()
        }

        if (burnInfo.isShuffleEnabled) {
            playlistUri.buildUpon().appendQueryParameter("shf", SHUFFLE_SONGS).build()
        }
        return playlistUri.toString().also {
            logd("Poweramp::getBurnString: playlistToBurn = '${burnInfo.playlistToBurn}', " +
                    "shuffle = ${burnInfo.isShuffleEnabled}, burn string is: $it")
        }
    }

    override fun playMusic(burnString: String) {
        val intent = makeCommonIntent().putExtra("cmd", "OPEN_TO_PLAY").setData(Uri.parse(burnString))
        appContext.startService(intent)
    }

    override fun close() {
        removeTrackInfoReceiver()
    }

    private fun removeTrackInfoReceiver() {
        logd("removeTrackInfoReceiver")
        broadcastReceiver?.let {
            appContext.unregisterReceiver(it)
            broadcastReceiver = null
        }
    }

    private fun makeBroadcastReceiver(onInfoCollected: (TrackInfo?) -> Unit) = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            logd("collectCurrentTrackInfo broadcastReceiver onReceive")
            removeTrackInfoReceiver()

            val trackInfo = try {
                with(intent!!.getBundleExtra("track")!!) {
                    TrackInfo(
                        title = getString("title")!!,
                        artist = getString("artist")!!,
                        album = getString("album")!!,
                        playlistType = playlistTypeFromCategory(getInt("cat"))
                    )
                }
            } catch (e: Exception) {
                logd("Cannot get track Bundle from Poweramp intent")
                null
            }
            onInfoCollected(trackInfo)
        }
    }

    private fun makeCommonIntent() = Intent(CMD_INTENT_ACTION)
        .setPackage(POWERAMP_PACKAGE)
        .setComponent(ComponentName(POWERAMP_PACKAGE, "$POWERAMP_PACKAGE.player.PlayerService"))
}

private fun playlistTypeFromCategory(category: Int): TrackInfo.PlaylistType {
    logd("playlistTypeFromCategory: $category")
    return when(category) {
        30 -> TrackInfo.PlaylistType.ALL_SONGS
        100 -> TrackInfo.PlaylistType.CUSTOM
        200 -> TrackInfo.PlaylistType.ALBUM
        500 -> TrackInfo.PlaylistType.ARTIST
        else -> TrackInfo.PlaylistType.UNKNOWN
    }
}