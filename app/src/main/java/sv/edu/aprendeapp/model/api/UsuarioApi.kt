package sv.edu.aprendeapp.model.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import sv.edu.aprendeapp.model.Usuario
import sv.edu.aprendeapp.model.UsuarioCalificacionesRequest
import sv.edu.aprendeapp.model.UsuarioRequest

interface UsuarioApi {

    @GET("usuarios")
    fun obtenerUsuarios(): Call<List<Usuario>>

    @POST("usuarios")
    fun registrarUsuario(
        @Body usuario: UsuarioRequest
    ): Call<Usuario>

    @PATCH("usuarios/{id}")
    fun actualizarCalificaciones(
        @Path("id") id: String,
        @Body solicitud: UsuarioCalificacionesRequest
    ): Call<Usuario>
}