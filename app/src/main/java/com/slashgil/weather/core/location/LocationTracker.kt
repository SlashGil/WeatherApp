package com.slashgil.weather.core.location

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.slashgil.weather.domain.model.Coordinates
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

interface LocationTracker {
    suspend fun getCurrentLocation(): Coordinates?
}

class DefaultLocationTracker @Inject constructor(
    private val locationClient: FusedLocationProviderClient,
    private val application: Application
) : LocationTracker {

    override suspend fun getCurrentLocation(): Coordinates? {
        val hasFine = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return null
        }

        return try {
            // 1. Try cached last known location from FusedLocationClient (2s timeout)
            val lastLoc = withTimeoutOrNull(2000) {
                locationClient.lastLocation.await()
            }
            if (lastLoc != null) {
                return Coordinates(lastLoc.latitude, lastLoc.longitude)
            }

            // 2. Try High Accuracy location fix (essential for emulators & real GPS) (5s timeout)
            val freshLocHigh = withTimeoutOrNull(5000) {
                val cts = CancellationTokenSource()
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).await()
            }
            if (freshLocHigh != null) {
                return Coordinates(freshLocHigh.latitude, freshLocHigh.longitude)
            }

            // 3. Try Balanced Power Accuracy location fix (5s timeout)
            val freshLocBalanced = withTimeoutOrNull(5000) {
                val cts = CancellationTokenSource()
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                ).await()
            }
            if (freshLocBalanced != null) {
                return Coordinates(freshLocBalanced.latitude, freshLocBalanced.longitude)
            }

            // 4. Fallback to System LocationManager (GPS & Network providers)
            val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = try { locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
            val netLoc = try { locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
            val bestLoc = gpsLoc ?: netLoc

            if (bestLoc != null) {
                Coordinates(bestLoc.latitude, bestLoc.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
