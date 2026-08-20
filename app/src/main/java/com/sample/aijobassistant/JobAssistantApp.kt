package com.sample.aijobassistant

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.
 *
 * Two responsibilities live here intentionally:
 * 1. [HiltAndroidApp] bootstraps the Hilt dependency graph for the whole app.
 * 2. PDFBox-Android requires its resource loader to be initialized once,
 *    before any PDF parsing happens anywhere in the app. Doing it here
 *    guarantees it happens exactly once, before any screen could possibly
 *    need it.
 */
@HiltAndroidApp
class JobAssistantApp : Application() {

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
    }
}
