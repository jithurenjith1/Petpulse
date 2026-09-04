package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

private val CoralPrimary = Color(0xFFE07856)
private val CreamBg = Color(0xFFFFF8F3)
private val SosRed = Color(0xFFE63946)
private val DarkText = Color(0xFF2D2A26)

@Composable
fun RealSosScreen(petName: String = "My Pet") {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLon by remember { mutableDoubleStateOf(0.0) }
    var hasLocation by remember { mutableStateOf(false) }
    var isTracking by remember { mutableStateOf(false) }
    var locationHistory by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    currentLat = loc.latitude
                    currentLon = loc.longitude
                    hasLocation = true
                    val point = GeoPoint(loc.latitude, loc.longitude)
                    locationHistory = locationHistory + point

                    // Update map
                    mapView?.let { mv ->
                        mv.controller.setCenter(point)
                        mv.overlays.clear()

                        // Current location marker
                        val marker = Marker(mv).apply {
                            position = point
                            title = "$petName - Live Location"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mv.overlays.add(marker)

                        // Trail polyline
                        if (locationHistory.size > 1) {
                            val polyline = Polyline(mv).apply {
                                setPoints(locationHistory)
                                outlinePaint.color = android.graphics.Color.parseColor("#E63946")
                                outlinePaint.strokeWidth = 8f
                            }
                            mv.overlays.add(polyline)
                        }

                        mv.invalidate()
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            startTracking(fusedLocationClient, locationCallback)
            isTracking = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isTracking) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(CreamBg)) {
        // SOS Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SosRed)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SOS - $petName", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (isTracking) "🔴 LIVE TRACKING ACTIVE" else "Tap Start to begin live tracking",
                    color = Color.White.copy(0.9f),
                    fontSize = 13.sp
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
                            controller.setZoom(18.0)
                            controller.setCenter(GeoPoint(currentLat, currentLon))
                            mapView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(48.dp))
                        Text("Waiting for location...", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }

        // Location info
        if (hasLocation) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Location", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Lat: ${String.format("%.6f", currentLat)}", fontSize = 12.sp, color = Color.Gray)
                    Text("Lon: ${String.format("%.6f", currentLon)}", fontSize = 12.sp, color = Color.Gray)
                    Text("Track points: ${locationHistory.size}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Google Maps: https://maps.google.com/?q=$currentLat,$currentLon",
                        fontSize = 11.sp, color = TealAccent
                    )
                }
            }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        if (!isTracking) {
                            startTracking(fusedLocationClient, locationCallback)
                            isTracking = true
                        } else {
                            fusedLocationClient.removeLocationUpdates(locationCallback)
                            isTracking = false
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ))
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isTracking) SosRed else CoralPrimary)
            ) {
                Text(if (isTracking) "STOP TRACKING" else "START LIVE TRACKING", fontWeight = FontWeight.Bold)
            }

            // Share location button
            if (hasLocation) {
                Button(
                    onClick = {
                        val mapsUrl = "https://maps.google.com/?q=$currentLat,$currentLon"
                        val shareText = "🚨 SOS! $petName is lost!\n\nLive location: $mapsUrl\n\nPlease help find $petName!"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share SOS Location"))
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealAccent)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SHARE SOS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private val TealAccent = Color(0xFF2A9D8F)

private fun startTracking(
    client: FusedLocationProviderClient,
    callback: LocationCallback
) {
    try {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()
        client.requestLocationUpdates(request, callback, android.os.Looper.getMainLooper())
    } catch (e: SecurityException) { /* no permission */ }
}

