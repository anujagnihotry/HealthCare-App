package com.healthcare.app.ui.patient.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.AppointmentDto
import com.healthcare.app.data.dto.FamilyMemberDto
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.AuthRepository
import com.healthcare.app.domain.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientDashboardState(
    val isLoading: Boolean = true,
    val upcoming: List<AppointmentDto> = emptyList(),
    val past: List<AppointmentDto> = emptyList(),
    val familyMembers: List<FamilyMemberDto> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class PatientDashboardViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PatientDashboardState())
    val state: StateFlow<PatientDashboardState> = _state

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val dashResult = patientRepository.getDashboard()
            val membersResult = patientRepository.getFamilyMembers()

            _state.value = _state.value.copy(
                isLoading = false,
                upcoming = (dashResult as? Resource.Success)?.data?.upcoming ?: emptyList(),
                past = (dashResult as? Resource.Success)?.data?.past ?: emptyList(),
                familyMembers = (membersResult as? Resource.Success)?.data ?: emptyList(),
                error = (dashResult as? Resource.Error)?.message,
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
