package com.healthcare.app.ui.patient.family

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.app.data.dto.FamilyMemberDto
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.PatientRepository
import com.healthcare.app.ui.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyMembersViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
) : ViewModel() {
    private val _members = MutableStateFlow<Resource<List<FamilyMemberDto>>>(Resource.Loading)
    val members: StateFlow<Resource<List<FamilyMemberDto>>> = _members

    init { load() }

    fun load() {
        viewModelScope.launch {
            _members.value = Resource.Loading
            _members.value = patientRepository.getFamilyMembers()
        }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            patientRepository.deleteFamilyMember(memberId)
            load()
        }
    }
}

@Composable
fun FamilyMembersScreen(
    onAddMember: () -> Unit,
    onBack: () -> Unit,
    viewModel: FamilyMembersViewModel = hiltViewModel(),
) {
    val members by viewModel.members.collectAsState()

    Scaffold(
        topBar = { AppTopBar(title = "Family Members", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMember,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Member", style = MaterialTheme.typography.labelLarge) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        when (val state = members) {
            is Resource.Loading -> LoadingScreen()
            is Resource.Error -> ErrorMessage(state.message, onRetry = { viewModel.load() })
            is Resource.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.data) { member ->
                        MemberCard(member = member, onDelete = {
                            if (!member.isSelf) viewModel.deleteMember(member.id)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberCard(member: FamilyMemberDto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.name, style = MaterialTheme.typography.titleMedium)
                    if (member.isSelf) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                "SELF",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                Text(
                    "Age: ${member.age} | ${member.gender.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!member.bloodGroup.isNullOrBlank()) {
                    Text(
                        "Blood: ${member.bloodGroup}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (member.allergies.isNotEmpty()) {
                    Text(
                        "Allergies: ${member.allergies.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            if (!member.isSelf) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
