
package sv.edu.aprendeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
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
    private lateinit var recursoAdapter: RecursoAdapter
    private lateinit var sesionManager: SesionManager
    private lateinit var preferenciasRecursos: RecursoPreferencias

    private var todosLosRecursos: List<Recurso> = emptyList()
    private var textoBusqueda = ""
    private var tipoSeleccionado = ""
    private var ordenSeleccionado = ""
    private var soloFavoritos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesionManager = SesionManager(this)

        if (!sesionManager.haySesion()) {
            irAAcceso()
            return
        }

        // Crear almacenamiento de preferencias para la cuenta actual.
        preferenciasRecursos = RecursoPreferencias(
            this,
            sesionManager.obtenerCorreo()
        )

        // Mostrar administración solamente a docentes.
        actualizarAccesoAdministracion()

        // Configurar la lista de recursos.
        recursoAdapter = RecursoAdapter(
            emptyList(),
            preferenciasRecursos
        ) {
            aplicarFiltros()
        }

        binding.rvRecursos.layoutManager =
            LinearLayoutManager(this)

        binding.rvRecursos.adapter = recursoAdapter

        // Abrir administración de recursos.
        binding.btnAdministrarRecursos.setOnClickListener {
            if (sesionManager.esDocente()) {
                startActivity(
                    Intent(this, DocenteActivity::class.java)
                )
            } else {
                AlertDialog.Builder(this)
                    .setMessage(
                        "No tienes permisos para administrar recursos."
                    )
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }

        // Cerrar sesión.
        binding.btnCerrarSesion.setOnClickListener {
            confirmarCerrarSesion()
        }

        // Mostrar favoritos o todos los recursos.
        binding.btnMostrarFavoritos.setOnClickListener {
            soloFavoritos = !soloFavoritos

            actualizarTextoBotonFavoritos()
            aplicarFiltros()
        }

        configurarBuscador()
        configurarFiltros()
        cargarRecursos()
    }

    override fun onResume() {
        super.onResume()

        if (::binding.isInitialized &&
            ::recursoAdapter.isInitialized
        ) {
            actualizarAccesoAdministracion()
            cargarRecursos()
        }
    }

    private fun actualizarAccesoAdministracion() {
        binding.btnAdministrarRecursos.visibility =
            if (sesionManager.esDocente()) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun actualizarTextoBotonFavoritos() {
        binding.btnMostrarFavoritos.text =
            if (soloFavoritos) {
                getString(R.string.favorites_show_all)
            } else {
                getString(R.string.favorites_show)
            }
    }

    private fun confirmarCerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_confirmation_title)
            .setMessage(R.string.logout_confirmation_message)
            .setNegativeButton(R.string.logout_cancel, null)
            .setPositiveButton(R.string.logout) { _, _ ->
                sesionManager.cerrarSesion()
                irAAcceso()
            }
            .show()
    }

    private fun irAAcceso() {
        val intent = Intent(this, AccesoActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
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
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    tipoSeleccionado =
                        parent?.getItemAtPosition(position)
                            ?.toString()
                            .orEmpty()

                    aplicarFiltros()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    tipoSeleccionado = ""
                }
            }

        binding.spOrden.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    ordenSeleccionado =
                        parent?.getItemAtPosition(position)
                            ?.toString()
                            .orEmpty()

                    aplicarFiltros()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    ordenSeleccionado = ""
                }
            }
    }

    private fun cargarRecursos() {
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
                            "${getString(R.string.resources_server_error)} HTTP ${response.code()}"
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
        val todosTipos = normalizar(
            getString(R.string.all_resource_types)
        )

        var resultado = todosLosRecursos.filter { recurso ->

            val coincideBusqueda =
                busqueda.isBlank() ||
                        normalizar(recurso.id).contains(busqueda) ||
                        normalizar(recurso.titulo).contains(busqueda) ||
                        normalizar(recurso.tipo).contains(busqueda)

            val coincideTipo =
                tipoFiltro.isBlank() ||
                        tipoFiltro == todosTipos ||
                        normalizar(recurso.tipo) == tipoFiltro

            coincideBusqueda && coincideTipo
        }

        // Aplicar el filtro de favoritos de la cuenta actual.
        if (soloFavoritos) {
            resultado = resultado.filter { recurso ->
                preferenciasRecursos.esFavorito(recurso.id)
            }
        }

        // Aplicar ordenamiento.
        resultado = when (ordenSeleccionado) {
            "Título A-Z" ->
                resultado.sortedBy {
                    normalizar(it.titulo)
                }

            "Título Z-A" ->
                resultado.sortedByDescending {
                    normalizar(it.titulo)
                }

            "Tipo" ->
                resultado.sortedBy {
                    normalizar(it.tipo)
                }

            else -> resultado
        }

        recursoAdapter.actualizarRecursos(resultado)

        if (resultado.isEmpty()) {
            mostrarEstado(
                if (soloFavoritos) {
                    getString(R.string.favorites_empty)
                } else {
                    getString(R.string.no_search_results)
                }
            )
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
