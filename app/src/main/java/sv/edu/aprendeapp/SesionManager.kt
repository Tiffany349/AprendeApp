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

    fun haySesion(): Boolean {
        return preferencias.getBoolean("sesion_activa", false)
    }

    fun obtenerNombre(): String {
        return preferencias.getString("nombre", "") ?: ""
    }

    fun obtenerCorreo(): String {
        return preferencias.getString("correo", "") ?: ""
    }

    fun obtenerRol(): String {
        return preferencias.getString("rol", "usuario") ?: "usuario"
    }

    fun esDocente(): Boolean {
        return obtenerRol().equals("docente", ignoreCase = true)
    }

    fun cerrarSesion() {
        preferencias.edit().clear().apply()
    }
}
