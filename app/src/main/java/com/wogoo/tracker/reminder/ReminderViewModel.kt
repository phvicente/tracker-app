package com.wogoo.tracker.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wogoo.tracker.model.entity.Reminder
import com.wogoo.tracker.model.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    val allReminders = repository.allReminders

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insert(reminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.update(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.delete(reminder)
        }
    }

    fun getRemindersByTime(userId: Int, horario: Long): Flow<List<Reminder>> {
        return repository.getRemindersByTime(userId, horario)
    }

    fun searchReminders(query: String, userId: Int): Flow<List<Reminder>> {
        return repository.searchReminders(query, userId)
    }

    fun getAllRemindersByUser(userId: Int): Flow<List<Reminder>> {
        return repository.getAllRemindersByUser(userId)
    }

    fun addReminderWithIntervals(
        medicineName: String,
        initialTime: Long,
        repeatForever: Boolean,
        repeatDays: Int,
        repeatIntervalMode: Boolean,
        intervalHours: Int,
        numIntervals: Int,
        userId: Int
    ) {
        viewModelScope.launch {
            if (repeatIntervalMode) {
                for (i in 0 until numIntervals) {
                    val reminderTime = initialTime + i * intervalHours * 3600000L
                    val reminder = Reminder(
                        nomeRemedio = medicineName,
                        horario = reminderTime,
                        ativo = true,
                        userId = userId,
                        repeatDays = 0,
                        repeatForever = false
                    )
                    repository.insert(reminder)
                }
            } else {
                val reminder = Reminder(
                    nomeRemedio = medicineName,
                    horario = initialTime,
                    ativo = true,
                    userId = userId,
                    repeatDays = repeatDays,
                    repeatForever = repeatForever
                )
                repository.insert(reminder)
            }
        }
    }
}