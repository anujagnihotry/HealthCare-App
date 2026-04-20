package com.healthcare.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    // Auth
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Patient
    data object PatientDashboard : Screen("patient_dashboard")
    data object DoctorList : Screen("doctor_list")
    data object DoctorDetail : Screen("doctor_detail/{doctorId}") {
        fun createRoute(doctorId: String) = "doctor_detail/$doctorId"
    }
    data object BookAppointment : Screen("book_appointment/{doctorId}/{locationId}") {
        fun createRoute(doctorId: String, locationId: String) =
            "book_appointment/$doctorId/$locationId"
    }
    data object FamilyMembers : Screen("family_members")
    data object AddFamilyMember : Screen("add_family_member")
    data object PatientRecords : Screen("patient_records/{familyMemberId}/{memberName}") {
        fun createRoute(familyMemberId: String, memberName: String) =
            "patient_records/$familyMemberId/${Uri.encode(memberName)}"
    }
    data object TokenTracker : Screen("token_tracker/{doctorId}/{locationId}/{date}") {
        fun createRoute(doctorId: String, locationId: String, date: String) =
            "token_tracker/$doctorId/$locationId/$date"
    }

    // Doctor
    data object DoctorDashboard : Screen("doctor_dashboard")
    data object ManageLocations : Screen("manage_locations")
    data object ManageAvailability : Screen("manage_availability")
    data object TokenQueue : Screen("token_queue/{locationId}/{date}") {
        fun createRoute(locationId: String, date: String) = "token_queue/$locationId/$date"
    }

    data object DoctorPatientSearch : Screen("doctor_patient_search")

    data object DoctorPatientProfile : Screen("doctor_patient_profile/{familyMemberId}/{memberName}") {
        fun createRoute(familyMemberId: String, memberName: String) =
            "doctor_patient_profile/$familyMemberId/${Uri.encode(memberName)}"
    }

    data object DoctorAddConsultation : Screen("doctor_add_consultation/{familyMemberId}/{patientId}/{appointmentId}") {
        fun createRoute(familyMemberId: String, patientId: String, appointmentId: String?) =
            "doctor_add_consultation/$familyMemberId/$patientId/${appointmentId ?: "none"}"

        fun parseAppointmentId(segment: String): String? =
            if (segment == "none" || segment.isBlank()) null else segment
    }
}
