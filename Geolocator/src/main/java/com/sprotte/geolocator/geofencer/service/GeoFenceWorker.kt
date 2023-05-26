package com.sprotte.geolocator.geofencer.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sprotte.geolocator.geofencer.Geofencer
import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.geofencer.models.WorkModuleInfo
import com.sprotte.geolocator.geofencer.models.WorkerModule
import com.sprotte.geolocator.utils.log
import com.sprotte.geolocator.utils.loge


@Suppress("UNCHECKED_CAST")
class GeoFenceWorker(val ctx: Context, params: WorkerParameters): Worker(ctx, params) {

    @SuppressLint("NewApi")
    private fun findWorkerClass(geoFence: Geofence){
        val resolveInfoList = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ctx.packageManager.queryIntentServices(
                Intent(WorkerModule.WORKER_MODULE_INTENT),
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
        } else {
            ctx.packageManager.queryIntentServices(
                Intent(WorkerModule.WORKER_MODULE_INTENT),
                PackageManager.GET_META_DATA
            )
        }

        if(resolveInfoList.isEmpty()){
            loge("No supported worker service declared")
            Result.failure()
            return
        }

        val resolvedClass = resolveInfoList.first()
        log("findWorkerClass ${resolvedClass.serviceInfo.name}")

        resolvedClass?.serviceInfo?.let { info ->
            if (!info.name.isNullOrBlank()) {
                try {
                    val clazz: Class<*> = Class.forName(info.name)
                    if (WorkerModule::class.java.isAssignableFrom(clazz)) {
                        val workInfo = WorkModuleInfo(
                            clazz as Class<out WorkerModule>,
                            info.metaData
                        )
                        startAppWorker(geoFence, workInfo)
                    }
                } catch (t: Throwable) {
                    loge(t.message)
                    loge("findWorkerClass Error discovering module ${info.name}")
                    Result.failure()
                }
            }
        }

    }

    private fun startAppWorker(geoFence: Geofence, moduleInfo: WorkModuleInfo){
        try {
            val obj = moduleInfo.moduleClass.getDeclaredConstructor().newInstance()
            if (obj !is WorkerModule) {
                // Should never come here
                Result.failure()
                return
            }
            obj.ctx = ctx
            obj.onGeofence(geoFence)
        } catch (t: Throwable) {
            Log.e("ModuleInitializer", "Error initialising module", t)
            Result.failure()
        }

    }

    override fun doWork(): Result {
        return try{
            val geoFenceId = inputData.getString(Geofencer.INTENT_EXTRAS_KEY) ?: return Result.failure()
            val geofence = Geofencer(ctx).get(geoFenceId) ?: return Result.failure()
            findWorkerClass(geofence)
            Result.success()
        }catch (e: Exception){
            Result.failure()
        }
    }

}


