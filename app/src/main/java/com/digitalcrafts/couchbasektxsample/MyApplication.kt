package com.digitalcrafts.couchbasektxsample

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {

        lateinit var application: Application
            private set
    }
}