package sv.edu.aprendeapp

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sv.edu.aprendeapp.databinding.ActivityMainBinding
import sv.edu.aprendeapp.model.Recurso
import sv.edu.aprendeapp.model.api.RetrofitClient
import sv.edu.aprendeapp.view.RecursoAdapter
import java.text.Normalizer
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var todosLosRecursos: List<Recurso> = emptyList()
    private lateinit var recursoAdapter: RecursoAdapter

    private var textoBusqueda: String = ""
    private var tipoSeleccionado: String = ""
    private var ordenSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recursoAdapter = RecursoAdapter(emptyList())

        binding.rvRecursos.layoutManager =
            LinearLayoutManager(this)

        binding.rvRecursos.adapter = recursoAdapter

        configurarBuscador()
        configurarFiltros()

        cargarRecursos()
    }

    private fun configurarBuscador() {
        binding.searchRecursos.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    textoBusqueda = query.orEmpty()
                    aplicarFiltros()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    textoBusqueda = newText.orEmpty()
                    aplicarFiltros()
                    return true
                }
            }
        )
    }

    private fun configurarFiltros() {
        binding.spFiltroTipo.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    tipoSeleccionado =
                        parent?.getItemAtPosition(position)?.toString()
                            .orEmpty()

                    aplicarFiltros()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                    tipoSeleccionado = ""
                }
            }

        binding.spOrden.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    ordenSeleccionado =
                        parent?.getItemAtPosition(position)?.toString()
                            .orEmpty()

                    aplicarFiltros()
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                    ordenSeleccionado = ""
                }
            }
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
                        todosLosRecursos = response.body().orEmpty()

                        if (todosLosRecursos.isEmpty()) {
                            mostrarEstado(
                                getString(R.string.resources_empty)
                            )
                        } else {
                            aplicarFiltros()
                        }
                    } else {
                        mostrarEstado(
                            "${getString(R.string.resources_server_error)} " +
                                    "HTTP ${response.code()}"
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

    private fun aplicarFiltros() {
        if (!::recursoAdapter.isInitialized) return

        val busqueda = normalizar(textoBusqueda.trim())
        val tipoFiltro = normalizar(tipoSeleccionado)

        var resultado = todosLosRecursos.filter { recurso ->

            val coincideBusqueda =
                busqueda.isBlank() ||
                        normalizar(recurso.id).contains(busqueda) ||
                        normalizar(recurso.titulo).contains(busqueda) ||
                        normalizar(recurso.tipo).contains(busqueda)

            val esTodosLosTipos =
                tipoSeleccionado.isBlank() ||
                        tipoFiltro == normalizar(
                    getString(R.string.all_resource_types)
                )

            val coincideTipo =
                esTodosLosTipos ||
                        normalizar(recurso.tipo) == tipoFiltro ||
                        (
                                tipoFiltro == normalizar("Artículo") &&
                                        normalizar(recurso.tipo) == normalizar("Articulo")
                                )

            coincideBusqueda && coincideTipo
        }

        resultado = when (ordenSeleccionado) {
            "Título A-Z" ->
                resultado.sortedBy { normalizar(it.titulo) }

            "Título Z-A" ->
                resultado.sortedByDescending { normalizar(it.titulo) }

            "Tipo" ->
                resultado.sortedBy { normalizar(it.tipo) }

            else -> resultado
        }

        recursoAdapter.actualizarRecursos(resultado)

        if (resultado.isEmpty()) {
            mostrarEstado(getString(R.string.no_search_results))
        } else {
            binding.tvEstado.visibility = View.GONE
            binding.rvRecursos.visibility = View.VISIBLE
        }
    }

    private fun normalizar(texto: String): String {
        return Normalizer.normalize(
            texto.lowercase(Locale.ROOT),
            Normalizer.Form.NFD
        ).replace("\\p{Mn}+".toRegex(), "")
    }

    private fun mostrarEstado(mensaje: String) {
        binding.tvEstado.text = mensaje
        binding.tvEstado.visibility = View.VISIBLE
        binding.rvRecursos.visibility = View.GONE
    }
}
