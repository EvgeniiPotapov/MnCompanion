package com.mn.mncompanion.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mn.mncompanion.ui.theme.MNCompanionTheme

@Composable
fun OutlinedActionButton(onClick: () -> Unit, text: String, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}


@Preview(showBackground = true)
@Composable
fun OutlinedActionButtonPreview() {
    MNCompanionTheme {
        OutlinedActionButton({}, "make action")
    }
}