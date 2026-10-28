
package sv.edu.aprendeapp.model.api

import retrofit2.Call
import retrofit2.http.GET
import sv.edu.aprendeapp.model.Recurso

interface RecursoApi {

    @GET("recursos")
    fun obtenerRecursos(): Call<List<Recurso>>
}
