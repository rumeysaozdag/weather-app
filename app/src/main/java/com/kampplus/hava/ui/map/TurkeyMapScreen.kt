package com.kampplus.hava.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.kampplus.hava.data.ApiClient
import com.kampplus.hava.data.RainViewerFrame
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.city.CurrentLocationProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import retrofit2.HttpException

private enum class RoutePoint { START, DESTINATION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurkeyMapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var start by remember { mutableStateOf<GeoPoint?>(null) }
    var destination by remember { mutableStateOf<GeoPoint?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var startCityName by remember { mutableStateOf<String?>(null) }
    var destinationCityName by remember { mutableStateOf<String?>(null) }
    var currentCityName by remember { mutableStateOf<String?>(null) }
    var choosePoint by remember { mutableStateOf(RoutePoint.START) }
    var radarEnabled by remember { mutableStateOf(true) }
    var radarPlaying by remember { mutableStateOf(false) }
    var radarLoading by remember { mutableStateOf(false) }
    var radarHost by remember { mutableStateOf<String?>(null) }
    var radarFrames by remember { mutableStateOf<List<RainViewerFrame>>(emptyList()) }
    var radarFrameIndex by remember { mutableStateOf(0) }
    var radarMessage by remember { mutableStateOf("") }
    var showDataSourcesDialog by remember { mutableStateOf(false) }
    var routeCoordinates by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var routeDistance by remember { mutableStateOf<Double?>(null) }
    var routeDuration by remember { mutableStateOf<Double?>(null) }
    var isRouting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("Haritaya dokunarak seçili noktayı belirle.") }
    var manualTarget by remember { mutableStateOf<RoutePoint?>(null) }
    var placeQuery by remember { mutableStateOf("") }
    var placeResults by remember { mutableStateOf<List<City>>(emptyList()) }
    var placeSearchError by remember { mutableStateOf<String?>(null) }
    var isSearchingPlace by remember { mutableStateOf(false) }
    var centerOnLocationAfterPermission by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val provinceBoundaries = remember(context) { loadProvinceBoundaries(context) }
    val mapInteraction = remember {
        MapInteraction { point ->
            if (choosePoint == RoutePoint.START) {
                start = point
                startCityName = provinceNameAt(point, provinceBoundaries)
                notice = "Başlangıç noktası seçildi. Varış noktasını seç."
            } else {
                destination = point
                destinationCityName = provinceNameAt(point, provinceBoundaries)
                notice = "Varış noktası seçildi. Rota çizmek için Rota oluştur'a dokun."
            }
            routeCoordinates = emptyList()
            routeDistance = null
            routeDuration = null
        }
    }

