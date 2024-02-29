package org.avmedia.gshockapi.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.net.wifi.ScanResult
import android.os.ParcelUuid
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleNumOfMatches
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanMode
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerMatchMode
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings
import no.nordicsemi.android.kotlin.ble.core.scanner.FilteredServiceUuid
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import org.avmedia.gshockapi.ProgressEvents

object GShockScanner {
    @SuppressLint("MissingPermission")
    val CASIO_SERVICE_UUID = "00001804-0000-1000-8000-00805f9b34fb"

    data class DeviceInfo(val name: String, val address: String)
    private lateinit var scannerFlow: Job

    @SuppressLint("MissingPermission")
    suspend fun scan(context: Context): CompletableDeferred<DeviceInfo> {

        val deferredResult: CompletableDeferred<DeviceInfo> = CompletableDeferred()

        val gShockFilters: List<BleScanFilter> = listOf(
            BleScanFilter(serviceUuid = FilteredServiceUuid(ParcelUuid.fromString(CASIO_SERVICE_UUID)))
        )

        val gShockSettings = BleScannerSettings(
            includeStoredBondedDevices = false,
            numOfMatches = BleNumOfMatches.MATCH_NUM_ONE_ADVERTISEMENT,
            matchMode = BleScannerMatchMode.MATCH_MODE_AGGRESSIVE,
            scanMode = BleScanMode.SCAN_MODE_LOW_LATENCY,
        )

        val scope = CoroutineScope(Dispatchers.IO)
        val deviceSet = mutableSetOf<String>()
        cancelFlow()


        scannerFlow = BleScanner(context).scan(filters = gShockFilters, settings = gShockSettings)
            .filter {
                val device: ServerDevice = it.device
                val ret = (device.name as String).startsWith("CASIO")
                ret
            }
            .onStart { }
            .onEmpty {
                ProgressEvents.onNext("ApiError", "No devices found while scanning")
                deferredResult.complete(DeviceInfo("", ""))
            }
            .onCompletion {
                // If we have not found any watches, resolve the promise to the app will not lock.
                if (deferredResult.isActive) {
                    deferredResult.complete(DeviceInfo("", ""))
                }
            }
            .onEach {
                val device = it.device
                if (device.address !in deviceSet) {
                    deviceSet.add(device.address)
                    deferredResult.complete(DeviceInfo(device.name as String, device.address))
                    cancelFlow()
                    scope.cancel("End scanning")
                }
            }
            .cancellable()
            .catch { e ->
                ProgressEvents.onNext("ApiError", "Scanning Error $e")
                deferredResult.complete(DeviceInfo("", ""))
            }
            .launchIn(scope)

        return deferredResult
    }

    fun cancelFlow() {
        if (::scannerFlow.isInitialized) {
            scannerFlow.cancel()
        }
    }
}