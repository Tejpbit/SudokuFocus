package dev.fredag.sudokufocus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenHeader(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 5.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, fontSize = 24.sp)
        Divider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colors.onBackground)
    }
}