package com.wogoo.tracker.ui.screen.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.wogoo.tracker.model.entity.Reminder
import com.wogoo.tracker.utils.createAndDownloadPdf

@Composable
fun GeneratePdfButton(reminders: List<Reminder>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Button(
        onClick = { createAndDownloadPdf(context, reminders) },
        modifier = modifier
    ) {
        Text("Gerar PDF")
    }
}