package com.example.sahyadrisiri

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

// Data Models
data class WaterReport(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val score: Int = 0,
    val clarity: Int = 0,
    val flow: String = "",
    val pollution: Boolean = false,
    val smell: String = ""
)

class WaterReportItem(val report: WaterReport) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(report.latitude, report.longitude)
    override fun getTitle(): String? = null
    override fun getSnippet(): String? = null
    override fun getZIndex(): Float? = null
}

// ViewModel to handle data logic
class ReportsViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance("https://sahyadrisiri-494604-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("reports")
    
    private val _reports = MutableStateFlow<List<WaterReport>>(emptyList())
    val reports = _reports.asStateFlow()

    init {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(WaterReport::class.java) }
                Log.d("ReportsViewModel", "Fetched ${list.size} reports from Firebase")
                _reports.value = list
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModel", "Firebase Error: ${error.message}")
            }
        })
    }

    fun saveReport(report: WaterReport) {
        database.push().setValue(report)
    }
}

@SuppressLint("PotentialBehaviorOverride")
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<WaterReportItem>
    private val viewModel: ReportsViewModel by viewModels()
    
    private var heatmapOverlay: TileOverlay? = null
    private var isHeatmapEnabled = false
    private var currentFilterId: Int? = R.id.chipAll

    // Domain Boundary: Sahyadri (Western Ghats) Lat/Lng Bounds
    private val sahyadriBounds = LatLngBounds(
        LatLng(8.0, 72.5), // South-West (Kanyakumari area)
        LatLng(21.0, 77.5) // North-East (Tapi river area)
    )

    private val reportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) handleReportResult(result.data)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) enableMyLocation()
        else Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (_: Exception) { }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupUI()
    }

    private fun setupUI() {
        findViewById<ExtendedFloatingActionButton>(R.id.btnReport).setOnClickListener {
            reportLauncher.launch(Intent(this, ReportActivity::class.java))
        }

        findViewById<FloatingActionButton>(R.id.btnWiki).setOnClickListener {
            startActivity(Intent(this, WikiActivity::class.java))
        }

        findViewById<FloatingActionButton>(R.id.btnAlerts).setOnClickListener {
            startActivity(Intent(this, AlertActivity::class.java))
        }

        findViewById<FloatingActionButton>(R.id.btnMapType)?.setOnClickListener {
            cycleMapMode()
        }

        findViewById<FloatingActionButton>(R.id.btnHealthMap)?.setOnClickListener {
            toggleHealthMap()
        }

        findViewById<ChipGroup>(R.id.chipGroupFilter).setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterId = checkedIds.firstOrNull()
            filterReports(currentFilterId)
        }

        // Feature: Location Search logic
        findViewById<EditText>(R.id.etSearch)?.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.text.toString())
                true
            } else false
        }
    }

    private fun performSearch(query: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    handleSearchResults(addresses)
                }
                override fun onError(errorMessage: String?) {
                    runOnUiThread { Toast.makeText(this@MainActivity, "Search error: $errorMessage", Toast.LENGTH_SHORT).show() }
                }
            })
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(query, 1)
                    handleSearchResults(addresses)
                } catch (_: Exception) {
                    runOnUiThread { Toast.makeText(this@MainActivity, "Search error", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    private fun handleSearchResults(addresses: List<Address>?) {
        runOnUiThread {
            if (!addresses.isNullOrEmpty()) {
                val latLng = LatLng(addresses[0].latitude, addresses[0].longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleHealthMap() {
        if (!::mMap.isInitialized) return
        isHeatmapEnabled = !isHeatmapEnabled
        if (isHeatmapEnabled) {
            updateHeatmap(viewModel.reports.value)
            Toast.makeText(this, "Health Map Enabled", Toast.LENGTH_SHORT).show()
        } else {
            heatmapOverlay?.remove()
            heatmapOverlay = null
            Toast.makeText(this, "Health Map Disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cycleMapMode() {
        if (!::mMap.isInitialized) return

        mMap.mapType = when (mMap.mapType) {
            GoogleMap.MAP_TYPE_NORMAL -> GoogleMap.MAP_TYPE_SATELLITE
            GoogleMap.MAP_TYPE_SATELLITE -> GoogleMap.MAP_TYPE_TERRAIN
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
        
        val modeName = when (mMap.mapType) {
            GoogleMap.MAP_TYPE_SATELLITE -> "Satellite View"
            GoogleMap.MAP_TYPE_TERRAIN -> "Terrain View"
            else -> "Normal View"
        }
        Toast.makeText(this, modeName, Toast.LENGTH_SHORT).show()
    }

    private fun filterReports(checkedId: Int?) {
        if (!::clusterManager.isInitialized) return
        clusterManager.clearItems()
        val filtered = viewModel.reports.value.filter { report ->
            when (checkedId) {
                R.id.chipHealthy -> report.score >= 7
                R.id.chipWarning -> report.score in 4..6
                R.id.chipUnsafe -> report.score < 4
                else -> true
            }
        }.map { WaterReportItem(it) }
        clusterManager.addItems(filtered)
        clusterManager.cluster()
    }

    private fun handleReportResult(data: Intent?) {
        val clarityRaw = data?.getIntExtra("clarity", 0) ?: 0
        val flow = data?.getStringExtra("flow") ?: "Unknown"
        val pollution = data?.getBooleanExtra("pollution", false) ?: false
        val smell = data?.getStringExtra("smell") ?: "Unknown"

        // Normalized clarity (assuming seekbar 0-10 or 0-100)
        val normalizedClarity = if (clarityRaw > 10) (clarityRaw / 10).coerceAtMost(5) else (clarityRaw / 2).coerceAtMost(5)
        val score = (normalizedClarity * 2 + (if (flow == "High") 2 else 0) - (if (pollution) 2 else 0) - (if (smell == "Bad") 2 else 0)).coerceIn(0, 10)

        if (score < 4) Toast.makeText(this, R.string.water_unsafe, Toast.LENGTH_LONG).show()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val latLng = if (location != null) LatLng(location.latitude, location.longitude) else mMap.cameraPosition.target
                
                if (!sahyadriBounds.contains(latLng)) {
                    Toast.makeText(this, "Reports limited to Sahyadri region", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                viewModel.saveReport(WaterReport(latLng.latitude, latLng.longitude, score, clarityRaw, flow, pollution, smell))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupClusterManager()
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setLatLngBoundsForCameraTarget(sahyadriBounds)

        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        } catch (e: Exception) { Log.e("MainActivity", "Style error", e) }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(13.5110, 75.0935), 10f))
        
        lifecycleScope.launch {
            viewModel.reports.collect { reports ->
                if (isHeatmapEnabled) updateHeatmap(reports)
                filterReports(currentFilterId) 
            }
        }
    }

    private fun updateHeatmap(reports: List<WaterReport>) {
        heatmapOverlay?.remove()
        if (reports.isEmpty() || !isHeatmapEnabled) return
        
        val data = reports.map { WeightedLatLng(LatLng(it.latitude, it.longitude), (10.0 - it.score).coerceAtLeast(0.1)) }
        val provider = HeatmapTileProvider.Builder()
            .weightedData(data)
            .radius(50)
            .gradient(Gradient(
                intArrayOf(Color.parseColor("#2196F3"), Color.YELLOW, Color.parseColor("#8B4513")), // Blue to Brown
                floatArrayOf(0.1f, 0.5f, 1.0f)
            ))
            .build()
        heatmapOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(provider))
    }

    private fun setupClusterManager() {
        clusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)

        clusterManager.renderer = object : DefaultClusterRenderer<WaterReportItem>(this, mMap, clusterManager) {
            override fun onBeforeClusterItemRendered(item: WaterReportItem, markerOptions: MarkerOptions) {
                val color = when {
                    item.report.score >= 7 -> Color.parseColor("#2196F3") // Healthy Blue
                    item.report.score >= 4 -> Color.YELLOW // Warning Yellow
                    else -> Color.parseColor("#8B4513") // Unsafe Brown
                }
                val icon = getBitmapDescriptor(this@MainActivity, R.drawable.ic_water_drop, color, item.report.score)
                if (icon != null) markerOptions.icon(icon)
            }
        }

        clusterManager.setOnClusterItemClickListener { item ->
            showDetails(item.report)
            true
        }
    }

    private fun showDetails(report: WaterReport) {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_details, findViewById(android.R.id.content), false)
        
        val pollutionText = if (report.pollution) getString(R.string.yes) else getString(R.string.no)
        view.findViewById<TextView>(R.id.txtSheetDetails).text = 
            getString(R.string.marker_details_format, report.clarity, report.flow, report.smell, pollutionText)
        view.findViewById<TextView>(R.id.txtSheetScore).text = getString(R.string.score_format, report.score)

        view.findViewById<Button>(R.id.btnNavigate)?.setOnClickListener {
            val uri = "google.navigation:q=${report.latitude},${report.longitude}".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/dir/?api=1&destination=${report.latitude},${report.longitude}".toUri()))
            }
        }

        view.findViewById<Button>(R.id.btnShare)?.setOnClickListener {
            val shareText = "SahyadriSiri Report:\nScore: ${report.score}/10\nLocation: http://maps.google.com/maps?q=${report.latitude},${report.longitude}"
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText)
            }, "Share Report"))
        }
        
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun getBitmapDescriptor(context: Context, resId: Int, color: Int, score: Int): BitmapDescriptor? {
        val drawable = ContextCompat.getDrawable(context, resId) ?: return null
        val size = (44 * context.resources.displayMetrics.density).toInt()
        drawable.setBounds(0, 0, size, size)
        drawable.setTint(color)
        
        val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)

        val paint = Paint().apply {
            this.color = Color.WHITE
            this.textAlign = Paint.Align.CENTER
            this.textSize = 14f * context.resources.displayMetrics.density
            this.isFakeBoldText = true
        }
        val xPos = canvas.width / 2f
        val yPos = (canvas.height * 0.55f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(score.toString(), xPos, yPos, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
    }
}
