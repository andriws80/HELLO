package com.andriws.hello

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andriws.hello.databinding.ItemMatchBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class MatchAdapter(private var matchList: List<Match>) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = matchList[position]
        holder.bind(match)
    }

    override fun getItemCount(): Int = matchList.size

    fun updateData(newList: List<Match>) {
        matchList = newList
        notifyDataSetChanged()
    }

    inner class MatchViewHolder(private val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root) {
        private val firestore = FirebaseFirestore.getInstance()

        fun bind(match: Match) {
            //  Asumiendo que quieres mostrar el nombre y la foto del "otro" usuario (user2)
            val otherUserId = match.user2Id  // o user1Id si quieres mostrar el del primer usuario.
            firestore.collection("users").document(otherUserId).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: "Nombre no disponible"
                    val age = document.getLong("age")?.toInt() ?: 0
                    val city = document.getString("city") ?: "Ciudad no disponible"
                    val profileImageUrl = document.getString("profileImageUrl")

                    binding.textViewName.text = name
                    binding.textViewInfo.text = "$age, $city - Compatibilidad: ${String.format("%.2f", match.score * 100)}%"  // Incluye el score

                    // Actualizar contentDescription dinámicamente
                    binding.textViewName.contentDescription = itemView.context.getString(R.string.match_name_description) + " " + name
                    binding.textViewInfo.contentDescription = itemView.context.getString(R.string.match_info_description) + " " + "$age, $city"

                    if (profileImageUrl != null) {
                        Glide.with(binding.imageViewProfile.context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(binding.imageViewProfile)
                    } else {
                        binding.imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder)
                    }

                    itemView.setOnClickListener {
                        //  Pasar el ID del otro usuario y/o la información necesaria a la actividad de detalles.
                        val intent = Intent(itemView.context, MatchDetailActivity::class.java)
                        intent.putExtra("otherUserId", otherUserId) // Opcional: si necesitas más datos, pasa un Bundle o un objeto Parcelable.
                        itemView.context.startActivity(intent)
                    }
                }
        }
    }
}