    val locate: (Boolean) -> Unit = { centerCamera ->
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasCoarse && !hasFine) {
            notice = "Konum izni gerekiyor."
        } else {
            scope.launch {
                notice = "Konum alınıyor…"
                runCatching { CurrentLocationProvider(context.applicationContext).getLocation() }
                    .onSuccess { location: Location ->
                        val point = GeoPoint(location.latitude, location.longitude)
                        currentLocation = point
                        currentCityName = provinceNameAt(point, provinceBoundaries)
                        start = point
                        startCityName = currentCityName
                        choosePoint = RoutePoint.DESTINATION
                        if (centerCamera) mapInteraction.map?.let { map ->
                            map.controller.setZoom(13.0)
                            map.controller.animateTo(point)
                        }
                        notice = "Konum başlangıç noktası olarak ayarlandı."
                    }
                    .onFailure { notice = it.message ?: "Konum alınamadı." }
            }
        }
    }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.any { it }) locate(centerOnLocationAfterPermission) else notice = "Konum izni verilmedi. Haritadan başlangıcı seçebilirsin."
    }
    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) locate(false)
        else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
    }
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(radarEnabled) {
        if (radarEnabled) {
            radarFrames = emptyList()
            radarHost = null
            while (isActive) {
                radarLoading = true
                radarMessage = "Radar görüntüleri alınıyor…"
                runCatching { ApiClient.rainViewerApi.getWeatherMaps() }
                    .onSuccess { response ->
                        val frames = response.radar.past.takeLast(6)
                        radarHost = response.host.trimEnd('/')
                        radarFrames = frames
                        radarFrameIndex = frames.lastIndex.coerceAtLeast(0)
                        radarMessage = if (frames.isEmpty()) "Bu konum için radar görüntüsü bulunamadı." else "Geçmiş radar görüntüleri hazır."
                    }
                    .onFailure {
                        if (radarFrames.isEmpty()) radarMessage = "Radar görüntüsü şu anda alınamıyor. Daha sonra tekrar dene."
                    }
                radarLoading = false
                delay(5 * 60 * 1000L)
            }
        } else {
            radarPlaying = false
        }
    }
    LaunchedEffect(radarPlaying, radarFrames) {
        if (radarPlaying && radarFrames.size > 1) {
            while (isActive) {
                delay(4_500)
                radarFrameIndex = if (radarFrameIndex >= radarFrames.lastIndex) 0 else radarFrameIndex + 1
            }
        }
    }
    LaunchedEffect(
        mapView,
        start,
        destination,
        currentLocation,
        startCityName,
        destinationCityName,
        currentCityName,
        routeCoordinates,
        radarEnabled,
        radarHost,
        radarFrames,
        radarFrameIndex
    ) {
        val map = mapView ?: return@LaunchedEffect
        val radarTileSource = if (radarEnabled) {
            radarHost?.let { host -> radarFrames.getOrNull(radarFrameIndex)?.let { RainViewerTileSource(host, it.path) } }
        } else null
        renderMapOverlays(
            map,
            start,
            destination,
            currentLocation,
            startCityName,
            destinationCityName,
            currentCityName,
            routeCoordinates,
            radarTileSource
        )
    }

    mapInteraction.onMapTap = { point ->
        if (choosePoint == RoutePoint.START) {
            start = point
            startCityName = provinceNameAt(point, provinceBoundaries)
            notice = "Başlangıç noktası seçildi. Şimdi Varış seçip haritaya dokun."
        } else {
            destination = point
            destinationCityName = provinceNameAt(point, provinceBoundaries)
            notice = "Varış noktası seçildi. Rota oluştur'a dokun."
        }
        routeCoordinates = emptyList()
        routeDistance = null
        routeDuration = null
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Column {
                            Text(
                                text = "Harita & Radar",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Yağış radarı ve rota planlayıcı",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        OutlinedButton(
                            onClick = {
                                val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasLocationPermission) locate(true)
                                else {
                                    centerOnLocationAfterPermission = true
                                    locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                                }
                            },
                            modifier = Modifier.padding(end = 12.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text("⌖ Konumum", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PointChoice(
                    label = "A · Başlangıç",
                    point = start,
                    cityName = startCityName,
                    selected = choosePoint == RoutePoint.START,
                    modifier = Modifier.weight(1f),
                    onMapSelect = {
                        choosePoint = RoutePoint.START
                        notice = "Haritaya dokunarak başlangıcı seç."
                    },
                    onManualSelect = {
                        manualTarget = RoutePoint.START
                        placeQuery = ""
                        placeResults = emptyList()
                        placeSearchError = null
                    }
                )
                PointChoice(
                    label = "B · Varış",
                    point = destination,
                    cityName = destinationCityName,
                    selected = choosePoint == RoutePoint.DESTINATION,
                    modifier = Modifier.weight(1f),
                    onMapSelect = {
                        choosePoint = RoutePoint.DESTINATION
                        notice = "Haritaya dokunarak varışı seç."
                    },
                    onManualSelect = {
                        manualTarget = RoutePoint.DESTINATION
                        placeQuery = ""
                        placeResults = emptyList()
                        placeSearchError = null
                    }
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🌧 Yağış radarı katmanı",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (radarLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Switch(
                        checked = radarEnabled,
                        onCheckedChange = { enabled ->
                            radarEnabled = enabled
                            if (!enabled) radarMessage = ""
                        }
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx -> createMapView(ctx, mapInteraction).also { mapInteraction.map = it; mapView = it } },
                        update = {}
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clickable { uriHandler.openUri("https://www.openstreetmap.org/copyright") },
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "© OpenStreetMap contributors",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clickable { showDataSourcesDialog = true },
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = "ⓘ",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            if (radarEnabled) {
                if (radarFrames.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = formatRadarTime(radarFrames[radarFrameIndex.coerceIn(radarFrames.indices)].time),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "Kare ${radarFrameIndex + 1}/${radarFrames.size} · ${if (radarPlaying) "Oynatılıyor" else "Elle seçilebilir"}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (radarPlaying) radarPlaying = false
                                        else {
                                            radarFrameIndex = 0
                                            radarPlaying = true
                                        }
                                    },
                                    enabled = radarFrames.size > 1,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Text(
                                        text = if (radarPlaying) "Ⅱ Duraklat" else "▶ Oynat",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            if (radarFrames.size > 1) {
                                Slider(
                                    value = radarFrameIndex.toFloat(),
                                    onValueChange = { radarPlaying = false; radarFrameIndex = it.roundToInt() },
                                    valueRange = 0f..radarFrames.lastIndex.toFloat(),
                                    steps = (radarFrames.size - 2).coerceAtLeast(0)
                                )
                            }
                        }
                    }
                } else if (radarMessage.isNotBlank()) {
                    Text(
                        text = radarMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            if (routeDistance != null && routeDuration != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${startCityName ?: "Başlangıç"} → ${destinationCityName ?: "Varış"}",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "OSRM Sürüş Rotası",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Mesafe",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = formatDistance(routeDistance!!),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Süre",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = formatDuration(routeDuration!!),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = notice,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    enabled = start != null && destination != null && !isRouting,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = {
                        val from = start
                        val to = destination
                        if (from != null && to != null) scope.launch {
                            isRouting = true
                            notice = "Rota hesaplanıyor…"
                            runCatching {
                                ApiClient.routingApi.getRoute("${from.longitude},${from.latitude};${to.longitude},${to.latitude}")
                            }.onSuccess { response ->
                                val route = response.routes.firstOrNull()
                                if (response.code == "Ok" && route != null) {
                                    routeCoordinates = route.geometry.coordinates.mapNotNull { coordinate ->
                                        if (coordinate.size < 2) null else GeoPoint(coordinate[1], coordinate[0])
                                    }
                                    routeDistance = route.distance
                                    routeDuration = route.duration
                                    mapInteraction.map?.let { map ->
                                        map.zoomToBoundingBox(
                                            BoundingBox(
                                                maxOf(from.latitude, to.latitude),
                                                maxOf(from.longitude, to.longitude),
                                                minOf(from.latitude, to.latitude),
                                                minOf(from.longitude, to.longitude)
                                            ),
                                            true,
                                            64
                                        )
                                    }
                                    notice = "Rota hazır. Süre trafik yoğunluğunu hesaba katmaz."
                                } else notice = "Bu iki nokta arasında rota bulunamadı."
                            }.onFailure { error ->
                                notice = if (error is HttpException) "Rota servisi şu anda yanıt vermiyor (${error.code()})." else "Rota alınamadı. İnternet bağlantını kontrol et."
                            }
                            isRouting = false
                        }
                    }
                ) {
                    if (isRouting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Rota oluştur")
                    }
                }
            }
        }
    }

    if (manualTarget != null) {
        AlertDialog(
            onDismissRequest = { manualTarget = null },
            shape = RoundedCornerShape(10.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(if (manualTarget == RoutePoint.START) "Başlangıç konumu" else "Varış konumu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Şehir ara veya koordinat gir (enlem; boylam), ör. 40,78; 30,38.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = placeQuery,
                        onValueChange = { placeQuery = it; placeSearchError = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        label = { Text("Şehir veya koordinat") }
                    )
                    placeSearchError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (placeResults.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            placeResults.take(5).forEach { city ->
                                Surface(
                                    onClick = {
                                        val point = GeoPoint(city.latitude, city.longitude)
                                        if (manualTarget == RoutePoint.START) {
                                            start = point
                                            startCityName = city.name
                                        } else {
                                            destination = point
                                            destinationCityName = city.name
                                        }
                                        mapInteraction.map?.let { map ->
                                            map.controller.setZoom(11.0)
                                            map.controller.animateTo(point)
                                        }
                                        manualTarget = null
                                        routeCoordinates = emptyList()
                                        routeDistance = null
                                        routeDuration = null
                                        notice = "${city.name} konumu seçildi."
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                        Text(city.name, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = city.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (isSearchingPlace) CircularProgressIndicator()
                }
            },
            confirmButton = {
                TextButton(enabled = placeQuery.isNotBlank() && !isSearchingPlace, onClick = {
                    val input = placeQuery.trim()
                    val point = parseCoordinates(input)
                    if (point != null) {
                        val cityName = provinceNameAt(point, provinceBoundaries)
                        if (manualTarget == RoutePoint.START) {
                            start = point
                            startCityName = cityName
                        } else {
                            destination = point
                            destinationCityName = cityName
                        }
                        mapInteraction.map?.let { map ->
                            map.controller.setZoom(11.0)
                            map.controller.animateTo(point)
                        }
                        manualTarget = null
                        routeCoordinates = emptyList()
                        routeDistance = null
                        routeDuration = null
                        notice = "Koordinat seçildi."
                    } else {
                        scope.launch {
                            isSearchingPlace = true
                            placeSearchError = null
                            runCatching { ApiClient.cityApi.search(input) }
                                .onSuccess { response ->
                                    placeResults = response.results.orEmpty()
                                    if (placeResults.isEmpty()) placeSearchError = "Bu adla konum bulunamadı. Koordinat da girebilirsin."
                                }
                                .onFailure { placeSearchError = "Konum aranamadı. İnternet bağlantısını kontrol et." }
                            isSearchingPlace = false
                        }
                    }
                }) { Text(if (isSearchingPlace) "Aranıyor…" else "Ara / Uygula") }
            },
            dismissButton = { TextButton(onClick = { manualTarget = null }) { Text("Kapat") } }
        )
    }

    if (showDataSourcesDialog) {
        AlertDialog(
            onDismissRequest = { showDataSourcesDialog = false },
            shape = RoundedCornerShape(10.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Harita bilgisi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Harita verisi", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { uriHandler.openUri("https://www.openstreetmap.org/copyright") }) {
                        Text("© OpenStreetMap contributors")
                    }
                    Text("Yağış radarı", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { uriHandler.openUri("https://www.rainviewer.com/") }) {
                        Text("Weather data by RainViewer")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDataSourcesDialog = false }) { Text("Kapat") } }
        )
    }
}

@Composable
private fun PointChoice(
    label: String,
    point: GeoPoint?,
    cityName: String?,
    selected: Boolean,
    modifier: Modifier,
    onMapSelect: () -> Unit,
    onManualSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onMapSelect),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = cityName
                            ?: point?.let { "${formatCoordinate(it.latitude)}, ${formatCoordinate(it.longitude)}" }
                            ?: "Haritadan seç",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (point != null && cityName != null) {
                        Text(
                            text = "${formatCoordinate(point.latitude)}, ${formatCoordinate(point.longitude)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = "✎ Elle gir / Şehir ara",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onManualSelect)
                    .padding(vertical = 2.dp)
            )
        }
    }
}

private class MapInteraction(var onMapTap: (GeoPoint) -> Unit) {
    var map: MapView? = null
}

private fun createMapView(context: Context, interaction: MapInteraction): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    return MapView(context).apply {
        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        setMinZoomLevel(2.0)
        controller.setZoom(5.0)
        controller.setCenter(GeoPoint(39.0, 35.0))
        onResume()
        overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                interaction.onMapTap(point)
                return true
            }
            override fun longPressHelper(point: GeoPoint): Boolean = singleTapConfirmedHelper(point)
        }))
    }
}

