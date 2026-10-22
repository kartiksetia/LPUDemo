package com.example.demolpu

import android.app.Application
import com.google.firebase.FirebaseApp

class StudyLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Without project configuration, keep the clearly labelled offline sample usable.
        if (FirebaseApp.initializeApp(this) != null) AppCheckSetup.install()
    }
}
