package com.example

import android.app.Application
import com.example.data.local.NovaDatabase
import com.example.data.repository.NovaRepository

class NovaApplication : Application() {
    val database by lazy { NovaDatabase.getDatabase(this) }
    val repository by lazy { NovaRepository(database.novaDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NovaApplication
            private set
    }
}