private fun renderMapOverlays(
    map: MapView,
    start: GeoPoint?,
    destination: GeoPoint?,
    currentLocation: GeoPoint?,
    startCityName: String?,
    destinationCityName: String?,
    currentCityName: String?,
    route: List<GeoPoint>,
    radarTileSource: RainViewerTileSource?
) {
    val eventOverlay = map.overlays.filterIsInstance<MapEventsOverlay>()
    map.overlays.removeAll { it is Marker || it is Polyline || it is TilesOverlay || it is MapEventsOverlay }
    map.overlays.addAll(eventOverlay)
    if (radarTileSource != null) {
        runCatching {
            val provider = MapTileProviderBasic(map.context, radarTileSource)
            map.overlays.add(TilesOverlay(provider, map.context).apply { loadingBackgroundColor = AndroidColor.TRANSPARENT })
        }
    }
    route.takeIf { it.size >= 2 }?.let { points ->
        map.overlays.add(Polyline(map).apply {
            setPoints(points)
            outlinePaint.color = AndroidColor.rgb(157, 80, 255)
            outlinePaint.strokeWidth = 10f
            outlinePaint.isAntiAlias = true
        })
    }
    buildList {
        start?.let { add(it to (startCityName ?: "A · Başlangıç")) }
        destination?.let { add(it to (destinationCityName ?: "B · Varış")) }
        currentLocation?.takeIf { it != start && it != destination }?.let { add(it to (currentCityName ?: "Konumum")) }
    }.forEach { (point, label) ->
        map.overlays.add(Marker(map).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setInfoWindow(null)
            setTextIcon(label)
            title = label
        })
    }
    map.invalidate()
}

