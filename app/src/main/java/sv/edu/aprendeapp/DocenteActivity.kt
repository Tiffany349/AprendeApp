
package sv.edu.aprendeapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sv.edu.aprendeapp.databinding.ActivityDocenteBinding
import sv.edu.aprendeapp.model.Recurso
import sv.edu.aprendeapp.model.RecursoRequest
import sv.edu.aprendeapp.model.api.RetrofitClient
import sv.edu.aprendeapp.view.AdminRecursoAdapter

class DocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocenteBinding
    private lateinit var recursoAdapter: AdminRecursoAdapter
    private lateinit var sesionManager: SesionManager

    private var recursos: List<Recurso> = emptyList()
    private var recursoEditando: Recurso? = null
    private var solicitudEnCurso = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Comprobar la sesión y el rol antes de mostrar la pantalla.
        sesionManager = SesionManager(this)

        if (!sesionManager.haySesion()) {
            val intent = Intent(this, AccesoActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
            return
        }

        if (!sesionManager.esDocente()) {
            Toast.makeText(
                this,
                getString(R.string.admin_permission_denied),
                Toast.LENGTH_LONG
            ).show()

            finish()
            return
        }

        // Mostrar la pantalla de administración.
        binding = ActivityDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVolver.setOnClickListener {
            finish()
        }

        // Configurar opciones del tipo de recurso.
        val tipos = resources.getStringArray(
            R.array.resource_type_options
        )

        val adaptadorTipos = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            tipos
        )

        binding.etTipo.setAdapter(adaptadorTipos)

        // Configurar adaptador de recursos.
        recursoAdapter = AdminRecursoAdapter(
            emptyList(),
            alEditar = { recurso ->
                prepararEdicion(recurso)
            },
            alEliminar = { recurso ->
                confirmarEliminacion(recurso)
            }
        )

        binding.rvAdminRecursos.layoutManager =
            LinearLayoutManager(this)

        binding.rvAdminRecursos.adapter = recursoAdapter

        // Botón para guardar o actualizar recursos.
        binding.btnGuardarRecurso.setOnClickListener {
            guardarRecurso()
        }

        // Botón para cancelar la edición.
        binding.btnCancelarEdicion.setOnClickListener {
            limpiarFormulario()
        }

        cargarRecursos()
    }

    private fun cargarRecursos() {
        RetrofitClient.api.obtenerRecursos()
            .enqueue(object : Callback<List<Recurso>> {

                override fun onResponse(
                    call: Call<List<Recurso>>,
                    response: Response<List<Recurso>>
                ) {
                    if (response.isSuccessful) {
                        recursos = response.body().orEmpty()
                        recursoAdapter.actualizarLista(recursos)
                    } else {
                        mensaje(
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
                    mensaje(
                        getString(R.string.admin_connection_load_error)
                    )
                }
            })
    }

    private fun guardarRecurso() {
        if (solicitudEnCurso) return

        val titulo = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val tipo = binding.etTipo.text.toString().trim()
        val enlace = binding.etEnlace.text.toString().trim()
        val imagen = binding.etImagen.text.toString().trim()

        if (titulo.isBlank() ||
            descripcion.isBlank() ||
            tipo.isBlank()
        ) {
            mensaje(
                getString(R.string.admin_complete_required_fields)
            )
            return
        }

        if (enlace.isNotBlank() &&
            !enlace.startsWith("https://") &&
            !enlace.startsWith("http://")
        ) {
            mensaje(
                getString(R.string.admin_invalid_link)
            )
            return
        }

        if (imagen.isNotBlank() &&
            !imagen.startsWith("https://") &&
            !imagen.startsWith("http://")
        ) {
            mensaje(
                getString(R.string.admin_invalid_image_url)
            )
            return
        }

        val solicitud = RecursoRequest(
            titulo = titulo,
            descripcion = descripcion,
            tipo = tipo,
            enlace = enlace,
            imagen = imagen
        )

        val recursoActual = recursoEditando

        solicitudEnCurso = true
        binding.btnGuardarRecurso.isEnabled = false

        val llamada = if (recursoActual == null) {
            RetrofitClient.api.crearRecurso(solicitud)
        } else {
            RetrofitClient.api.actualizarRecurso(
                recursoActual.id,
                solicitud
            )
        }

        llamada.enqueue(object : Callback<Recurso> {

            override fun onResponse(
                call: Call<Recurso>,
                response: Response<Recurso>
            ) {
                solicitudEnCurso = false
                binding.btnGuardarRecurso.isEnabled = true

                if (response.isSuccessful) {
                    mensaje(
                        if (recursoActual == null) {
                            getString(R.string.admin_resource_created)
                        } else {
                            getString(R.string.admin_resource_updated)
                        }
                    )

                    limpiarFormulario()
                    cargarRecursos()

                } else {
                    mensaje(
                        getString(
                            R.string.admin_save_error,
                            response.code()
                        )
                    )
                }
            }

            override fun onFailure(
                call: Call<Recurso>,
                t: Throwable
            ) {
                solicitudEnCurso = false
                binding.btnGuardarRecurso.isEnabled = true

                mensaje(
                    getString(R.string.admin_connection_save_error)
                )
            }
        })
    }

    private fun prepararEdicion(recurso: Recurso) {
        recursoEditando = recurso

        binding.etTitulo.setText(recurso.titulo)
        binding.etDescripcion.setText(recurso.descripcion)
        binding.etTipo.setText(recurso.tipo)
        binding.etEnlace.setText(recurso.enlace)
        binding.etImagen.setText(recurso.imagen)

        binding.btnGuardarRecurso.setText(
            R.string.update_resource
        )

        binding.btnCancelarEdicion.visibility = View.VISIBLE
        binding.etTitulo.requestFocus()
    }

    private fun confirmarEliminacion(recurso: Recurso) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_resource)
            .setMessage(
                getString(
                    R.string.admin_delete_confirmation,
                    recurso.titulo
                )
            )
            .setNegativeButton(R.string.cancel_action, null)
            .setPositiveButton(R.string.delete_action) { _, _ ->
                eliminarRecurso(recurso)
            }
            .show()
    }

    private fun eliminarRecurso(recurso: Recurso) {
        if (solicitudEnCurso) return

        solicitudEnCurso = true

        RetrofitClient.api.eliminarRecurso(recurso.id)
            .enqueue(object : Callback<Void> {

                override fun onResponse(
                    call: Call<Void>,
                    response: Response<Void>
                ) {
                    solicitudEnCurso = false

                    if (response.isSuccessful) {
                        mensaje(
                            getString(R.string.admin_resource_deleted)
                        )

                        if (recursoEditando?.id == recurso.id) {
                            limpiarFormulario()
                        }

                        cargarRecursos()

                    } else {
                        mensaje(
                            getString(
                                R.string.admin_delete_error,
                                response.code()
                            )
                        )
                    }
                }

                override fun onFailure(
                    call: Call<Void>,
                    t: Throwable
                ) {
                    solicitudEnCurso = false

                    mensaje(
                        getString(R.string.admin_connection_delete_error)
                    )
                }
            })
    }

    private fun limpiarFormulario() {
        recursoEditando = null

        binding.etTitulo.text?.clear()
        binding.etDescripcion.text?.clear()
        binding.etTipo.text?.clear()
        binding.etEnlace.text?.clear()
        binding.etImagen.text?.clear()

        binding.btnGuardarRecurso.setText(
            R.string.create_resource
        )

        binding.btnCancelarEdicion.visibility = View.GONE
    }

    private fun mensaje(texto: String) {
        Toast.makeText(
            this,
            texto,
            Toast.LENGTH_LONG
        ).show()
    }
}
