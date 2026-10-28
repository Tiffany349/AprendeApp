
package sv.edu.aprendeapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sv.edu.aprendeapp.databinding.ActivityMainBinding
import sv.edu.aprendeapp.model.Recurso
import sv.edu.aprendeapp.model.api.RetrofitClient
import sv.edu.aprendeapp.view.RecursoAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvRecursos.layoutManager =
            LinearLayoutManager(this)

        cargarRecursos()
    }

    private fun cargarRecursos() {
        mostrarEstado(getString(R.string.loading_resources))
        binding.progressBar.visibility = View.VISIBLE

        RetrofitClient.api.obtenerRecursos()
            .enqueue(object : Callback<List<Recurso>> {

                override fun onResponse(
                    call: Call<List<Recurso>>,
                    response: Response<List<Recurso>>
                ) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val recursos = response.body().orEmpty()

                        if (recursos.isEmpty()) {
                            mostrarEstado(
                                getString(R.string.resources_empty)
                            )
                        } else {
                            binding.tvEstado.visibility = View.GONE
                            binding.rvRecursos.visibility = View.VISIBLE
                            binding.rvRecursos.adapter =
                                RecursoAdapter(recursos)
                        }
                    } else {
                        mostrarEstado(
                            "Error HTTP ${response.code()}: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<Recurso>>,
                    t: Throwable
                ) {
                    binding.progressBar.visibility = View.GONE

                    mostrarEstado(
                        getString(R.string.resources_load_error)
                    )
                }
            })
    }

    private fun mostrarEstado(mensaje: String) {
        binding.tvEstado.text = mensaje
        binding.tvEstado.visibility = View.VISIBLE
        binding.rvRecursos.visibility = View.GONE
    }
}
