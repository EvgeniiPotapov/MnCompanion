package com.mn.mncompanion.ui.musicappcontrol.poweramp

import android.content.Context
import android.net.Uri

private val ROOT_URI = Uri.Builder().scheme("content").authority("com.maxmpz.audioplayer.data").build()
private val ALL_SONGS_URI = ROOT_URI.buildUpon().appendEncodedPath("files").build()
private val ARTISTS_URI = ROOT_URI.buildUpon().appendEncodedPath("artists").build()
private val ALBUMS_BY_ARTISTS_URI = ROOT_URI.buildUpon().appendEncodedPath("artists_albums").build()

fun Context.getArtistSongsUri(artist: String): Uri {
    return ARTISTS_URI.buildUpon().appendEncodedPath(getArtistId(artist).toString()).appendEncodedPath("files").build()
}

fun getAllSongsUri() = ALL_SONGS_URI

private fun Context.getArtistId(artistName: String): Long = contentResolver.query(
    ARTISTS_URI,
    arrayOf("artists._id", "artist"),
    "artist=?",
    arrayOf(artistName),
    null
)!!.use {
    it.moveToNext()
    return@use it.getLong(0)
}