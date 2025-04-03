package com.andriws.hello

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andriws.hello.databinding.ItemMatchBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
//import java.util.Locale

class MatchAdapter(private var matchList: MutableList<Match>) : // Usar MutableList
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
        // Calcular las diferencias y actualizar de forma eficiente
        val diffResult = androidx.recyclerview.widget.DiffUtil.calculateDiff(MatchDiffCallback(matchList, newList))
        matchList.clear()
        matchList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class MatchViewHolder(private val binding: ItemMatchBinding) : RecyclerView.ViewHolder(binding.root) {
        private val firestore = FirebaseFirestore.getInstance()

        fun bind(match: Match) {
            val context = itemView.context // Obtener el contexto aquí
            val otherUserId = match.user2Id
            firestore.collection("users").document(otherUserId).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name") ?: context.getString(R.string.name_not_available)
                    val age = document.getLong("age")?.toInt() ?: 0
                    val city = document.getString("city") ?: context.getString(R.string.city_not_available)
                    val profileImageUrl = document.getString("profileImageUrl")

                    binding.textViewName.text = name

                    // Usar string resource con placeholders y String.format(Locale.getDefault(), ...)
                    val infoText = context.getString(R.string.match_info_format, age, city, match.score * 100)
                    binding.textViewInfo.text = infoText

                    // Actualizar contentDescription dinámicamente (igual que antes, pero usando infoText)
                    binding.textViewName.contentDescription =
                        context.getString(R.string.match_name_description) + " " + name
                    binding.textViewInfo.contentDescription =
                        context.getString(R.string.match_info_description) + " " + infoText  // Usar infoText aquí


                    if (profileImageUrl != null) {
                        Glide.with(binding.imageViewProfile.context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(binding.imageViewProfile)
                    } else {
                        binding.imageViewProfile.setImageResource(R.drawable.ic_profile_placeholder)
                    }

                    itemView.setOnClickListener {
                        val intent = Intent(context, MatchDetailActivity::class.java)  // Usar context aquí
                        intent.putExtra("otherUserId", otherUserId)
                        context.startActivity(intent) // Usar context aquí
                    }
                }
        }
    }
}

// 1. DiffUtil Callback
class MatchDiffCallback(
    private val oldList: List<Match>,
    private val newList: List<Match>
) : androidx.recyclerview.widget.DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].user2Id == newList[newItemPosition].user2Id // O el ID que uses
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}