private class RainViewerTileSource(private val host: String, private val framePath: String) : OnlineTileSourceBase(
    "RainViewer-${framePath.substringAfterLast('/')}", 0, 7, 256, ".png", arrayOf(host)
) {
    override fun getTileURLString(pMapTileIndex: Long): String =
        "$host$framePath/256/${MapTileIndex.getZoom(pMapTileIndex)}/${MapTileIndex.getX(pMapTileIndex)}/${MapTileIndex.getY(pMapTileIndex)}/2/1_0.png"
}

private fun formatDistance(meters: Double): String = if (meters >= 1000) "${String.format(Locale.forLanguageTag("tr-TR"), "%.1f", meters / 1000)} km" else "${meters.roundToInt()} m"
private fun formatDuration(seconds: Double): String {
    val minutes = (seconds / 60).roundToInt()
    return if (minutes < 60) "$minutes dk" else "${minutes / 60} sa ${minutes % 60} dk"
}
private fun formatCoordinate(value: Double) = String.format(Locale.forLanguageTag("tr-TR"), "%.4f", value)
private fun formatRadarTime(time: Long): String =
    "Geçmiş radar · ${DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).withZone(ZoneId.systemDefault()).format(Instant.ofEpochSecond(time))}"

private data class ProvinceBoundary(val name: String, val outerRings: List<List<GeoPoint>>) {
    fun contains(point: GeoPoint): Boolean = outerRings.any { ring ->
        var inside = false
        var previous = ring.lastOrNull() ?: return@any false
        for (current in ring) {
            val crosses = (current.latitude > point.latitude) != (previous.latitude > point.latitude) &&
                point.longitude < (previous.longitude - current.longitude) *
                (point.latitude - current.latitude) / (previous.latitude - current.latitude) + current.longitude
            if (crosses) inside = !inside
            previous = current
        }
        inside
    }
}

