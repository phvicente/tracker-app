package com.wogoo.tracker.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wogoo.tracker.model.entity.UserProfile
import com.wogoo.tracker.model.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    val profiles = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addProfile(name: String) {
        viewModelScope.launch {
            repository.addProfile(UserProfile(name = name))
        }
    }
}