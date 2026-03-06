package net.kibotu.geofencer.demo.kotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import net.kibotu.geofencer.demo.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onSupportNavigateUp() =
        Navigation.findNavController(this, R.id.navHost).navigateUp() || super.onSupportNavigateUp()
}
