package com.aodstudio.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * AOD Studio Application class.
 * Annotated with @HiltAndroidApp to trigger Hilt code generation
 * and provide the application-level dependency container.
 */
@HiltAndroidApp
class AODStudioApp : Application()
