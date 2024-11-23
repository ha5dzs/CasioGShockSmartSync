package org.avmedia.gshockGoogleSync.data.repository

import org.avmedia.gshockapi.GShockAPI
import org.avmedia.gshockapi.IGShockAPI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GShockRepository @Inject constructor(
    val api: GShockAPI
) : IGShockAPI by api {
    // Additional repository-specific logic can be added here if needed
}