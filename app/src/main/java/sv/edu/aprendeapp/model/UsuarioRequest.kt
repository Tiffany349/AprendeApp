package sv.edu.aprendeapp.model

data class UsuarioRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: String
)
