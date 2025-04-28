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

    // Clave para el valor en DataStore
    private val useMockKey = booleanPreferencesKey("use_mock")

    // Variable para almacenar el estado actual
    private var currentMockState: Boolean = false

    override fun isMockEnabled(): Boolean {

        // En producción, siempre Retrofit (false)
        if (!BuildConfig.DEBUG) {
            return false
        }

        return currentMockState
    }

    override fun onCreate() {
        super.onCreate()

        runBlocking {
            currentMockState = dataStore.data.first()[useMockKey] ?: false
        }
    }

    // Método para actualizar el estado del sistema de mocks
    fun updateMockState(isEnabled: Boolean) {
        currentMockState = isEnabled
    }
}