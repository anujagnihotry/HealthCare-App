package com.healthcare.app.ui.doctor.queue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.*
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.TokenRepository
import com.healthcare.app.ui.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QueueState(
    val isLoading: Boolean = true,
    val queue: List<TokenDto> = emptyList(),
    val currentToken: Int? = null,
    val error: String? = null,
)

@HiltViewModel
class TokenQueueViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val locationId: String = savedStateHandle["locationId"] ?: ""
    val date: String = savedStateHandle["date"] ?: ""

    private val _state = MutableStateFlow(QueueState())
    val state: StateFlow<QueueState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = tokenRepository.getTokenQueue(locationId, date)
            when (result) {
                is Resource.Success -> {
                    val serving = result.data.find { it.status == "serving" }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        queue = result.data,
                        currentToken = serving?.tokenNumber,
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun startServing() {
        viewModelScope.launch {
            tokenRepository.startServing(locationId, date)
            load()
        }
    }

    fun advanceToken() {
        viewModelScope.launch {
            tokenRepository.advanceToken(locationId, date)
            load()
        }
    }

    fun assignOffline(patientName: String) {
        viewModelScope.launch {
            tokenRepository.assignOfflineToken(
                AssignOfflineTokenRequest(locationId, date, patientName)
            )
            load()
        }
    }

    fun markArrival(tokenId: String, arrived: Boolean) {
        viewModelScope.launch {
            tokenRepository.markArrival(tokenId, arrived)
            load()
        }
    }

    fun skipToken(tokenId: String) {
        viewModelScope.launch {
            tokenRepository.skipToken(tokenId)
            load()
        }
    }
}

@Composable
fun TokenQueueScreen(
    locationId: String,
    date: String,
    onBack: () -> Unit,
    onOpenPatientProfile: (familyMemberId: String, displayName: String) -> Unit = { _, _ -> },
    onAddConsultation: (familyMemberId: String, patientId: String, appointmentId: String?) -> Unit =
        { _, _, _ -> },
    viewModel: TokenQueueViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showOfflineDialog by remember { mutableStateOf(false) }

    if (showOfflineDialog) {
        OfflineTokenDialog(
            onDismiss = { showOfflineDialog = false },
            onAssign = { name ->
                viewModel.assignOffline(name)
                showOfflineDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Token Queue",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Current token banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Currently Serving", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (state.currentToken != null) "Token ${state.currentToken}" else "Not Started",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            // Control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.currentToken == null) {
                    Button(
                        onClick = { viewModel.startServing() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start")
                    }
                } else {
                    Button(
                        onClick = { viewModel.advanceToken() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Next")
                    }
                }

                OutlinedButton(
                    onClick = { showOfflineDialog = true },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Walk-in")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Token list
            Text(
                "Queue (${state.queue.size} tokens)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.queue) { token ->
                    val apt = token.appointment
                    val fm = apt?.familyMember
                    val canPatientTools = fm != null && apt != null
                    val displayName =
                        token.patientName ?: fm?.name ?: "Patient"
                    TokenCard(
                        token = token,
                        onMarkArrival = { viewModel.markArrival(token.id, !token.hasArrived) },
                        onSkip = { viewModel.skipToken(token.id) },
                        canPatientTools = canPatientTools,
                        onOpenProfile = {
                            if (fm != null) onOpenPatientProfile(fm.id, displayName)
                        },
                        onAddConsultation = {
                            if (fm != null && apt != null) {
                                onAddConsultation(fm.id, apt.patientId, apt.id)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenCard(
    token: TokenDto,
    onMarkArrival: () -> Unit,
    onSkip: () -> Unit,
    canPatientTools: Boolean,
    onOpenProfile: () -> Unit,
    onAddConsultation: () -> Unit,
) {
    val bgColor = when (token.status) {
        "serving" -> MaterialTheme.colorScheme.primaryContainer
        "completed" -> MaterialTheme.colorScheme.tertiaryContainer
        "skipped" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val statusLabel = when (token.status) {
        "serving" -> "SERVING"
        "completed" -> "DONE"
        "skipped" -> "SKIPPED"
        else -> if (token.hasArrived) "ARRIVED" else "WAITING"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Token number
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        "${token.tokenNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    token.patientName
                        ?: token.appointment?.familyMember?.name
                        ?: "Patient",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    statusLabel + if (token.isOffline) " (Walk-in)" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (token.status == "waiting") {
                IconButton(onClick = onMarkArrival) {
                    Icon(
                        if (token.hasArrived) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle arrival",
                        tint = if (token.hasArrived) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                IconButton(onClick = onSkip) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Skip",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            if (canPatientTools && (token.status == "serving" || token.status == "completed")) {
                IconButton(onClick = onOpenProfile) {
                    Icon(Icons.Default.Person, contentDescription = "Open patient profile")
                }
                IconButton(onClick = onAddConsultation) {
                    Icon(Icons.Default.Description, contentDescription = "Add prescription")
                }
            }
        }
    }
}

@Composable
private fun OfflineTokenDialog(
    onDismiss: () -> Unit,
    onAssign: (name: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Walk-in Token") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Patient Name (optional)") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
            )
        },
        confirmButton = {
            LargeButton(text = "Assign Token", onClick = { onAssign(name) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
