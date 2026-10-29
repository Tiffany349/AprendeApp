package sv.edu.aprendeapp.model.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import sv.edu.aprendeapp.model.Recurso
import sv.edu.aprendeapp.model.RecursoRequest

interface RecursoApi {

    @GET("recursos")
    fun obtenerRecursos(): Call<List<Recurso>>

    @POST("recursos")
    fun crearRecurso(
        @Body recurso: RecursoRequest
    ): Call<Recurso>

    @PUT("recursos/{id}")
    fun actualizarRecurso(
        @Path("id") id: String,
        @Body recurso: RecursoRequest
    ): Call<Recurso>

    @DELETE("recursos/{id}")
    fun eliminarRecurso(
        @Path("id") id: String
    ): Call<Void>
}
