package sv.edu.aprendeapp.model

data class CalificacionRequest(
    val recursoId: String,
    val usuarioId: String,
    val calificacion: Float
)