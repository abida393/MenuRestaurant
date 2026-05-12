package com.savoria.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SavoriaApplication : Application() {
    // A scope that lives for the entire lifecycle of the application
    val applicationScope = CoroutineScope(SupervisorJob())
}
