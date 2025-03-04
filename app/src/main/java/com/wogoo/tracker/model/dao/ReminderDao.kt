package com.wogoo.tracker.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wogoo.tracker.model.entity.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY horario ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY horario ASC")
    fun getAllRemindersByUser(userId: Int): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND nomeRemedio LIKE '%' || :query || '%' ORDER BY horario ASC")
    fun searchReminders(query: String, userId: Int): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND horario = :horario")
    fun getRemindersByTime(userId: Int, horario: Long): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}