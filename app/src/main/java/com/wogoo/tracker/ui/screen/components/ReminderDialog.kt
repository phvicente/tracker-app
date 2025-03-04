package com.wogoo.tracker.ui.screen.components

import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Calendar

@Composable
fun ReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        medicineName: String,
        timeMillis: Long,
        repeatForever: Boolean,
        repeatDays: Int,
        repeatIntervalMode: Boolean,
        intervalHours: Int,
        numIntervals: Int
    ) -> Unit,
    initialMedicineName: String = "",
    initialTimeMillis: Long? = null,
    initialRepeatForever: Boolean = false,
    initialRepeatDays: Int = 0
) {

    var medicineName by remember { mutableStateOf(initialMedicineName) }
    var timeInMillis by remember { mutableStateOf(initialTimeMillis ?: System.currentTimeMillis()) }
    var timeText by remember { mutableStateOf("") }
    var repeatForever by remember { mutableStateOf(initialRepeatForever) }
    var repeatDaysText by remember { mutableStateOf(if (initialRepeatDays > 0) initialRepeatDays.toString() else "") }

    var repeatIntervalMode by remember { mutableStateOf(false) }
    var intervalHoursText by remember { mutableStateOf("") }
    var numIntervalsText by remember { mutableStateOf("") }

    LaunchedEffect(timeInMillis) {
        val calendar = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
        timeText = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text
                    medicineName = recognizedText
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Falha ao reconhecer texto", Toast.LENGTH_SHORT).show()
                }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialMedicineName.isEmpty()) "Novo Reminder" else "Editar Reminder") },
        text = {
            Column {
                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text("Nome do Remédio") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (medicineName.isEmpty()) {
                            IconButton(
                                onClick = {
                                    cameraLauncher.launch(null)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "Abrir câmera",
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Horário: $timeText",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val newCalendar = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    timeInMillis = newCalendar.timeInMillis
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tocar até ser cancelado")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = repeatForever,
                        onCheckedChange = { repeatForever = it }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!repeatForever && !repeatIntervalMode) {
                    OutlinedTextField(
                        value = repeatDaysText,
                        onValueChange = { repeatDaysText = it },
                        label = { Text("Repetir por quantos dias") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Repetir em intervalos")
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(
                        checked = repeatIntervalMode,
                        onCheckedChange = { repeatIntervalMode = it }
                    )
                }
                if (repeatIntervalMode) {
                    OutlinedTextField(
                        value = intervalHoursText,
                        onValueChange = { intervalHoursText = it },
                        label = { Text("Intervalo (horas)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = numIntervalsText,
                        onValueChange = { numIntervalsText = it },
                        label = { Text("Número de intervalos") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (medicineName.isNotBlank()) {
                        val days = repeatDaysText.toIntOrNull() ?: 0
                        val intervalHours = intervalHoursText.toIntOrNull() ?: 0
                        val numIntervals = numIntervalsText.toIntOrNull() ?: 0
                        onConfirm(
                            medicineName,
                            timeInMillis,
                            repeatForever,
                            days,
                            repeatIntervalMode,
                            intervalHours,
                            numIntervals
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Confirmar", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}