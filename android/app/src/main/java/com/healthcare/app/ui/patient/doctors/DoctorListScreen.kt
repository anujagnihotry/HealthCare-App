@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)

package com.healthcare.app.ui.patient.doctors


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
class DoctorListViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
) : ViewModel() {
    private val _doctors = MutableStateFlow<Resource<List<DoctorDto>>>(Resource.Loading)
    val doctors: StateFlow<Resource<List<DoctorDto>>> = _doctors

    init { loadDoctors() }

    fun loadDoctors() {
        viewModelScope.launch {
            _doctors.value = Resource.Loading
            _doctors.value = doctorRepository.getAllDoctors()
        }
    }
}

@Composable
fun DoctorListScreen(
    onDoctorClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: DoctorListViewModel = hiltViewModel(),
) {
    val doctors by viewModel.doctors.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Find a Doctor", onBack = onBack) },
    ) { padding ->
        when (val state = doctors) {
            is Resource.Loading -> LoadingScreen()
            is Resource.Error -> ErrorMessage(state.message, onRetry = { viewModel.loadDoctors() })
            is Resource.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.data) { doctor ->
                        DoctorCard(doctor = doctor, onClick = { onDoctorClick(doctor.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorCard(doctor: DoctorDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(doctor.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                doctor.specialization,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (!doctor.locations.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                doctor.locations.forEach { loc ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            loc.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
