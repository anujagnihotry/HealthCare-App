@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
package com.healthcare.app.ui.patient.dashboard

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.healthcare.app.data.dto.AppointmentDto
import com.healthcare.app.ui.common.*
import java.time.LocalDate

@Composable
fun PatientDashboardScreen(
    onFindDoctor: () -> Unit,
    onFamilyMembers: () -> Unit,
    onRecords: (familyMemberId: String, memberName: String) -> Unit,
    onTrackToken: (doctorId: String, locationId: String, date: String) -> Unit,
    onLogout: () -> Unit,
    viewModel: PatientDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val today = remember { LocalDate.now().toString() }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "My Health",
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Quick actions — large elder-friendly buttons
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    QuickActionCard(
                        title = "Find Doctor",
                        icon = Icons.Default.Search,
                        onClick = onFindDoctor,
                        modifier = Modifier.weight(1f),
                    )
                    QuickActionCard(
                        title = "Family",
                        icon = Icons.Default.People,
                        onClick = onFamilyMembers,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Upcoming appointments
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Upcoming Appointments", style = MaterialTheme.typography.titleLarge)
            }

            if (state.isLoading) {
                item { LoadingScreen() }
            } else if (state.upcoming.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Text(
                            "No upcoming appointments.\nTap 'Find Doctor' to book one!",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
            } else {
                items(state.upcoming) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        isUpcoming = true,
                        today = today,
                        onTrackToken = {
                            onTrackToken(
                                appointment.doctorId,
                                appointment.locationId,
                                appointment.date,
                            )
                        },
                        onNavigate = {
                            val lat = appointment.location?.latitude
                            val lng = appointment.location?.longitude
                            if (lat != null && lng != null) {
                                val uri = Uri.parse("google.navigation:q=$lat,$lng")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        },
                        onViewRecords = {
                            val fm = appointment.familyMember
                            if (fm != null) {
                                onRecords(fm.id, fm.name)
                            }
                        },
                    )
                }
            }

            // Past appointments
            if (state.past.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Past Appointments", style = MaterialTheme.typography.titleLarge)
                }
                items(state.past) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        isUpcoming = false,
                        today = today,
                        onViewRecords = {
                            val fm = appointment.familyMember
                            if (fm != null) {
                                onRecords(fm.id, fm.name)
                            }
                        },
                    )
                }
            }

            // Family member records quick access
            if (state.familyMembers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Medical Records", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "View visits, prescriptions & reports by family member",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(state.familyMembers) { member ->
                    Card(
                        onClick = { onRecords(member.id, member.name) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
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
                                    "Visits, prescriptions, reports",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "View records",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Bottom spacer
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: AppointmentDto,
    isUpcoming: Boolean,
    today: String,
    onTrackToken: (() -> Unit)? = null,
    onNavigate: (() -> Unit)? = null,
    onViewRecords: (() -> Unit)? = null,
) {
    val isToday = appointment.date == today

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = appointment.doctor?.name ?: "Doctor",
                    style = MaterialTheme.typography.titleMedium,
                )
                if (appointment.token != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "Token: ${appointment.token.tokenNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${appointment.date} at ${appointment.timeSlot}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = appointment.location?.name ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "For: ${appointment.familyMember?.name ?: "Self"}",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons row
            if (isUpcoming) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Track Live Token — always visible for upcoming
                    if (onTrackToken != null) {
                        Button(
                            onClick = onTrackToken,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Track Token", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    // Navigate — only show on the day of appointment
                    if (isToday && onNavigate != null) {
                        OutlinedButton(
                            onClick = onNavigate,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Navigate", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // View records button — for both upcoming and past
            if (onViewRecords != null) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = onViewRecords,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Records", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
