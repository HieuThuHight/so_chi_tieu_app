package com.example.glassfinance

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glassfinance.data.preferences.ThemeMode
import com.example.glassfinance.data.viewmodel.FinanceViewModel
import com.example.glassfinance.data.viewmodel.FinanceViewModelFactory
import com.example.glassfinance.ui.GlassFinanceApp
import com.example.glassfinance.ui.theme.GlassFinanceTheme

class MainActivity : ComponentActivity() {

    // Android 13+: xin quyền hiện thông báo "Đã lưu giao dịch".
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val viewModel: FinanceViewModel = viewModel(
                factory = FinanceViewModelFactory(applicationContext)
            )
            val settings by viewModel.settings.collectAsState()

            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Icon status bar / nav bar đổi theo theme người dùng chọn, không chỉ theo hệ thống.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    // Không phủ lớp scrim xám/trắng của hệ thống lên vùng phía sau
                    // thanh Liquid Glass. Nền app sẽ tiếp tục hiển thị xuyên suốt.
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT
                    ) { darkTheme }
                )
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
                onDispose { }
            }

            GlassFinanceTheme(darkTheme = darkTheme) {
                GlassFinanceApp(viewModel)
            }
        }
    }
}