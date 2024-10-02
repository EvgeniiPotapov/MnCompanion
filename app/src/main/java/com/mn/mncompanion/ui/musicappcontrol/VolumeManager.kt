package com.mn.mncompanion.ui.musicappcontrol

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC

private const val MAX_MN_VOLUME = 255 // MN device sends volume value in one byte

class VolumeManager(appContext: Context) {
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val volumeMultiplier = MAX_MN_VOLUME / audioManager.getStreamMaxVolume(STREAM_MUSIC)

    fun setMusicVolume(mnVolume: UInt) {
        val musicVolume = (mnVolume.toInt() + 1) / volumeMultiplier
        audioManager.setStreamVolume(STREAM_MUSIC, musicVolume, 0)
    }
}