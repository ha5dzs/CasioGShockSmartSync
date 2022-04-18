/*
 * Created by Ivo Zivkov (izivkov@gmail.com) on 2022-04-03, 6:13 p.m.
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 2022-04-03, 6:13 p.m.
 */

package org.avmedia.gShockPhoneSync.customComponents

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

object CalenderEvents {
    private var permissionGranted: Boolean = false

    @SuppressLint("Range")
    fun getDataFromEventTable(context: Context): ArrayList<EventsData.Event> {
        return getEvents(context)
    }

    private fun getEvents(context: Context): ArrayList<EventsData.Event> {
        val events: ArrayList<EventsData.Event> = ArrayList()

        var cur: Cursor? = null
        val cr: ContentResolver = context.contentResolver

        val mProjection = arrayOf(
            "_id",
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.HAS_ALARM,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.ALL_DAY,
        )

        var calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR, 0)

        val selection: String =
            """
            ${CalendarContract.Events.HAS_ALARM} = "1"
            and (${CalendarContract.Events.DTSTART} >= ${calendar.timeInMillis}
            or ${CalendarContract.Events.RRULE} IS NOT NULL)
            """

        val selectionArgs: Array<String>? = null

        val uri: Uri = CalendarContract.Events.CONTENT_URI
        cur = cr.query(
            uri,
            mProjection,
            selection,
            selectionArgs,
            null)

        while (cur!!.moveToNext()) {
            val title: String? = cur.getString(cur.getColumnIndex(CalendarContract.Events.TITLE))
            val dateStart: String? =
                cur.getString(cur.getColumnIndex(CalendarContract.Events.DTSTART))
            var dateEnd: String? =
                cur.getString(cur.getColumnIndex(CalendarContract.Events.DTEND))
            val rrule: String? = cur.getString(cur.getColumnIndex(CalendarContract.Events.RRULE))
            val allDay: String? =
                cur.getString(cur.getColumnIndex(CalendarContract.Events.ALL_DAY))

            var zone = ZoneId.systemDefault()
            if (allDay == "1") {
                zone = ZoneId.of("UTC")
            }

            val startDate = EventsData.createEventDate(dateStart!!.toLong(), zone)
            var endDate = startDate

            var (localEndDate, incompatible, daysOfWeek, repeatPeriod) =
                RRuleValues.getValues(rrule, startDate, zone)

            if (localEndDate != null) {
                endDate = EventsData.EventDate(
                    localEndDate.year,
                    localEndDate.month,
                    localEndDate.dayOfMonth
                )
            }

            val end = LocalDate.of(endDate.year!!, endDate.month!!, endDate.day!!)
            if (!startDate.equals(endDate) && end.isBefore(LocalDate.now())) {
                continue // do not add expired events
            }

            val selected = events.size < EventsData.MAX_REMINDERS
            if (title != null) {
                events.add(
                    EventsData.Event(
                        title!!,
                        startDate,
                        endDate,
                        repeatPeriod,
                        daysOfWeek,
                        true,
                        incompatible,
                        selected
                    )
                )
            }
        }

        return events
    }

    fun setGrated(grantResults: IntArray) {
        permissionGranted = grantResults[0] == 1
    }
}