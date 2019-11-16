package com.justai.junction.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.kontakt.sdk.android.ble.configuration.ScanMode
import com.kontakt.sdk.android.ble.configuration.ScanPeriod
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener
import com.kontakt.sdk.android.common.KontaktSDK
import com.kontakt.sdk.android.common.profile.IBeaconDevice
import com.kontakt.sdk.android.common.profile.IBeaconRegion
import com.kontakt.sdk.android.common.profile.IEddystoneDevice
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace
import me.lambdatamer.kandroid.viewmodel.KViewModel
import java.util.concurrent.TimeUnit


class RadarViewModel(application: Application) : KViewModel(application) {
    val beaconId = MutableLiveData<String>("M9Kh")
    val distance = MutableLiveData<Double>()

    init {
        KontaktSDK.initialize("mjLRQozplULgtHMWJNnvmNqHbznJwRTy")
    }

    private val proximityManager = ProximityManagerFactory.create(context)

    init {
        configureProximityManager()
    }

    fun startScan() {
        proximityManager.connect {
            if (!proximityManager.isScanning) proximityManager.startScanning()
        }
    }

    fun stopScan() {
        if (proximityManager.isScanning) proximityManager.stopScanning()
    }

    fun clear() {
        beaconId.value = null
        distance.value = null
    }

    private fun configureProximityManager() {
        proximityManager.configuration()
            //Using ranging for continuous scanning or MONITORING for scanning with intervals
            .scanPeriod(ScanPeriod.RANGING)
            //Using BALANCED for best performance/battery ratio
            .scanMode(ScanMode.LOW_LATENCY)
            //OnDeviceUpdate callback will be received with 5 seconds interval
            .deviceUpdateCallbackInterval(300)


        proximityManager.setIBeaconListener(object : IBeaconListener {
            override fun onIBeaconLost(iBeacon: IBeaconDevice, region: IBeaconRegion?) {
                if (iBeacon.uniqueId == beaconId.value) updateState(iBeacon)
            }

            override fun onIBeaconsUpdated(
                iBeacons: MutableList<IBeaconDevice>,
                region: IBeaconRegion?
            ) {
                iBeacons.find { it.uniqueId == beaconId.value }?.let(::updateState)
            }

            override fun onIBeaconDiscovered(iBeacon: IBeaconDevice, region: IBeaconRegion?) {
                if (iBeacon.uniqueId == beaconId.value) updateState(iBeacon)
            }

            fun updateState(iBeacon: IBeaconDevice?) {
                distance.postValue(iBeacon?.distance)
            }
        })
//        proximityManager.setEddystoneListener(object : EddystoneListener {
//            override fun onEddystonesUpdated(
//                eddystones: MutableList<IEddystoneDevice>,
//                namespace: IEddystoneNamespace?
//            ) {
//                eddystones.find { it.instanceId == beaconId.value }?.let(::updateState)
//            }
//
//            override fun onEddystoneDiscovered(
//                eddystone: IEddystoneDevice,
//                namespace: IEddystoneNamespace?
//            ) {
//                if (eddystone.instanceId == beaconId.value) updateState(eddystone)
//            }
//
//            override fun onEddystoneLost(
//                eddystone: IEddystoneDevice,
//                namespace: IEddystoneNamespace?
//            ) {
//                if (eddystone.instanceId == beaconId.value) updateState(eddystone)
//            }
//
//            fun updateState(eddyStone: IEddystoneDevice?) {
//                distance.postValue(eddyStone?.distance)
//            }
//        })
    }

}