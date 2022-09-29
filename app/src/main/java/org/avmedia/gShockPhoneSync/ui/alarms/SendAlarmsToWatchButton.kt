/*
 * Created by Ivo Zivkov (izivkov@gmail.com) on 2022-03-30, 12:06 a.m.
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 2022-03-22, 1:55 p.m.
 */

package org.avmedia.gShockPhoneSync.ui.alarms

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.avmedia.gShockPhoneSync.customComponents.Button
import org.avmedia.gShockPhoneSync.utils.Utils

class SendAlarmsToWatchButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Button(context, attrs, defStyleAttr) {

    init {
        setOnTouchListener(OnTouchListener())
        onState()
    }

    inner class OnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_UP -> {
                    if (AlarmsModel.alarms.isEmpty()) {
                        Utils.snackBar(context, "Cannot set alarms...unsupported watch?")
                        return false
                    }
                    sendMessage("{action: \"SET_ALARMS\", value: ${AlarmsModel.toJson()}}")
                    Utils.snackBar(context, "Alarms Sent to Watch")
                }
            }
            v?.performClick()
            return false
        }
    }
}
