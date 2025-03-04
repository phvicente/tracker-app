package com.wogoo.tracker.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.wogoo.tracker.model.entity.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlarmScreen(
    modifier: Modifier = Modifier,
    userName: String,
    reminders: List<Reminder>,
    onCreateReminderClicked: () -> Unit,
    onEditReminderClicked: (Reminder) -> Unit,
    onDeleteReminderClicked: (Reminder) -> Unit,
    onReportClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Olá, $userName!", style = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateReminderClicked, modifier = Modifier.fillMaxWidth()) {
            Text("Criar Lembrete")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Lembretes Salvos:", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (reminders.isEmpty()) {
            Text("Nenhum lembrete salvo.", style = MaterialTheme.typography.bodyMedium)
        } else {
            val groupedReminders = reminders.groupBy { it.horario }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                groupedReminders.forEach { (time, remindersForTime) ->
                    item {
                        ExpandableReminderCard(
                            time = time,
                            reminders = remindersForTime,
                            onEditClicked = onEditReminderClicked,
                            onDeleteClicked = onDeleteReminderClicked
                        )
                    }
                }
            }
    }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onReportClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver Relatório")
        }
}
}

@Composable
fun ExpandableReminderCard(
    time: Long,
    reminders: List<Reminder>,
    onEditClicked: (Reminder) -> Unit,
    onDeleteClicked: (Reminder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val formattedTime = formatTime(time)
                Text(
                    text = "Horário: $formattedTime",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.rotate(rotationState)
                )
                {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand/Collapse"
                    )
                }

            }


            AnimatedVisibility(visible = expanded) {
                Column {
                    reminders.forEach { reminder ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = reminder.nomeRemedio,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (reminder.ativo) "Ativo" else "Inativo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (reminder.ativo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { onEditClicked(reminder) }) {
                                    Text("Editar")
                                }
                                TextButton(onClick = { onDeleteClicked(reminder) }) {
                                    Text("Excluir")
                                }
                            }
                            Divider()
                        }

                    }
                }
            }
        }
    }
}

private fun formatTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}