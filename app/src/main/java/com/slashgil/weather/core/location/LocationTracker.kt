package com.slashgil.weather.core.location

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
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
        val hasAccessFineLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAccessFineLocationPermission && !hasAccessCoarseLocationPermission) {
            return null
        }

        return try {
            // 1. Try cached last known location with 2s timeout
            val lastLoc = withTimeoutOrNull(2000) {
                locationClient.lastLocation.await()
            }
            if (lastLoc != null) {
                return Coordinates(lastLoc.latitude, lastLoc.longitude)
            }

            // 2. Try active location fix with 5s timeout
            val freshLoc = withTimeoutOrNull(5000) {
                val cts = CancellationTokenSource()
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                ).await()
            }
            if (freshLoc != null) {
                Coordinates(freshLoc.latitude, freshLoc.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
