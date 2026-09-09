package com.sih26168.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih26168.app.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val vm: com.sih26168.app.viewmodel.NavigationViewModel = viewModel()

            val locationPermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                vm.setLocationPermissionGranted(allGranted)
            }

            val uiState by vm.uiState.collectAsState()

            LaunchedEffect(Unit) {
                val finePermission = Manifest.permission.ACCESS_FINE_LOCATION
                val coarsePermission = Manifest.permission.ACCESS_COARSE_LOCATION
                val hasFine = ContextCompat.checkSelfPermission(this@MainActivity, finePermission) == PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(this@MainActivity, coarsePermission) == PackageManager.PERMISSION_GRANTED
                if (hasFine && hasCoarse) {
                    vm.setLocationPermissionGranted(true)
                } else {
                    locationPermission.launch(arrayOf(finePermission, coarsePermission))
                }
            }

            LaunchedEffect(uiState.locationPermissionGranted) {
                if (uiState.locationPermissionGranted && uiState.compassSpinStage == com.sih26168.app.gnss.CompassSpinStage.COMPASS_SPIN) {
                    vm.startCompassSpin()
                }
            }

            App()
        }
    }
}
