package es.andresruiz.practicaandroid

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp
import es.andresruiz.domain.UseMockProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Application.dataStore by preferencesDataStore(name = "settings")

@HiltAndroidApp
class PracticaAndroidApplication : Application(), UseMockProvider {

    override fun isMockEnabled(): Boolean {
        val key = booleanPreferencesKey("use_mock")
        return runBlocking {
            dataStore.data.first()[key] == true
        }
    }

    override fun onCreate() {
        super.onCreate()
    }
}