
package sv.edu.aprendeapp

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import sv.edu.aprendeapp.databinding.ActivityAccesoBinding
import sv.edu.aprendeapp.model.Usuario
import sv.edu.aprendeapp.model.UsuarioRequest
import sv.edu.aprendeapp.model.api.RetrofitClient

class AccesoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccesoBinding

    private var modoRegistro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccesoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SesionManager(this).haySesion()) {
            abrirAplicacion()
            return
        }

        actualizarModo()

        binding.btnCambiarModo.setOnClickListener {
            modoRegistro = !modoRegistro
            limpiarCampos()
            actualizarModo()
        }

        binding.btnAcceder.setOnClickListener {
            if (modoRegistro) {
                registrarUsuario()
            } else {
                iniciarSesion()
            }
        }
    }

    private fun actualizarModo() {
        if (modoRegistro) {
            binding.tvTituloAcceso.text =
                getString(R.string.auth_register_title)

            binding.tvDescripcionAcceso.text =
                getString(R.string.auth_register_description)

            binding.tilNombre.visibility = View.VISIBLE
            binding.tilConfirmarPassword.visibility = View.VISIBLE
            binding.tvReglasPassword.visibility = View.VISIBLE

            binding.btnAcceder.text =
                getString(R.string.auth_register_button)

            binding.btnCambiarModo.text =
                getString(R.string.auth_go_login)

        } else {
            binding.tvTituloAcceso.text =
                getString(R.string.auth_login_title)

            binding.tvDescripcionAcceso.text =
                getString(R.string.auth_login_description)

            binding.tilNombre.visibility = View.GONE
            binding.tilConfirmarPassword.visibility = View.GONE
            binding.tvReglasPassword.visibility = View.GONE

            binding.btnAcceder.text =
                getString(R.string.auth_login_button)

            binding.btnCambiarModo.text =
                getString(R.string.auth_go_register)
        }
    }

    private fun limpiarCampos() {
        binding.etNombre.text?.clear()
        binding.etCorreo.text?.clear()
        binding.etPassword.text?.clear()
        binding.etConfirmarPassword.text?.clear()
    }

    private fun iniciarSesion() {
        val correo = binding.etCorreo.text
            ?.toString()
            ?.trim()
            ?.lowercase()
            .orEmpty()

        val password = binding.etPassword.text
            ?.toString()
            .orEmpty()

        if (correo.isBlank() ||
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches()
        ) {
            mostrarMensaje(getString(R.string.auth_enter_email))
            return
        }

        if (password.isBlank()) {
            mostrarMensaje(getString(R.string.auth_enter_password))
            return
        }

        RetrofitClient.usuarioApi.obtenerUsuarios()
            .enqueue(object : Callback<List<Usuario>> {

                override fun onResponse(
                    call: Call<List<Usuario>>,
                    response: Response<List<Usuario>>
                ) {
                    if (response.isSuccessful) {
                        val usuarios = response.body().orEmpty()

                        val usuarioEncontrado = usuarios.firstOrNull {
                            it.correo.equals(correo, ignoreCase = true) &&
                                    it.password == password
                        }

                        if (usuarioEncontrado != null) {
                            SesionManager(this@AccesoActivity)
                                .guardarSesion(usuarioEncontrado)

                            mostrarMensaje(
                                getString(R.string.auth_login_success)
                            )

                            abrirAplicacion()
                        } else {
                            mostrarMensaje(
                                getString(R.string.auth_invalid_credentials)
                            )
                        }
                    } else {
                        mostrarMensaje(
                            getString(
                                R.string.auth_server_error,
                                response.code()
                            )
                        )
                    }
                }

                override fun onFailure(
                    call: Call<List<Usuario>>,
                    t: Throwable
                ) {
                    mostrarMensaje(
                        getString(R.string.auth_connection_error)
                    )
                }
            })
    }

    private fun registrarUsuario() {
        val nombre = binding.etNombre.text
            ?.toString()
            ?.trim()
            .orEmpty()

        val correo = binding.etCorreo.text
            ?.toString()
            ?.trim()
            ?.lowercase()
            .orEmpty()

        val password = binding.etPassword.text
            ?.toString()
            .orEmpty()

        val confirmarPassword = binding.etConfirmarPassword.text
            ?.toString()
            .orEmpty()

        if (nombre.isBlank()) {
            mostrarMensaje(getString(R.string.auth_enter_name))
            return
        }

        if (correo.isBlank() ||
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches()
        ) {
            mostrarMensaje(getString(R.string.auth_enter_email))
            return
        }

        if (!passwordValida(password)) {
            mostrarMensaje(
                getString(R.string.auth_invalid_password)
            )
            return
        }

        if (password != confirmarPassword) {
            mostrarMensaje(
                getString(R.string.auth_password_mismatch)
            )
            return
        }

        RetrofitClient.usuarioApi.obtenerUsuarios()
            .enqueue(object : Callback<List<Usuario>> {

                override fun onResponse(
                    call: Call<List<Usuario>>,
                    response: Response<List<Usuario>>
                ) {
                    if (!response.isSuccessful) {
                        mostrarMensaje(
                            getString(
                                R.string.auth_server_error,
                                response.code()
                            )
                        )
                        return
                    }

                    val usuarios = response.body().orEmpty()

                    val correoExiste = usuarios.any {
                        it.correo.equals(correo, ignoreCase = true)
                    }

                    if (correoExiste) {
                        mostrarMensaje(
                            getString(R.string.auth_email_exists)
                        )
                        return
                    }

                    guardarNuevoUsuario(
                        nombre,
                        correo,
                        password
                    )
                }

                override fun onFailure(
                    call: Call<List<Usuario>>,
                    t: Throwable
                ) {
                    mostrarMensaje(
                        getString(R.string.auth_connection_error)
                    )
                }
            })
    }

    private fun guardarNuevoUsuario(
        nombre: String,
        correo: String,
        password: String
    ) {
        val nuevoUsuario = UsuarioRequest(
            nombre = nombre,
            correo = correo,
            password = password,
            rol = "usuario"
        )

        RetrofitClient.usuarioApi.registrarUsuario(nuevoUsuario)
            .enqueue(object : Callback<Usuario> {

                override fun onResponse(
                    call: Call<Usuario>,
                    response: Response<Usuario>
                ) {
                    if (response.isSuccessful) {
                        val usuarioCreado = response.body()

                        if (usuarioCreado == null) {
                            mostrarMensaje(
                                getString(R.string.auth_empty_response)
                            )
                            return
                        }

                        SesionManager(this@AccesoActivity)
                            .guardarSesion(usuarioCreado)

                        mostrarMensaje(
                            getString(R.string.auth_register_success)
                        )

                        abrirAplicacion()
                    } else {
                        mostrarMensaje(
                            getString(
                                R.string.auth_server_error,
                                response.code()
                            )
                        )
                    }
                }

                override fun onFailure(
                    call: Call<Usuario>,
                    t: Throwable
                ) {
                    mostrarMensaje(
                        getString(R.string.auth_connection_error)
                    )
                }
            })
    }

    private fun passwordValida(password: String): Boolean {
        val tieneMayuscula = password.any { it.isUpperCase() }
        val tieneMinuscula = password.any { it.isLowerCase() }
        val tieneNumero = password.any { it.isDigit() }

        val caracteresEspeciales = "!@#$%^&*"

        val tieneEspecial = password.any {
            it in caracteresEspeciales
        }

        return password.length >= 12 &&
                tieneMayuscula &&
                tieneMinuscula &&
                tieneNumero &&
                tieneEspecial
    }

    private fun abrirAplicacion() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
}
