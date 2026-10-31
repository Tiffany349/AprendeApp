package sv.edu.aprendeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sv.edu.aprendeapp.databinding.ActivityMainBinding
import sv.edu.aprendeapp.model.Calificacion
import sv.edu.aprendeapp.model.Recurso
import sv.edu.aprendeapp.model.Usuario
import sv.edu.aprendeapp.model.UsuarioCalificacionesRequest
import sv.edu.aprendeapp.model.api.RetrofitClient
import sv.edu.aprendeapp.view.RecursoAdapter
import java.text.Normalizer
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recursoAdapter: RecursoAdapter
    private lateinit var sesionManager: SesionManager
    private lateinit var preferenciasRecursos: RecursoPreferencias

    private val gson = Gson()

    private var todosLosRecursos: List<Recurso> = emptyList()
    private var textoBusqueda = ""
    private var tipoSeleccionado = ""
    private var ordenSeleccionado = ""
    private var soloFavoritos = false

    private val recursosCalificando = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sesionManager = SesionManager(this)

        if (!sesionManager.haySesion()) {
            irAAcceso()
            return
        }

        preferenciasRecursos = RecursoPreferencias(
            this,
            sesionManager.obtenerCorreo()
        )

        actualizarAccesoAdministracion()

        // Configurar el adaptador.
        recursoAdapter = RecursoAdapter(
            recursos = emptyList(),
            preferencias = preferenciasRecursos,
            usuarioId = sesionManager.obtenerId(),
            alCambiarFavorito = {
                aplicarFiltros()
            },
            alCalificar = { recurso, calificacion ->
                guardarCalificacion(recurso, calificacion)
            }
        )

        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = recursoAdapter

        // Acceso a la administración de recursos.
        binding.btnAdministrarRecursos.setOnClickListener {
            if (sesionManager.esDocente()) {
                startActivity(
                    Intent(this, DocenteActivity::class.java)
                )
            } else {
                AlertDialog.Builder(this)
                    .setMessage(R.string.admin_permission_denied)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }

        // Cerrar sesión.
        binding.btnCerrarSesion.setOnClickListener {
            confirmarCerrarSesion()
        }

        // Mostrar u ocultar favoritos.
        binding.btnMostrarFavoritos.setOnClickListener {
            soloFavoritos = !soloFavoritos
            actualizarTextoBotonFavoritos()
            aplicarFiltros()
        }

        configurarBuscador()
        configurarFiltros()
        actualizarTextoBotonFavoritos()
    }

    override fun onResume() {
        super.onResume()

        if (
            ::binding.isInitialized &&
            ::recursoAdapter.isInitialized
        ) {
            actualizarAccesoAdministracion()
            cargarRecursos()
            cargarCalificaciones()
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

    // Cargar los recursos desde MockAPI.
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
                            getString(
                                R.string.admin_load_error,
                                response.code()
                            )
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

    // Cargar las calificaciones almacenadas en cada usuario.
    private fun cargarCalificaciones() {
        RetrofitClient.usuarioApi.obtenerUsuarios()
            .enqueue(object : Callback<List<Usuario>> {

                override fun onResponse(
                    call: Call<List<Usuario>>,
                    response: Response<List<Usuario>>
                ) {
                    if (response.isSuccessful) {

                        val todasLasCalificaciones =
                            response.body().orEmpty().flatMap { usuario ->

                                leerCalificacionesUsuario(
                                    usuario.calificaciones
                                ).map { calificacion ->

                                    // El ID del usuario se obtiene del registro
                                    // que contiene sus calificaciones.
                                    calificacion.copy(
                                        usuarioId = usuario.id
                                    )
                                }
                            }

                        recursoAdapter.actualizarCalificaciones(
                            todasLasCalificaciones
                        )

                    } else {
                        recursoAdapter.actualizarCalificaciones(
                            emptyList()
                        )

                        mostrarAviso(
                            getString(R.string.rating_loading_error)
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<Usuario>>,
                    t: Throwable
                ) {
                    recursoAdapter.actualizarCalificaciones(
                        emptyList()
                    )

                    mostrarAviso(
                        getString(R.string.rating_connection_error)
                    )
                }
            })
    }

    // Convertir el texto JSON de un usuario a una lista de Calificacion.
    private fun leerCalificacionesUsuario(
        datos: String?
    ): List<Calificacion> {

        if (datos.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            gson.fromJson(
                datos,
                Array<Calificacion>::class.java
            )?.toList().orEmpty()

        } catch (e: Exception) {
            emptyList()
        }
    }

    // Guardar o actualizar la calificación de un usuario.
    private fun guardarCalificacion(
        recurso: Recurso,
        valor: Float
    ) {
        if (recursosCalificando.contains(recurso.id)) {
            return
        }

        val usuarioId = sesionManager.obtenerId()

        if (usuarioId.isBlank()) {
            mostrarAviso(
                getString(R.string.rating_user_error)
            )
            return
        }

        recursosCalificando.add(recurso.id)

        // Consultar los usuarios registrados.
        RetrofitClient.usuarioApi.obtenerUsuarios()
            .enqueue(object : Callback<List<Usuario>> {

                override fun onResponse(
                    call: Call<List<Usuario>>,
                    response: Response<List<Usuario>>
                ) {
                    if (!response.isSuccessful) {
                        recursosCalificando.remove(recurso.id)

                        mostrarAviso(
                            getString(
                                R.string.rating_save_error,
                                response.code()
                            )
                        )
                        return
                    }

                    // Buscar al usuario que inició sesión.
                    val usuario = response.body()
                        .orEmpty()
                        .firstOrNull {
                            it.id == usuarioId
                        }

                    if (usuario == null) {
                        recursosCalificando.remove(recurso.id)

                        mostrarAviso(
                            getString(R.string.rating_user_error)
                        )
                        return
                    }

                    // Obtener las calificaciones que ya tiene el usuario.
                    val calificaciones =
                        leerCalificacionesUsuario(
                            usuario.calificaciones
                        ).map {
                            it.copy(usuarioId = usuario.id)
                        }.toMutableList()

                    // Comprobar si ya calificó este recurso.
                    val posicion = calificaciones.indexOfFirst {
                        it.recursoId == recurso.id
                    }

                    if (posicion >= 0) {

                        // Actualizar la calificación existente.
                        calificaciones[posicion] =
                            calificaciones[posicion].copy(
                                calificacion = valor,
                                usuarioId = usuario.id
                            )

                    } else {

                        // Agregar una nueva calificación.
                        calificaciones.add(
                            Calificacion(
                                recursoId = recurso.id,
                                usuarioId = usuario.id,
                                calificacion = valor
                            )
                        )
                    }

                    // Convertir las calificaciones a formato JSON.
                    val solicitud = UsuarioCalificacionesRequest(
                        calificaciones = gson.toJson(calificaciones)
                    )

                    // Actualizar el campo calificaciones del usuario.
                    RetrofitClient.usuarioApi.actualizarCalificaciones(
                        usuario.id,
                        solicitud
                    ).enqueue(object : Callback<Usuario> {

                        override fun onResponse(
                            call: Call<Usuario>,
                            response: Response<Usuario>
                        ) {
                            recursosCalificando.remove(recurso.id)

                            if (response.isSuccessful) {
                                mostrarAviso(
                                    getString(R.string.rating_saved_message)
                                )

                                // Actualizar las calificaciones y los promedios.
                                cargarCalificaciones()

                            } else {
                                mostrarAviso(
                                    getString(
                                        R.string.rating_save_error,
                                        response.code()
                                    )
                                )
                            }
                        }

                        override fun onFailure(
                            call: Call<Usuario>,
                            t: Throwable
                        ) {
                            recursosCalificando.remove(recurso.id)

                            mostrarAviso(
                                getString(R.string.rating_connection_error)
                            )
                        }
                    })
                }

                override fun onFailure(
                    call: Call<List<Usuario>>,
                    t: Throwable
                ) {
                    recursosCalificando.remove(recurso.id)

                    mostrarAviso(
                        getString(R.string.rating_connection_error)
                    )
                }
            })
    }

    // Aplicar búsqueda, filtros, favoritos y ordenamiento.
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

        // Filtrar los recursos marcados como favoritos.
        if (soloFavoritos) {
            resultado = resultado.filter { recurso ->
                preferenciasRecursos.esFavorito(recurso.id)
            }
        }

        // Obtener las opciones de ordenamiento desde strings.xml.
        val opcionesOrden = resources.getStringArray(
            R.array.resource_sort_options
        )

        resultado = when (ordenSeleccionado) {

            opcionesOrden[1] ->
                resultado.sortedBy {
                    normalizar(it.titulo)
                }

            opcionesOrden[2] ->
                resultado.sortedByDescending {
                    normalizar(it.titulo)
                }

            opcionesOrden[3] ->
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

    // Normalizar texto para facilitar las búsquedas.
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

    private fun mostrarAviso(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }
}