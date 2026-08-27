package com.example.plantidentifier

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()



        super.onCreate(savedInstanceState)

        // Keep content below the status bar on devices that enforce edge-to-edge.
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {

            val preferences = getSharedPreferences(
                "plant_ai_preferences",
                MODE_PRIVATE
            )

            val hasSeenOnboarding =
                preferences.getBoolean(
                    "hasSeenOnboarding",
                    false
                )

            if (!hasSeenOnboarding) {

                val navHostFragment =
                    supportFragmentManager.findFragmentById(
                        R.id.nav_host_fragment
                    ) as NavHostFragment

                navHostFragment.navController.navigate(
                    R.id.onboardingFragment
                )
            }
        }
    }
}
