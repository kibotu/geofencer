package com.sprotte.geofencer.demo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sprotte.geofencer.Geofencer
import com.sprotte.geofencer.demo.R
import net.kibotu.logger.LogcatLogger
import net.kibotu.logger.Logger

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Logger.addLogger(LogcatLogger())

        Geofencer()
    }
}
