package com.andriws.hello

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotoAdapter(
    private val imageUris: MutableList<Uri>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.photoImageView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        init {
            deleteButton.setOnClickListener {
                onDeleteClick(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val imageUri = imageUris[position]
        Glide.with(holder.itemView.context)
            .load(imageUri)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUris.size
}