package com.healthcare.app.ui.doctor.locations

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Tasks
import com.healthcare.app.data.dto.CreateLocationRequest
import com.healthcare.app.data.dto.LocationDto
import com.healthcare.app.domain.model.Resource
import com.healthcare.app.domain.repository.DoctorRepository
import com.healthcare.app.ui.common.AppTopBar
import com.healthcare.app.ui.common.ErrorMessage
import com.healthcare.app.ui.common.LargeButton
import com.healthcare.app.ui.common.LoadingScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject

private sealed class LocationFormTarget {
    data object Add : LocationFormTarget()
    data class Edit(val location: LocationDto) : LocationFormTarget()
}

@HiltViewModel
class ManageLocationsViewModel @Inject constructor(
    private val doctorRepository: DoctorRepository,
) : ViewModel() {
    private val _locations = MutableStateFlow<Resource<List<LocationDto>>>(Resource.Loading)
    val locations: StateFlow<Resource<List<LocationDto>>> = _locations
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    init { load() }

    fun load() {
        viewModelScope.launch {
            _locations.value = Resource.Loading
            _locations.value = doctorRepository.getMyLocations()
        }
    }

    fun addLocation(name: String, address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _isSaving.value = true
            doctorRepository.createLocation(CreateLocationRequest(name, address, lat, lng))
            _isSaving.value = false
            load()
        }
    }

    fun updateLocation(locationId: String, name: String, address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            _isSaving.value = true
            doctorRepository.updateLocation(
                locationId,
                CreateLocationRequest(name, address, lat, lng),
            )
            _isSaving.value = false
            load()
        }
    }

    fun deleteLocation(locationId: String) {
        viewModelScope.launch {
            _isSaving.value = true
            doctorRepository.deleteLocation(locationId)
            _isSaving.value = false
            load()
        }
    }
}

@Composable
fun ManageLocationsScreen(
    onBack: () -> Unit,
    viewModel: ManageLocationsViewModel = hiltViewModel(),
) {
    val locations by viewModel.locations.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    var formTarget by remember { mutableStateOf<LocationFormTarget?>(null) }
    var pendingDelete by remember { mutableStateOf<LocationDto?>(null) }

    pendingDelete?.let { loc ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove location?") },
            text = {
                Text(
                    "“${loc.name}” will be removed from your clinic list. You can add it again later.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLocation(loc.id)
                        pendingDelete = null
                    },
                    enabled = !isSaving,
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }

    when (val target = formTarget) {
        is LocationFormTarget.Add ->
            key("add") {
                LocationFormDialog(
                    title = "Add Clinic Location",
                    confirmLabel = "Add",
                    initial = null,
                    isSaving = isSaving,
                    onDismiss = { formTarget = null },
                    onConfirm = { name, address, lat, lng ->
                        viewModel.addLocation(name, address, lat, lng)
                        formTarget = null
                    },
                )
            }
        is LocationFormTarget.Edit ->
            key(target.location.id) {
                LocationFormDialog(
                    title = "Edit Clinic Location",
                    confirmLabel = "Save",
                    initial = target.location,
                    isSaving = isSaving,
                    onDismiss = { formTarget = null },
                    onConfirm = { name, address, lat, lng ->
                        viewModel.updateLocation(target.location.id, name, address, lat, lng)
                        formTarget = null
                    },
                )
            }
        null -> {}
    }

    Scaffold(
        topBar = { AppTopBar(title = "Clinic Locations", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { formTarget = LocationFormTarget.Add },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Location") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        when (val state = locations) {
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
                    items(state.data, key = { it.id }) { location ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp),
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(location.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        location.address,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(
                                    onClick = { formTarget = LocationFormTarget.Edit(location) },
                                    enabled = !isSaving,
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit location",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                IconButton(
                                    onClick = { pendingDelete = location },
                                    enabled = !isSaving,
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove location",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private suspend fun reverseGeocodeLine(context: Context, latitude: Double, longitude: Double): String? =
    withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (cont.isActive) {
                                    cont.resume(addresses.firstOrNull()?.getAddressLine(0))
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) cont.resume(null)
                            }
                        },
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.getAddressLine(0)
            }
        } catch (_: Exception) {
            null
        }
    }

private fun coordinatesValid(latStr: String, lngStr: String): Boolean {
    val lat = latStr.toDoubleOrNull() ?: return false
    val lng = lngStr.toDoubleOrNull() ?: return false
    return lat in -90.0..90.0 && lng in -180.0..180.0
}

@Composable
private fun LocationFormDialog(
    title: String,
    confirmLabel: String,
    initial: LocationDto?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String, lat: Double, lng: Double) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context.applicationContext)
    }

    val formKey = initial?.id ?: "new"
    var name by remember(formKey) { mutableStateOf(initial?.name ?: "") }
    var address by remember(formKey) { mutableStateOf(initial?.address ?: "") }
    var lat by remember(formKey) {
        mutableStateOf(
            initial?.let { String.format(Locale.US, "%.6f", it.latitude) } ?: "",
        )
    }
    var lng by remember(formKey) {
        mutableStateOf(
            initial?.let { String.format(Locale.US, "%.6f", it.longitude) } ?: "",
        )
    }
    var fetchingLocation by remember(formKey) { mutableStateOf(false) }
    var locationError by remember(formKey) { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    fun fetchCurrentLocation() {
        scope.launch {
            locationError = null
            fetchingLocation = true
            try {
                val location = withContext(Dispatchers.IO) {
                    Tasks.await(
                        fusedClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            CancellationTokenSource().token,
                        ),
                    )
                }
                if (location != null) {
                    lat = String.format(Locale.US, "%.6f", location.latitude)
                    lng = String.format(Locale.US, "%.6f", location.longitude)
                    val line = reverseGeocodeLine(context, location.latitude, location.longitude)
                    if (address.isBlank() && !line.isNullOrBlank()) {
                        address = line
                    }
                } else {
                    locationError = "Location unavailable. Enable GPS or try again."
                }
            } catch (e: SecurityException) {
                locationError = "Location permission is required."
            } catch (e: Exception) {
                locationError = e.message ?: "Could not read location."
            } finally {
                fetchingLocation = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val ok = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) {
            fetchCurrentLocation()
        } else {
            locationError = "Location permission denied."
        }
    }

    fun onUseCurrentLocationClick() {
        if (fetchingLocation) return
        locationError = null
        if (hasLocationPermission(context)) {
            fetchCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    val canSubmit = !isSaving &&
        name.isNotBlank() &&
        address.isNotBlank() &&
        coordinatesValid(lat, lng)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Clinic Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { onUseCurrentLocationClick() },
                    enabled = !fetchingLocation && !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (fetchingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Getting location…")
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use current location")
                    }
                }
                locationError?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Latitude and longitude are filled automatically, or you can type them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lat,
                        onValueChange = { lat = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = lng,
                        onValueChange = { lng = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                LargeButton(
                    text = if (isSaving) "Saving..." else confirmLabel,
                    onClick = {
                        val latD = lat.toDoubleOrNull() ?: return@LargeButton
                        val lngD = lng.toDoubleOrNull() ?: return@LargeButton
                        onConfirm(name.trim(), address.trim(), latD, lngD)
                    },
                    enabled = canSubmit,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
