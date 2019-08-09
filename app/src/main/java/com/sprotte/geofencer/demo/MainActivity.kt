package com.sprotte.geofencer.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sprotte.geofencer.Geofencer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Geofencer()
    }
}
