package sv.edu.aprendeapp

import android.content.Context
import sv.edu.aprendeapp.model.Usuario

class SesionManager(context: Context) {

    private val preferencias = context.getSharedPreferences(
        "AprendeAppSesion",
        Context.MODE_PRIVATE
    )

    fun guardarSesion(usuario: Usuario) {
        preferencias.edit()
            .putString("id", usuario.id)
            .putString("nombre", usuario.nombre)
            .putString("correo", usuario.correo)
            .putString("rol", usuario.rol)
            .putBoolean("sesion_activa", true)
            .apply()
    }

    fun haySesion(): Boolean =
        preferencias.getBoolean("sesion_activa", false)

    fun obtenerId(): String =
        preferencias.getString("id", "") ?: ""

    fun obtenerNombre(): String =
        preferencias.getString("nombre", "") ?: ""

    fun obtenerCorreo(): String =
        preferencias.getString("correo", "") ?: ""

    fun obtenerRol(): String =
        preferencias.getString("rol", "estudiante") ?: "estudiante"

    fun esDocente(): Boolean =
        obtenerRol().equals("docente", ignoreCase = true)

    fun cerrarSesion() {
        preferencias.edit().clear().apply()
    }
}