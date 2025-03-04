package com.wogoo.tracker.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.wogoo.tracker.profile.UserProfileViewModel
import com.wogoo.tracker.ui.screen.ProfileSelectionScreen
import com.wogoo.tracker.ui.theme.TrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import com.wogoo.tracker.ui.activities.MainActivity

@AndroidEntryPoint
class StartActivity : ComponentActivity() {
    private val userProfileViewModel: UserProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrackerTheme {
                ProfileSelectionScreen(
                    profiles = userProfileViewModel.profiles,
                    onProfileSelected = { profile ->
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("USER_NAME", profile.name)
                            putExtra("USER_ID", profile.id)
                        }
                        startActivity(intent)
                    },
                    onAddProfile = { name ->
                        userProfileViewModel.addProfile(name)
                    }
                )
            }
        }
    }
}
