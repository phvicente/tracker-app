package com.wogoo.tracker.model.repository

import com.wogoo.tracker.model.dao.UserProfileDao
import com.wogoo.tracker.model.entity.UserProfile
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    val allProfiles: Flow<List<UserProfile>> = userProfileDao.getAllProfiles()

    suspend fun addProfile(userProfile: UserProfile) {
        userProfileDao.insertProfile(userProfile)
    }
}