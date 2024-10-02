package com.mn.mncompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mn.mncompanion.ui.theme.MNCompanionTheme

@Composable
fun KeyValueTextBox(key: String, value: String, splitValueAt: Int? = null) {
    Box(
        Modifier
            .padding(vertical = 8.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        KeyValueColumn(key, value, splitValueAt)
    }
}

@Composable
private fun KeyValueColumn(key: String, value: String, splitValueAt: Int? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)

    ) {
        Text(
            text = key,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            style = MaterialTheme.typography.titleLarge
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primaryContainer
        )

        val renderedValue =
            if (splitValueAt == null) {
                value
            } else {
                value.chunked(splitValueAt).joinToString(separator = "\n")
            }
        Text(
            text = renderedValue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ClickableKeyValueTextBox(key: String, value: String, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(vertical = 8.dp)
            .border(
                width = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp)
            ).padding(vertical = 8.dp, horizontal = 4.dp)
            .clickable { onClick() }
    ) {
        KeyValueColumn(key, value)
    }
}

@Preview(showBackground = true, locale = "ru", showSystemUi = true)
@Composable
private fun KeyValueTextBoxPreview() {
    MNCompanionTheme {
        Surface {
            Column {
                KeyValueTextBox(key = "TestKey1", value = "TestValue1")
                ClickableKeyValueTextBox(key = "TestKey2", value = "TestValue2", onClick = {})
            }
        }
    }
}