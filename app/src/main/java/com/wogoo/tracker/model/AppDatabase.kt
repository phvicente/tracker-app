package com.wogoo.tracker.model

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wogoo.tracker.model.dao.ReminderDao
import com.wogoo.tracker.model.dao.UserProfileDao
import com.wogoo.tracker.model.entity.Reminder
import com.wogoo.tracker.model.entity.UserProfile

@Database(entities = [Reminder::class, UserProfile::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun userProfileDao(): UserProfileDao
}
