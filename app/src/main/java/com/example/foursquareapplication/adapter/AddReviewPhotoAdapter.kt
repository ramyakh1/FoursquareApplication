package com.example.foursquareapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.foursquareapplication.R
import com.example.foursquareapplication.model.ReviewPhotos

class AddReviewPhotoAdapter(val capturedPhotos: ArrayList<ReviewPhotos>): RecyclerView.Adapter<AddReviewPhotoAdapter.AddReviewPhotosViewHolder>() {


    class AddReviewPhotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val addReviewPhoto=itemView.findViewById<ImageView>(R.id.add_review_photo)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddReviewPhotosViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AddReviewPhotosViewHolder(inflater.inflate(R.layout.item_add_review_photos, parent, false))

    }

    override fun getItemCount(): Int {

        return capturedPhotos.size
    }

    override fun onBindViewHolder(holder: AddReviewPhotosViewHolder, position: Int) {
        val reviewPhoto=capturedPhotos[position]
        holder.addReviewPhoto.setImageURI(reviewPhoto.image)
       // holder.addReviewPhoto.setImageBitmap(reviewPhoto.image)


    }
}