package com.healthcare.app.ui.patient.doctors

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.DoctorDto
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.DoctorRepository
import com.healthcare.app.ui.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DoctorDetailViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val doctorId: String = savedStateHandle["doctorId"] ?: ""
    private val _doctor = MutableStateFlow<Resource<DoctorDto>>(Resource.Loading)
    val doctor: StateFlow<Resource<DoctorDto>> = _doctor

    init { load() }

    fun load() {
        viewModelScope.launch {
            _doctor.value = Resource.Loading
            _doctor.value = doctorRepository.getDoctorById(doctorId)
        }
    }
}

@Composable
fun DoctorDetailScreen(
    doctorId: String,
    onBookAtLocation: (locationId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: DoctorDetailViewModel = hiltViewModel(),
) {
    val doctorState by viewModel.doctor.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { AppTopBar(title = "Doctor Details", onBack = onBack) },
    ) { padding ->
        when (val state = doctorState) {
            is Resource.Loading -> LoadingScreen()
            is Resource.Error -> ErrorMessage(state.message, onRetry = { viewModel.load() })
            is Resource.Success -> {
                val doctor = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    // Doctor info
                    Text(doctor.name, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        doctor.specialization,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (!doctor.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(doctor.bio, style = MaterialTheme.typography.bodyLarge)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Clinic Locations", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(12.dp))

                    doctor.locations?.filter { it.isActive }?.forEach { location ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(location.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            location.address,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    // Book button
                                    Button(
                                        onClick = { onBookAtLocation(location.id) },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 3.dp,
                                        ),
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Book")
                                    }

                                    // Navigate button
                                    OutlinedButton(
                                        onClick = {
                                            val uri = Uri.parse(
                                                "google.navigation:q=${location.latitude},${location.longitude}"
                                            )
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                    ) {
                                        Icon(Icons.Default.Navigation, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Navigate")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
