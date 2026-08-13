package com.matnokh.driver.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.matnokh.driver.R

@Composable
private fun MapShell(title: String, center: LatLng, onBack: () -> Unit, myLocation: Boolean = false, markers: @Composable () -> Unit) {
    val ctx = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    LaunchedEffect(Unit) { if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
    val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(center, 12.5f) }
    LaunchedEffect(myLocation, granted) { if (myLocation) currentLatLng(ctx)?.let { camera.position = CameraPosition.fromLatLngZoom(LatLng(it.first, it.second), 14f) } }
    Box(Modifier.fillMaxSize().background(C.bg)) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camera,
            properties = MapProperties(isMyLocationEnabled = granted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = granted, zoomControlsEnabled = true, mapToolbarEnabled = true, compassEnabled = true),
        ) { OsmTiles(); markers() }
        Box(Modifier.align(Alignment.TopStart).statusBarsPadding().padding(14.dp).size(46.dp).clip(RoundedCornerShape(16.dp)).background(C.card).border(1.dp, C.line, RoundedCornerShape(16.dp)).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            Ic(R.drawable.ic_back, 20.dp, C.head)
        }
        Box(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(14.dp).clip(RoundedCornerShape(50.dp)).background(Color(0xF2FFFFFF)).border(1.dp, C.line, RoundedCornerShape(50.dp)).padding(horizontal = 16.dp, vertical = 10.dp)) {
            T(title, 13, FontWeight.ExtraBold, C.head)
        }
    }
}

/* خريطة كاملة: موقعي + الطلبات القريبة */
@Composable
fun OrdersMapFull(onBack: () -> Unit) {
    val center = LatLng(Drv.driverLat.value ?: 24.7136, Drv.driverLng.value ?: 46.6753)
    MapShell("الطلبات القريبة منك", center, onBack, myLocation = true) {
        Marker(state = MarkerState(center), title = "موقعي")
        Drv.received.forEach { j ->
            val la = j.fromLat; val ln = j.fromLng
            if (la != null && ln != null) Marker(state = MarkerState(LatLng(la, ln)), title = j.svc, snippet = j.price.toString() + " ريال")
        }
    }
}

/* خريطة كاملة: مسار الطلب النشط (الاستلام + التسليم) */
@Composable
fun RouteMapFull(job: Job?, onBack: () -> Unit) {
    val fLat = job?.fromLat; val fLng = job?.fromLng
    val from = if (fLat != null && fLng != null) LatLng(fLat, fLng) else LatLng(Drv.driverLat.value ?: 24.7136, Drv.driverLng.value ?: 46.6753)
    val tLat = job?.toLat; val tLng = job?.toLng
    MapShell("مسار الطلب النشط", from, onBack) {
        Marker(state = MarkerState(from), title = "الاستلام")
        if (tLat != null && tLng != null) Marker(state = MarkerState(LatLng(tLat, tLng)), title = "التسليم")
    }
}
