package net.kibotu.geofencer.demo.kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import net.kibotu.geofencer.demo.ui.MapScreen
import net.kibotu.geofencer.demo.ui.theme.GeofencerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            GeofencerTheme {
                MapScreen()
            }
        }
    }
}
