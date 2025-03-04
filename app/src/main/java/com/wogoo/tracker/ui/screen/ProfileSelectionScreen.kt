package com.wogoo.tracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wogoo.tracker.model.entity.UserProfile
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProfileSelectionScreen(
    profiles: StateFlow<List<UserProfile>>,
    onProfileSelected: (UserProfile) -> Unit,
    onAddProfile: (String) -> Unit
) {
    val profileList by profiles.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Criar novo perfil")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (profileList.isEmpty()) {
                Text("Nenhum perfil encontrado. Crie um novo perfil para continuar.")
            } else {
                Text("Selecione um perfil:")
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(profileList) { profile ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { onProfileSelected(profile) }
                        ) {
                            ProfileItem(profile = profile, onProfileSelected = onProfileSelected) // Use o componente ProfileItem
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddProfileDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                onAddProfile(name)
                showDialog = false
            }
        )
    }
}

@Composable
fun ProfileItem(profile: UserProfile, onProfileSelected: (UserProfile) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { onProfileSelected(profile) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )

    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Ícone do perfil",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun AddProfileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Perfil") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) errorMessage = null
                    },
                    label = { Text("Nome do perfil") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "O nome não pode ser vazio"
                    } else {
                        onConfirm(name.trim())
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}