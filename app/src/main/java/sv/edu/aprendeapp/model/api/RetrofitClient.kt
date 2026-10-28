
package sv.edu.aprendeapp.model.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://6ae182c1be3bc8fead9899dc.mockapi.io/apiv1/"

    val api: RecursoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecursoApi::class.java)
    }
}
