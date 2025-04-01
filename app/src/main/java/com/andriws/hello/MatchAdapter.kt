package com.andriws.hello

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andriws.hello.databinding.ItemMatchBinding
import com.bumptech.glide.Glide

class MatchAdapter(private var matchList: List<com.google.android.gms.appsearch.SearchResult>) :
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

    fun updateData(newList: List<com.google.android.gms.appsearch.SearchResult>) {
        matchList = newList
        notifyDataSetChanged()
    }

    inner class MatchViewHolder(private val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: com.google.android.gms.appsearch.SearchResult) {
            binding.textViewName.text = match.name
            binding.textViewInfo.text = "${match.age}, ${match.city}" // Ajusta según lo que quieras mostrar en la lista
            if (match.profileImageUrl != null) {
                Glide.with(binding.imageViewProfile.context)
                    .load(match.profileImageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder) // Reemplaza con tu placeholder
                    .into(binding.imageViewProfile)
            } else {
                binding.imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder) // Reemplaza con tu placeholder
            }

            // Añadir OnClickListener para la navegación a detalles
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, MatchDetailActivity::class.java)
                intent.putExtra("searchResult", match) // Pasar el objeto SearchResult
                itemView.context.startActivity(intent)
            }
        }
    }
}