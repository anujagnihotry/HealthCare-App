package com.healthcare.app.ui.patient.token

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.CurrentTokenResponse
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.TokenRepository
import com.healthcare.app.ui.common.*
import com.healthcare.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenTrackerViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val doctorId: String = savedStateHandle["doctorId"] ?: ""
    private val locationId: String = savedStateHandle["locationId"] ?: ""
    private val date: String = savedStateHandle["date"] ?: ""

    private val _currentToken = MutableStateFlow<CurrentTokenResponse?>(null)
    val currentToken: StateFlow<CurrentTokenResponse?> = _currentToken
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { startPolling() }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                val result = tokenRepository.getCurrentToken(doctorId, locationId, date)
                if (result is Resource.Success) {
                    _currentToken.value = result.data
                }
                _isLoading.value = false
                delay(10_000) // Poll every 10 seconds
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = tokenRepository.getCurrentToken(doctorId, locationId, date)
            if (result is Resource.Success) {
                _currentToken.value = result.data
            }
            _isLoading.value = false
        }
    }
}

@Composable
fun TokenTrackerScreen(
    doctorId: String,
    locationId: String,
    date: String,
    onBack: () -> Unit,
    viewModel: TokenTrackerViewModel = hiltViewModel(),
) {
    val currentToken by viewModel.currentToken.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Live Token Tracker", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isLoading && currentToken == null) {
                LoadingScreen()
            } else {
                Text(
                    "Currently Serving",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Big token number display
                Card(
                    modifier = Modifier.size(200.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (currentToken?.currentToken != null)
                                "Token\n${currentToken!!.currentToken}"
                            else "Not\nStarted",
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "${currentToken?.totalWaiting ?: 0} patients waiting",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Date: $date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Auto-refreshes every 10 seconds",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Refresh Now", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
