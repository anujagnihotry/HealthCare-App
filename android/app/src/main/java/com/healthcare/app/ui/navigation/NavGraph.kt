package com.healthcare.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.healthcare.app.ui.auth.LoginScreen
import com.healthcare.app.ui.auth.RegisterScreen
import com.healthcare.app.ui.auth.SplashScreen
import com.healthcare.app.ui.patient.dashboard.PatientDashboardScreen
import com.healthcare.app.ui.patient.doctors.DoctorListScreen
import com.healthcare.app.ui.patient.doctors.DoctorDetailScreen
import com.healthcare.app.ui.patient.booking.BookAppointmentScreen
import com.healthcare.app.ui.patient.family.FamilyMembersScreen
import com.healthcare.app.ui.patient.family.AddFamilyMemberScreen
import com.healthcare.app.ui.patient.records.PatientRecordsScreen
import com.healthcare.app.ui.patient.token.TokenTrackerScreen
import com.healthcare.app.ui.doctor.dashboard.DoctorDashboardScreen
import com.healthcare.app.ui.doctor.locations.ManageLocationsScreen
import com.healthcare.app.ui.doctor.availability.ManageAvailabilityScreen
import com.healthcare.app.ui.doctor.queue.TokenQueueScreen
import com.healthcare.app.ui.doctor.patients.DoctorAddConsultationScreen
import com.healthcare.app.ui.doctor.patients.DoctorPatientProfileScreen
import com.healthcare.app.ui.doctor.patients.DoctorPatientSearchScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth ──────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onContinue = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val dest = if (role == "doctor") Screen.DoctorDashboard.route
                    else Screen.PatientDashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { role ->
                    val dest = if (role == "doctor") Screen.DoctorDashboard.route
                    else Screen.PatientDashboard.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Patient ───────────────────────────────────
        composable(Screen.PatientDashboard.route) {
            PatientDashboardScreen(
                onFindDoctor = { navController.navigate(Screen.DoctorList.route) },
                onFamilyMembers = { navController.navigate(Screen.FamilyMembers.route) },
                onRecords = { familyMemberId, memberName ->
                    navController.navigate(
                        Screen.PatientRecords.createRoute(familyMemberId, memberName)
                    )
                },
                onTrackToken = { doctorId, locationId, date ->
                    navController.navigate(
                        Screen.TokenTracker.createRoute(doctorId, locationId, date)
                    )
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.DoctorList.route) {
            DoctorListScreen(
                onDoctorClick = { doctorId ->
                    navController.navigate(Screen.DoctorDetail.createRoute(doctorId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            Screen.DoctorDetail.route,
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: return@composable
            DoctorDetailScreen(
                doctorId = doctorId,
                onBookAtLocation = { locId ->
                    navController.navigate(Screen.BookAppointment.createRoute(doctorId, locId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            Screen.BookAppointment.route,
            arguments = listOf(
                navArgument("doctorId") { type = NavType.StringType },
                navArgument("locationId") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: return@composable
            val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable
            BookAppointmentScreen(
                doctorId = doctorId,
                locationId = locationId,
                onBookingComplete = {
                    navController.navigate(Screen.PatientDashboard.route) {
                        popUpTo(Screen.PatientDashboard.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.FamilyMembers.route) {
            FamilyMembersScreen(
                onAddMember = { navController.navigate(Screen.AddFamilyMember.route) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.AddFamilyMember.route) {
            AddFamilyMemberScreen(
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            Screen.PatientRecords.route,
            arguments = listOf(
                navArgument("familyMemberId") { type = NavType.StringType },
                navArgument("memberName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val familyMemberId = backStackEntry.arguments?.getString("familyMemberId") ?: return@composable
            val memberNameRaw = backStackEntry.arguments?.getString("memberName") ?: return@composable
            val memberName = Uri.decode(memberNameRaw)
            PatientRecordsScreen(
                familyMemberId = familyMemberId,
                memberName = memberName,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            Screen.TokenTracker.route,
            arguments = listOf(
                navArgument("doctorId") { type = NavType.StringType },
                navArgument("locationId") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: return@composable
            val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable
            val date = backStackEntry.arguments?.getString("date") ?: return@composable
            TokenTrackerScreen(
                doctorId = doctorId,
                locationId = locationId,
                date = date,
                onBack = { navController.popBackStack() },
            )
        }

        // ── Doctor ────────────────────────────────────
        composable(Screen.DoctorDashboard.route) {
            DoctorDashboardScreen(
                onManageLocations = { navController.navigate(Screen.ManageLocations.route) },
                onManageAvailability = { navController.navigate(Screen.ManageAvailability.route) },
                onTokenQueue = { locationId, date ->
                    navController.navigate(Screen.TokenQueue.createRoute(locationId, date))
                },
                onPatientSearch = { navController.navigate(Screen.DoctorPatientSearch.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.DoctorPatientSearch.route) {
            DoctorPatientSearchScreen(
                onBack = { navController.popBackStack() },
                onPatientClick = { familyMemberId, displayName ->
                    navController.navigate(
                        Screen.DoctorPatientProfile.createRoute(familyMemberId, displayName),
                    )
                },
            )
        }

        composable(
            Screen.DoctorPatientProfile.route,
            arguments = listOf(
                navArgument("familyMemberId") { type = NavType.StringType },
                navArgument("memberName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val memberName =
                Uri.decode(backStackEntry.arguments?.getString("memberName") ?: "")
            DoctorPatientProfileScreen(
                memberName = memberName,
                onBack = { navController.popBackStack() },
                onAddConsultation = { familyMemberId, patientId, appointmentId ->
                    navController.navigate(
                        Screen.DoctorAddConsultation.createRoute(
                            familyMemberId,
                            patientId,
                            appointmentId,
                        ),
                    )
                },
            )
        }

        composable(
            Screen.DoctorAddConsultation.route,
            arguments = listOf(
                navArgument("familyMemberId") { type = NavType.StringType },
                navArgument("patientId") { type = NavType.StringType },
                navArgument("appointmentId") { type = NavType.StringType },
            ),
        ) {
            DoctorAddConsultationScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.ManageLocations.route) {
            ManageLocationsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ManageAvailability.route) {
            ManageAvailabilityScreen(onBack = { navController.popBackStack() })
        }

        composable(
            Screen.TokenQueue.route,
            arguments = listOf(
                navArgument("locationId") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: return@composable
            val date = backStackEntry.arguments?.getString("date") ?: return@composable
            TokenQueueScreen(
                locationId = locationId,
                date = date,
                onBack = { navController.popBackStack() },
                onOpenPatientProfile = { familyMemberId, displayName ->
                    navController.navigate(
                        Screen.DoctorPatientProfile.createRoute(familyMemberId, displayName),
                    )
                },
                onAddConsultation = { familyMemberId, patientId, appointmentId ->
                    navController.navigate(
                        Screen.DoctorAddConsultation.createRoute(
                            familyMemberId,
                            patientId,
                            appointmentId,
                        ),
                    )
                },
            )
        }
    }
}
