package com.sprotte.geolocator.geofencer.demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.sprotte.geolocator.geofencer.demo.R
import net.kibotu.logger.LogcatLogger
import net.kibotu.logger.Logger

class MainActivity : AppCompatActivity() {

    val navHostFragment
        get() = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

    val navController
        get() = navHostFragment.navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Logger.addLogger(LogcatLogger())
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp() || super.onSupportNavigateUp()

}
