package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

private val CoralPrimary = Color(0xFFE07856)
private val CreamBg = Color(0xFFFFF8F3)
private val TealAccent = Color(0xFF2A9D8F)
private val DarkText = Color(0xFF2D2A26)

data class PetServicePlace(
    val name: String,
    val lat: Double,
    val lon: Double,
    val type: String,
    val distanceKm: Double = 0.0,
    val phone: String = "",
    val address: String = ""
)

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userLat by remember { mutableDoubleStateOf(0.0) }
    var userLon by remember { mutableDoubleStateOf(0.0) }
    var hasLocation by remember { mutableStateOf(false) }
    var places by remember { mutableStateOf<List<PetServicePlace>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("all") }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            fetchLocation(fusedLocationClient) { lat, lon ->
                userLat = lat
                userLon = lon
                hasLocation = true
                scope.launch { loadNearbyPlaces(lat, lon, selectedFilter) { places = it } }
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFineLocation) {
            fetchLocation(fusedLocationClient) { lat, lon ->
                userLat = lat
                userLon = lon
                hasLocation = true
                scope.launch { loadNearbyPlaces(lat, lon, selectedFilter) { places = it } }
            }
        } else {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    LaunchedEffect(selectedFilter, hasLocation) {
        if (hasLocation) {
            isLoading = true
            loadNearbyPlaces(userLat, userLon, selectedFilter) { places = it }
            isLoading = false
        }
    }

    // Update map markers when places change
    LaunchedEffect(places, hasLocation) {
        val mv = mapView ?: return@LaunchedEffect
        mv.overlays.clear()
        if (hasLocation) {
            val userMarker = Marker(mv).apply {
                position = GeoPoint(userLat, userLon)
                title = "Your Location"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mv.overlays.add(userMarker)
        }
        places.forEach { place ->
            val marker = Marker(mv).apply {
                position = GeoPoint(place.lat, place.lon)
                title = place.name
                snippet = "${place.type} • ${String.format("%.1f", place.distanceKm)} km away"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            mv.overlays.add(marker)
        }
        mv.invalidate()
    }

    Column(modifier = Modifier.fillMaxSize().background(CreamBg)) {
        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("all" to "All", "pet_store" to "Pet Stores", "veterinary" to "Vets", "grooming" to "Grooming", "boarding" to "Boarding")
            filters.forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (hasLocation) {
                AndroidView(
                    factory = { ctx ->
                        Configuration.getInstance().userAgentValue = ctx.packageName
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(userLat, userLon))
                            mapView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isLoading) {
                            CircularProgressIndicator(color = CoralPrimary)
                            Text("Getting location...", modifier = Modifier.padding(top = 12.dp), color = Color.Gray)
                        } else {
                            Text("Location permission needed", color = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    locationPermissionLauncher.launch(arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                            ) { Text("Grant Location") }
                        }
                    }
                }
            }

            // Recenter button
            if (hasLocation) {
                FloatingActionButton(
                    onClick = {
                        mapView?.controller?.setCenter(GeoPoint(userLat, userLon))
                        mapView?.controller?.setZoom(15.0)
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    containerColor = CoralPrimary
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = Color.White)
                }
            }
        }

        // Places list
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nearby Pet Services (${places.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = DarkText,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(places) { place ->
                PlaceCard(place)
            }
        }
    }
}

@Composable
private fun PlaceCard(place: PetServicePlace) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(CoralPrimary.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (place.type) {
                        "Pet Store" -> Icons.Default.Store
                        "Veterinary" -> Icons.Default.LocalHospital
                        "Grooming" -> Icons.Default.ContentCut
                        "Boarding" -> Icons.Default.Home
                        else -> Icons.Default.Pets
                    },
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DarkText)
                Text("${place.type} • ${String.format("%.1f", place.distanceKm)} km away", fontSize = 12.sp, color = Color.Gray)
                if (place.address.isNotBlank()) {
                    Text(place.address, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                }
            }
        }
    }
}

