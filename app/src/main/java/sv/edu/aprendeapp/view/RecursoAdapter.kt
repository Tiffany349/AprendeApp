
package sv.edu.aprendeapp.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import sv.edu.aprendeapp.R
import sv.edu.aprendeapp.model.Recurso

class RecursoAdapter(
    private var recursos: List<Recurso>
) : RecyclerView.Adapter<RecursoAdapter.RecursoViewHolder>() {

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
            holder.imagen.setImageResource(
                android.R.drawable.ic_menu_gallery
            )
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

    override fun getItemCount(): Int = recursos.size

    fun actualizarRecursos(nuevaLista: List<Recurso>) {
        recursos = nuevaLista
        notifyDataSetChanged()
    }
}
