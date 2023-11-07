package org.avmedia.gshockapi.io

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import org.avmedia.gshockapi.Settings
import org.avmedia.gshockapi.ble.Connection
import org.avmedia.gshockapi.casio.CasioConstants
import org.avmedia.gshockapi.utils.Utils
import org.avmedia.gshockapi.utils.Utils.getBooleanSafe
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
object TimeAdjustmentIO {

    private object DeferredValueHolder {
        lateinit var deferredResult: CompletableDeferred<Boolean>
    }

    suspend fun request(): Boolean {
        return CachedIO.request("GET_TIME_ADJUSTMENT", ::getTimeAdjustment) as Boolean
    }

    private suspend fun getTimeAdjustment(key: String): Boolean {
        DeferredValueHolder.deferredResult = CompletableDeferred()
        Connection.sendMessage("{ action: '$key'}")
        return DeferredValueHolder.deferredResult.await()
    }

    fun set(settings: Settings) {
        val settingJson = Gson().toJson(settings)
        CachedIO.cache.remove("GET_TIME_ADJUSTMENT")
        Connection.sendMessage("{action: \"SET_TIME_ADJUSTMENT\", value: ${settingJson}}")
    }

    fun onReceived (data: String) {
        val timeAdjustmentSet = isTimeAdjustmentSet(data)
        CasioIsAutoTimeOriginalValue.value = data
        DeferredValueHolder.deferredResult.complete(timeAdjustmentSet)
    }

    private fun isTimeAdjustmentSet(data: String): Boolean {
        // syncing off: 110f0f0f0600500004000100->80<-37d2
        // syncing on:  110f0f0f0600500004000100->00<-37d2

        CasioIsAutoTimeOriginalValue.value = data // save original data for future use
        return Utils.toIntArray(data)[12] == 0
    }

    private fun toJsonTimeAdjustment(isTimeAdjustmentSet: Boolean): JSONObject {
        return JSONObject("{\"timeAdjustment\": $isTimeAdjustmentSet }")
    }

    object CasioIsAutoTimeOriginalValue {
        var value = ""
    }

    fun sendToWatch(message: String) {
        CasioIO.writeCmd(
            0x000c,
            Utils.byteArray(CasioConstants.CHARACTERISTICS.CASIO_SETTING_FOR_BLE.code.toByte())
        )
    }

    fun sendToWatchSet(message: String) {
        val settings = JSONObject(message).get("value") as JSONObject
        // add the original string from Casio, so we do not mess up any ot the other settings.
        settings.put(
            "casioIsAutoTimeOriginalValue",
            CasioIsAutoTimeOriginalValue.value
        )
        val encodedTimeAdj = encodeTimeAdjustment(settings)
        if (encodedTimeAdj.isNotEmpty()) {
            CasioIO.writeCmd(0x000e, encodedTimeAdj)
        }
    }

    private fun encodeTimeAdjustment(settings: JSONObject): ByteArray {

        var casioIsAutoTimeOriginalValue = settings.getString("casioIsAutoTimeOriginalValue")
        if (casioIsAutoTimeOriginalValue.isEmpty()) {
            return "".toByteArray()
        }

        // syncing off: 110f0f0f0600500004000100->80<-37d2
        // syncing on:  110f0f0f0600500004000100->00<-37d2

        var intArray = Utils.toIntArray(casioIsAutoTimeOriginalValue)

        if (settings.get("timeAdjustment") == true) {
            intArray[12] = 0x00
        } else {
            intArray[12] = 0x80
        }

        return intArray.foldIndexed(ByteArray(intArray.size)) { i, a, v ->
            a.apply {
                set(
                    i,
                    v.toByte()
                )
            }
        }
    }
}