private fun loadProvinceBoundaries(context: Context): List<ProvinceBoundary> = runCatching {
    val json = context.assets.open("turkiye_iller.geojson").bufferedReader(Charsets.UTF_8).use { it.readText() }
    JsonParser.parseString(json).asJsonObject.getAsJsonArray("features").mapNotNull { element ->
        val feature = element.asJsonObject
        val name = feature.getAsJsonObject("properties").get("il_adi")?.asString ?: return@mapNotNull null
        val geometry = feature.getAsJsonObject("geometry")
        val rings = when (geometry.get("type").asString) {
            "Polygon" -> listOfNotNull(geometry.getAsJsonArray("coordinates").firstOrNull()?.asJsonArray?.toGeoRing())
            "MultiPolygon" -> geometry.getAsJsonArray("coordinates").mapNotNull { polygon ->
                polygon.asJsonArray.firstOrNull()?.asJsonArray?.toGeoRing()
            }
            else -> emptyList()
        }.filter { it.size >= 3 }
        if (rings.isEmpty()) null else ProvinceBoundary(name, rings)
    }
}.getOrDefault(emptyList())

private fun JsonArray.toGeoRing(): List<GeoPoint> = mapNotNull { coordinate ->
    runCatching {
        val pair = coordinate.asJsonArray
        GeoPoint(pair[1].asDouble, pair[0].asDouble)
    }.getOrNull()
}

private fun provinceNameAt(point: GeoPoint, boundaries: List<ProvinceBoundary>): String? =
    boundaries.firstOrNull { it.contains(point) }?.name

private fun parseCoordinates(input: String): GeoPoint? {
    val parts = when {
        ';' in input -> input.split(';').map { it.trim().replace(',', '.') }
        Regex("^-?\\d+(?:\\.\\d+)?\\s*,\\s*-?\\d+(?:\\.\\d+)?$").matches(input.trim()) ->
            input.split(',').map(String::trim)
        input.count { it == ',' } >= 3 && '.' !in input -> {
            val values = input.split(',').map(String::trim)
            if (values.size == 4) listOf("${values[0]}.${values[1]}", "${values[2]}.${values[3]}") else emptyList()
        }
        else -> input.trim().split(Regex("\\s+")).map(String::trim)
    }.mapNotNull(String::toDoubleOrNull)
    if (parts.size != 2 || parts[0] !in -90.0..90.0 || parts[1] !in -180.0..180.0) return null
    return GeoPoint(parts[0], parts[1])
}
