package com.example

import android.app.Application
import com.onesignal.OneSignal
import com.google.firebase.FirebaseApp

const val ONESIGNAL_APP_ID = "85a9b91c-9b4b-4986-8331-83a20ee4a1f2"

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase on launch to prevent IllegalStateException
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Enable OneSignal safely on launch
        try {
            OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
