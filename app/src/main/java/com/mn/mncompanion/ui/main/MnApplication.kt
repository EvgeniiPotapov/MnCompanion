package com.mn.mncompanion.ui.main

import android.app.Application
import com.mn.mncompanion.koinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MnApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MnApplication)
            modules(koinModule)
        }
    }
}