private fun fetchLocation(
    client: FusedLocationProviderClient,
    onResult: (Double, Double) -> Unit
) {
    try {
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (loc != null) onResult(loc.latitude, loc.longitude)
            }
    } catch (e: SecurityException) { /* permission not granted */ }
}

private suspend fun loadNearbyPlaces(
    lat: Double, lon: Double, filter: String,
    onResult: (List<PetServicePlace>) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val queries = when (filter) {
                "pet_store" -> listOf("shop" to "Pet Store", "pet" to "Pet Store")
                "veterinary" -> listOf("amenity=veterinary" to "Veterinary", "clinic" to "Veterinary")
                "grooming" -> listOf("shop=pet_grooming" to "Grooming", "grooming" to "Grooming")
                "boarding" -> listOf("tourism=boarding" to "Boarding", "boarding" to "Boarding")
                else -> listOf(
                    "shop=pet" to "Pet Store",
                    "amenity=veterinary" to "Veterinary",
                    "shop=pet_grooming" to "Grooming",
                    "tourism=boarding" to "Boarding"
                )
            }

            val allPlaces = mutableListOf<PetServicePlace>()

            // Overpass API query
            val radius = 5000 // 5km
            val overpassQuery = buildOverpassQuery(lat, lon, radius, filter)
            val url = "https://overpass-api.de/api/interpreter?data=${URLEncoder.encode(overpassQuery, "UTF-8")}"

            val response = URL(url).readText()
            val json = JSONObject(response)
            val elements = json.getJSONArray("elements")

            for (i in 0 until elements.length()) {
                val el = elements.getJSONObject(i)
                val tags = el.optJSONObject("tags") ?: continue
                val name = tags.optString("name", "")
                val elLat = el.optDouble("lat", 0.0)
                val elLon = el.optDouble("lon", 0.0)

                val type = when {
                    tags.optString("shop") == "pet" || tags.optString("shop") == "pet_store" -> "Pet Store"
                    tags.optString("amenity") == "veterinary" -> "Veterinary"
                    tags.optString("shop") == "pet_grooming" || tags.optString("grooming") == "yes" -> "Grooming"
                    tags.optString("tourism") == "boarding" || tags.optString("boarding") == "yes" -> "Boarding"
                    else -> "Pet Service"
                }

                if (name.isNotBlank() && elLat != 0.0) {
                    val distance = FloatArray(1)
                    Location.distanceBetween(lat, lon, elLat, elLon, distance)
                    val distKm = distance[0] / 1000.0

                    allPlaces.add(PetServicePlace(
                        name = name,
                        lat = elLat,
                        lon = elLon,
                        type = type,
                        distanceKm = distKm,
                        phone = tags.optString("phone", tags.optString("contact:phone", "")),
                        address = tags.optString("addr:street", "") + " " + tags.optString("addr:city", "")
                    ))
                }
            }

            allPlaces.sortBy { it.distanceKm }
            onResult(allPlaces.take(30))
        } catch (e: Exception) {
            onResult(emptyList())
        }
    }
}

private fun buildOverpassQuery(lat: Double, lon: Double, radius: Int, filter: String): String {
    val filters = when (filter) {
        "pet_store" -> listOf("node[shop=pet](around:$radius,$lat,$lon)", "node[shop=pet_store](around:$radius,$lat,$lon)")
        "veterinary" -> listOf("node[amenity=veterinary](around:$radius,$lat,$lon)")
        "grooming" -> listOf("node[shop=pet_grooming](around:$radius,$lat,$lon)", "node[grooming=yes](around:$radius,$lat,$lon)")
        "boarding" -> listOf("node[tourism=boarding](around:$radius,$lat,$lon)", "node[boarding=yes](around:$radius,$lat,$lon)")
        else -> listOf(
            "node[shop=pet](around:$radius,$lat,$lon)",
            "node[amenity=veterinary](around:$radius,$lat,$lon)",
            "node[shop=pet_grooming](around:$radius,$lat,$lon)",
            "node[tourism=boarding](around:$radius,$lat,$lon)"
        )
    }
    val union = filters.joinToString(";") { it }
    return "[$union];out 50;"
}

