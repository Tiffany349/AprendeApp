
package sv.edu.aprendeapp.model.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import sv.edu.aprendeapp.model.Usuario
import sv.edu.aprendeapp.model.UsuarioRequest

interface UsuarioApi {

    @GET("usuarios")
    fun obtenerUsuarios(): Call<List<Usuario>>

    @POST("usuarios")
    fun registrarUsuario(
        @Body usuario: UsuarioRequest
    ): Call<Usuario>
}
