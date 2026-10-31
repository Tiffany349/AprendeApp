
package sv.edu.aprendeapp

import android.content.Context
import android.util.Base64
import java.util.Locale

class RecursoPreferencias(
    context: Context,
    correo: String
) {

    private val preferencias = context.getSharedPreferences(
        "AprendeAppRecursos_" +
                Base64.encodeToString(
                    correo.trim()
                        .lowercase(Locale.ROOT)
                        .toByteArray(Charsets.UTF_8),
                    Base64.URL_SAFE or
                            Base64.NO_WRAP or
                            Base64.NO_PADDING
                ),
        Context.MODE_PRIVATE
    )

    fun esFavorito(recursoId: String): Boolean {
        return preferencias.getBoolean(
            "favorito_$recursoId",
            false
        )
    }

    fun alternarFavorito(recursoId: String): Boolean {
        val nuevoEstado = !esFavorito(recursoId)

        preferencias.edit()
            .putBoolean("favorito_$recursoId", nuevoEstado)
            .apply()

        return nuevoEstado
    }

    fun obtenerCalificacion(recursoId: String): Float {
        return preferencias.getFloat(
            "calificacion_$recursoId",
            0f
        )
    }

    fun guardarCalificacion(
        recursoId: String,
        calificacion: Float
    ) {
        preferencias.edit()
            .putFloat(
                "calificacion_$recursoId",
                calificacion.coerceIn(0f, 5f)
            )
            .apply()
    }
}
