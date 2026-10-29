package sv.edu.aprendeapp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import sv.edu.aprendeapp.R
import sv.edu.aprendeapp.model.Recurso

class AdminRecursoAdapter(
    private var recursos: List<Recurso>,
    private val alEditar: (Recurso) -> Unit,
    private val alEliminar: (Recurso) -> Unit
) : RecyclerView.Adapter<AdminRecursoAdapter.RecursoViewHolder>() {

    class RecursoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val titulo: TextView =
            itemView.findViewById(R.id.tvAdminTitulo)

        val tipo: TextView =
            itemView.findViewById(R.id.tvAdminTipo)

        val descripcion: TextView =
            itemView.findViewById(R.id.tvAdminDescripcion)

        val editar: MaterialButton =
            itemView.findViewById(R.id.btnEditarRecurso)

        val eliminar: MaterialButton =
            itemView.findViewById(R.id.btnEliminarRecurso)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecursoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_recurso, parent, false)

        return RecursoViewHolder(vista)
    }

    override fun onBindViewHolder(
        holder: RecursoViewHolder,
        position: Int
    ) {
        val recurso = recursos[position]

        holder.titulo.text = recurso.titulo
        holder.tipo.text = recurso.tipo
        holder.descripcion.text = recurso.descripcion

        holder.editar.setOnClickListener {
            alEditar(recurso)
        }

        holder.eliminar.setOnClickListener {
            alEliminar(recurso)
        }
    }

    override fun getItemCount(): Int = recursos.size

    fun actualizarLista(nuevaLista: List<Recurso>) {
        recursos = nuevaLista
        notifyDataSetChanged()
    }
}
