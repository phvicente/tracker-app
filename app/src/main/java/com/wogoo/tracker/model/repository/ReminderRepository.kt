package com.wogoo.tracker.model.repository

import com.wogoo.tracker.model.dao.ReminderDao
import com.wogoo.tracker.model.entity.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {

    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun insert(reminder: Reminder) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun update(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun delete(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }
    fun getRemindersByTime(userId: Int, horario: Long): Flow<List<Reminder>> {
        return reminderDao.getRemindersByTime(userId, horario)
    }
    fun getAllRemindersByUser(userId: Int): Flow<List<Reminder>> {
        return reminderDao.getAllRemindersByUser(userId)
    }

    fun searchReminders(query: String, userId: Int): Flow<List<Reminder>> {
        return reminderDao.searchReminders(query, userId)
    }
}