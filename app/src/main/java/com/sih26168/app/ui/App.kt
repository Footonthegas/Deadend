package com.sih26168.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sih26168.app.ui.theme.SIH26168Theme
import com.sih26168.app.viewmodel.NavigationViewModel

@Composable
fun App() {
    val viewModel: NavigationViewModel = viewModel()
    
    SIH26168Theme {
        NavigationScreen(viewModel)
    }
}
