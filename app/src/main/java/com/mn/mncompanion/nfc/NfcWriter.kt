package com.mn.mncompanion.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

fun NfcAdapter.enableUltralightForegroundDispatch(activity: Activity) {
    val intent = Intent(activity, activity.javaClass).apply { addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) }
    val pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_MUTABLE)
    val ultralightFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
    val techList = arrayOf(arrayOf(MifareUltralight::class.java.name))

    enableForegroundDispatch(activity, pendingIntent, arrayOf(ultralightFilter), techList)
}

fun Intent.getUltralightTag(): MifareUltralight? {
    try {
        val tagFromIntent: Tag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)!!
            else
                getParcelableExtra(NfcAdapter.EXTRA_TAG)!!

        return MifareUltralight.get(tagFromIntent)
    } catch (_: Exception) {
        return null
    }
}

suspend fun MifareUltralight.writeTextHash(text: String) {
    if (text.isBlank() || text.isEmpty())
        throw IllegalArgumentException("Text cannot be empty")

    withContext(Dispatchers.IO) {
        val sha256Hash = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        writeDataToTag(sha256Hash)
    }
}

/**
 * Writes given bytearray to a Mifare Ultralight Tag.
 * @param data data to write. MUST be equal or less than 32 bytes, as data is written to fixed position on Tag
 */
suspend fun MifareUltralight.writeDataToTag(data: ByteArray) {
    if (data.size > 32)
        throw IllegalArgumentException("Cannot write more than 32 Bytes ${data.size} Bytes are given")

    withContext(Dispatchers.IO) {
        val dataToWrite = ByteArray(32).apply {
            data.copyInto(this)
        }

        val firstPageIndex = 4
        val pageSizeBytes = 4
        val pagesRequired = 8 // 32 Bytes == 8 pages of 4 bytes each

        use {
            connect()

            for (index in 0..<pagesRequired) {
                val pageIndex = firstPageIndex + index
                val pageData = dataToWrite.copyOfRange(index * pageSizeBytes, index * pageSizeBytes + pageSizeBytes)
                writePage(pageIndex, pageData)
            }

            // Checking if data is written correctly
            val dataFromTag = byteArrayOf() +
                    readPages(firstPageIndex) + //reads 4 pages at once
                    readPages(firstPageIndex + 4)

            if (!dataFromTag.contentEquals(dataToWrite))
                throw RuntimeException("Data from tag is not equal to original")
        }
    }
}