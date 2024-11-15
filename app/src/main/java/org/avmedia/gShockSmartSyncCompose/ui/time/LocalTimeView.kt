package org.avmedia.gShockSmartSyncCompose.ui.time

import AppText
import AppTextLarge
import RealTimeClock
import TimeFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.avmedia.gShockSmartSyncCompose.R
import org.avmedia.gShockSmartSyncCompose.ui.common.AppButton
import org.avmedia.gShockSmartSyncCompose.ui.common.AppCard
import java.util.TimeZone
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun LocalTimeView(
    modifier: Modifier,
    timeModel: TimeViewModel = viewModel()
) {
    AppCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First Column: Local Time and TimeZoneTextView
            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(horizontal = 12.dp),
            ) {
                AppTextLarge(
                    modifier = Modifier.padding(start = 6.dp),
                    text = stringResource(id = R.string.local_time),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextClockComposable(
                    modifier = Modifier.align(Alignment.Start)
                )

                Box {
                    TimeZoneTextView(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 6.dp),
                        textSize = 16.sp
                    )
                }
            }

            // Second Column: Send Time Button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SendTimeButton(
                    modifier = Modifier
                        .padding(5.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    text = stringResource(id = R.string.send_to_watch),
                    onClick = {
                        timeModel.sendTimeToWatch()
                    }
                )
            }
        }
    }
}

@Composable
fun TextClockComposable(
    modifier: Modifier = Modifier,
    format: TimeFormat = TimeFormat.TwentyFourHour
) {
    RealTimeClock(modifier = modifier.padding(start = 6.dp))
}

@Composable
fun TimeZoneTextView(modifier: Modifier = Modifier, textSize: TextUnit) {
    // Use remember to store the time zone ID in a state variable
    var timeZoneId by remember { mutableStateOf(TimeZone.getDefault().id) }

    // LaunchedEffect to monitor changes in the time zone and update the state
    LaunchedEffect(Unit) {
        while (true) {
            val newTimeZoneId = TimeZone.getDefault().id
            timeZoneId = newTimeZoneId

            // Check for changes periodically
            kotlinx.coroutines.delay(1000L) // Adjust the interval as needed
        }
    }

    // Display the text with the current time zone ID
    AppText(
        text = timeZoneId,
        fontSize = textSize,
        modifier = modifier
    )
}

@Composable
fun SendTimeButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    AppButton(
        onClick = {
            onClick()
        },
        text = text
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLocalTimeCard() {
    LocalTimeView(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(vertical = 0.dp) // Adjust padding as needed
    )
}