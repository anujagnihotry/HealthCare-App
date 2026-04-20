package com.healthcare.app.ui.doctor.patients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.PatientSearchResultDto
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.DoctorCareRepository
import com.healthcare.app.ui.common.AppTopBar
import com.healthcare.app.ui.common.LoadingScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchFilter(val label: String, val apiMode: String?) {
    All("All", null),
    Name("Name", "name"),
    Mobile("Mobile", "mobile"),
    Code("Code", "code"),
}

data class DoctorPatientSearchState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.All,
    val isLoading: Boolean = false,
    val results: List<PatientSearchResultDto> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class DoctorPatientSearchViewModel @Inject constructor(
    private val doctorCareRepository: DoctorCareRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DoctorPatientSearchState())
    val state: StateFlow<DoctorPatientSearchState> = _state

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun setFilter(f: SearchFilter) {
        _state.value = _state.value.copy(filter = f)
    }

    fun search() {
        val q = _state.value.query.trim()
        if (q.length < 2) {
            _state.value = _state.value.copy(error = "Enter at least 2 characters", results = emptyList())
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = doctorCareRepository.searchPatients(q, _state.value.filter.apiMode)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isLoading = false, results = res.data)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = res.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorPatientSearchScreen(
    onBack: () -> Unit,
    onPatientClick: (familyMemberId: String, displayName: String) -> Unit,
    viewModel: DoctorPatientSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Find patient",
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = { viewModel.setQuery(it) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Search") },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(onClick = { viewModel.search() }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Filter", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                SearchFilter.values().forEach { f ->
                    FilterChip(
                        selected = state.filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.label) },
                    )
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.isLoading) {
                LoadingScreen()
                return@Column
            }

            if (state.results.isEmpty() && state.query.length >= 2 && !state.isLoading) {
                Text(
                    "No patients match your search.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.results) { row ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPatientClick(row.familyMemberId, row.name)
                            },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(row.name, style = MaterialTheme.typography.titleMedium)
                            row.phone?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                            row.customCode?.let {
                                Text("Code: $it", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
