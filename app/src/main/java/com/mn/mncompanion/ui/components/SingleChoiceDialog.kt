package com.mn.mncompanion.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable

fun interface ReadableString {
    fun asReadableString(): String
}

@Composable
fun <T: ReadableString> SingleChoiceDialog(
    elements: List<T>,
    title:  @Composable (() -> Unit),
    onElementChosen: (T) -> Unit,
    onDismiss: () -> Unit
) {
    if (elements.isEmpty())
        throw IllegalArgumentException("Elements cannot be empty")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { },
        title = title,
        text = {
            Column(verticalArrangement = Arrangement.Center) {
                elements.forEach {
                    OutlinedActionButton(
                        onClick = { onElementChosen(it) },
                        text = it.asReadableString()
                    )
                }
            }
        }
    )
}