package com.example.sahyadrisiri

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

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
