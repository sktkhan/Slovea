package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ads.ConsentManager
import com.example.ui.DailyFixViewModel
import com.example.ui.navigation.DailyFixAppShell
import com.example.ui.theme.DailyFixTheme

class MainActivity : ComponentActivity() {

    private val consentManager = ConsentManager()

    private val viewModel: DailyFixViewModel by viewModels {
        val app = application as DailyFixApplication
        DailyFixViewModel.Factory(app.repository, app.preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DailyFixApplication
        val adManager = app.adManager

        // Gather UMP consent asynchronously; initialize Mobile Ads SDK once consent allows.
        // Never blocks UI or offline tool usage.
        consentManager.gatherConsent(this) {
            adManager.initializeMobileAds(this)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            DailyFixTheme(themeMode = uiState.themeMode) {
                DailyFixAppShell(
                    viewModel = viewModel,
                    adManager = adManager,
                    consentManager = consentManager,
                    onShowPrivacyOptions = {
                        consentManager.showPrivacyOptionsForm(this) { errorMsg ->
                            if (!errorMsg.isNullOrBlank()) {
                                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            (application as DailyFixApplication).adManager.release()
        }
        super.onDestroy()
    }
}
