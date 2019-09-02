# Geofencer [![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15) [![Gradle Version](https://img.shields.io/badge/gradle-5.6.0-green.svg)](https://docs.gradle.org/current/release-notes)  [![Kotlin](https://img.shields.io/badge/kotlin-1.3.41-green.svg)](https://kotlinlang.org/) [ ![Download](https://api.bintray.com/packages/exozetag/maven/Geofencer/images/download.svg) ](https://bintray.com/exozetag/maven/Geofencer/_latestVersion)
 
        
# How to install (tbd)

##### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://dl.bintray.com/exozetag/maven' }
		}
	}
##### Step 2. Add the dependency

	dependencies {
		implementation 'com.sprotte:geofencer:1.0.3'
	}
	
# Set up for geofence monitoring

The first step in requesting geofence monitoring is to request the necessary permission. To use geofencing, your app must request `ACCESS_FINE_LOCATION`. To request this permission, add the following element as a child element of the `<manifest>` element in your app manifest:

	<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
	
You have to inherit from [GeofenceIntentService](geofencer/service/GeofenceBroadcastReceiver.kt) 

	class AppGeofenceService : GeofenceIntentService() {
	
	    override fun onGeofence(geofence: Geofence) {
	    	 //do your stuff like sendNotification(applicationContext, geofence.title, geofence.message)
	    }
	}

And register your service in the manifest

    <service
     android:name=".ui.AppGeofenceService"
     android:permission="android.permission.BIND_JOB_SERVICE"
     android:exported="true"/>
     
Add your google key in strings.xml like so

	<string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR_KEY</string>

     
# How to use the library

    val geofence = Geofence(id = "bla", latitude = 42.22, longitude = 57.234, radius = 10.0)
	Geofencer(requireContext()).addGeofence(geofence, AppGeofenceService::class.java) {
		//success
	}



## Contributors

[Jan Rabe](jan.rabe@exozet.com)

[Paul Sprotte](paul.sprotte@exozet.com)