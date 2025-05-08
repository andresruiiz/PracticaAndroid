package es.andresruiz.core.utils

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import jakarta.inject.Inject

/**
 * Proveedor de recursos para acceder a los strings desde ViewModels
 */
@ViewModelScoped
class ResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Obtiene un string de los recursos por su ID
     */
    fun getString(@StringRes resourceId: Int): String {
        return context.getString(resourceId)
    }
}