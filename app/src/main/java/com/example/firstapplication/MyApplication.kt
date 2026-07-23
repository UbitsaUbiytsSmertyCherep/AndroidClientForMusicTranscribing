package com.example.firstapplication

import android.app.Application
import com.example.firstapplication.ServiceLocator

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}