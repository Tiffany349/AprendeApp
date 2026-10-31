package sv.edu.aprendeapp.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import sv.edu.aprendeapp.R
import sv.edu.aprendeapp.RecursoPreferencias
import sv.edu.aprendeapp.model.Calificacion
import sv.edu.aprendeapp.model.Recurso

class RecursoAdapter(
    private var recursos: List<Recurso>,
    private val preferencias: RecursoPreferencias,
    private val usuarioId: String,
    private val alCambiarFavorito: () -> Unit,
    private val alCalificar: (Recurso, Float) -> Unit
) : RecyclerView.Adapter<RecursoAdapter.RecursoViewHolder>() {

    private var calificaciones: List<Calificacion> = emptyList()

    class RecursoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tarjeta: MaterialCardView =
            itemView.findViewById(R.id.cardRecurso)

        val imagen: ImageView =
            itemView.findViewById(R.id.ivImagen)

        val titulo: TextView =
            itemView.findViewById(R.id.tvTitulo)

        val descripcion: TextView =
            itemView.findViewById(R.id.tvDescripcion)

        val tipo: TextView =
            itemView.findViewById(R.id.tvTipo)

        val enlace: MaterialButton =
            itemView.findViewById(R.id.btnEnlace)

        val favorito: MaterialButton =
            itemView.findViewById(R.id.btnFavorito)

        val calificacion: RatingBar =
            itemView.findViewById(R.id.rbCalificacion)

        val textoCalificacion: TextView =
            itemView.findViewById(R.id.tvCalificacion)

        val promedioCalificacion: TextView =
            itemView.findViewById(R.id.tvPromedioCalificacion)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecursoViewHolder {

        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recurso, parent, false)

        return RecursoViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: RecursoViewHolder,
        position: Int
    ) {
        val recurso = recursos[position]
        val context = holder.itemView.context

        holder.titulo.text = recurso.titulo
        holder.descripcion.text = recurso.descripcion

        holder.tipo.text = context.getString(
            R.string.resource_type_format,
            recurso.tipo
        )

        if (recurso.imagen.isNotBlank()) {
            Picasso.get()
                .load(recurso.imagen)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .fit()
                .centerCrop()
                .into(holder.imagen)
        } else {
            Picasso.get().cancelRequest(holder.imagen)
            holder.imagen.setImageResource(
                android.R.drawable.ic_menu_gallery
            )
        }

        actualizarBotonFavorito(holder, recurso.id)

        holder.favorito.setOnClickListener {
            preferencias.alternarFavorito(recurso.id)
            actualizarBotonFavorito(holder, recurso.id)

            Toast.makeText(
                context,
                if (preferencias.esFavorito(recurso.id)) {
                    R.string.favorite_added_message
                } else {
                    R.string.favorite_removed_message
                },
                Toast.LENGTH_SHORT
            ).show()

            alCambiarFavorito()
        }

        // Obtener la calificación del usuario conectado.
        val calificacionUsuario = calificaciones.firstOrNull {
            it.recursoId == recurso.id &&
                    it.usuarioId == usuarioId
        }

        holder.calificacion.onRatingBarChangeListener = null

        holder.calificacion.rating =
            calificacionUsuario?.calificacion ?: 0f

        holder.textoCalificacion.text =
            if (calificacionUsuario == null) {
                context.getString(R.string.rating_not_rated)
            } else {
                context.getString(
                    R.string.rating_value,
                    calificacionUsuario.calificacion.toInt()
                )
            }

        // Calcular el promedio de todos los usuarios.
        val calificacionesRecurso = calificaciones.filter {
            it.recursoId == recurso.id
        }

        if (calificacionesRecurso.isEmpty()) {
            holder.promedioCalificacion.text =
                context.getString(R.string.rating_no_votes)
        } else {
            val promedio = calificacionesRecurso
                .map { it.calificacion }
                .average()
                .toFloat()

            holder.promedioCalificacion.text = context.getString(
                R.string.rating_average,
                promedio,
                calificacionesRecurso.size
            )
        }

        holder.calificacion.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { _, rating, fromUser ->

                if (fromUser && rating > 0f) {
                    alCalificar(recurso, rating)
                }
            }

        holder.enlace.setOnClickListener {
            if (recurso.enlace.isBlank()) {
                Toast.makeText(
                    context,
                    R.string.resource_link_missing,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                try {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(recurso.enlace)
                    )

                    context.startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        R.string.resource_link_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun actualizarBotonFavorito(
        holder: RecursoViewHolder,
        recursoId: String
    ) {
        val esFavorito = preferencias.esFavorito(recursoId)

        if (esFavorito) {
            holder.favorito.setText(R.string.favorite_remove)

            holder.favorito.setIconResource(
                android.R.drawable.btn_star_big_on
            )
        } else {
            holder.favorito.setText(R.string.favorite_add)

            holder.favorito.setIconResource(
                android.R.drawable.btn_star_big_off
            )
        }
    }

    override fun getItemCount(): Int = recursos.size

    fun actualizarRecursos(nuevaLista: List<Recurso>) {
        recursos = nuevaLista
        notifyDataSetChanged()
    }

    fun actualizarCalificaciones(
        nuevasCalificaciones: List<Calificacion>
    ) {
        calificaciones = nuevasCalificaciones
        notifyDataSetChanged()
    }
}