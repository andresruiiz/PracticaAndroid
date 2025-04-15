package es.andresruiz.practicaandroid

import android.app.Application
import es.andresruiz.data_retrofit.network.RetrofitInstance

class PracticaAndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar la instancia de Retrofit/Retromock
        RetrofitInstance.init(this)
    }
}