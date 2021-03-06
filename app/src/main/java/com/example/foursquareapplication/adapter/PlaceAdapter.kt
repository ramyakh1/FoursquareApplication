package com.example.foursquareapplication.adapter

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foursquareapplication.OnFavouriteCLickListener
import com.example.foursquareapplication.R
import com.example.foursquareapplication.databinding.ItemPlaceBinding
import com.example.foursquareapplication.helper.ChangeRatingColor
import com.example.foursquareapplication.helper.Constants
import com.example.foursquareapplication.helper.PlaceUtils
import com.example.foursquareapplication.model.DataPlace
import com.example.foursquareapplication.model.FavouriteResponse
import com.example.foursquareapplication.model.Place
import com.example.foursquareapplication.ui.DetailsActivity
import com.example.foursquareapplication.viewmodel.FavouriteViewModel


class PlaceAdapter (private val mCtx: Context) :
    PagedListAdapter<DataPlace, PlaceAdapter.ItemViewHolder>(DIFF_CALLBACK) {

    private val favouriteViewModel = ViewModelProvider.AndroidViewModelFactory(mCtx.applicationContext as Application).create(FavouriteViewModel::class.java)
    private val sharedPreferences: SharedPreferences = mCtx.getSharedPreferences(Constants.USER_PREFERENCE,MODE_PRIVATE)
    val userId = sharedPreferences.getString(Constants.USER_ID,"")
    var token = sharedPreferences.getString(Constants.USER_TOKEN,"")
    var favouriteData : List<Place>?=null
    private  var listeenr : OnFavouriteCLickListener? =null
    init {
        if(sharedPreferences.contains(Constants.USER_ID)) {
            getFavourite()
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val inflater =
            LayoutInflater.from(parent.context)
        val placeBinding = ItemPlaceBinding.inflate(inflater,parent,false)
        return ItemViewHolder(placeBinding)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {

            item.getPlace()?.let {
                val isFavourite = checkIsFavourite(it.getPlaceId())
                if(isFavourite)
                    holder.placeBinding.favourite.isChecked = true
                holder.placeBinding.name.text = it.getName()
                Glide.with(mCtx).load(it.getImage()).placeholder(R.drawable.loading).into(holder.placeBinding.placeImage)
                val rating = it.getOverallRating()
                if(rating!=0.0f) {
                    holder.placeBinding.ratingBackground.visibility = View.VISIBLE
                    holder.placeBinding.rating.text = String.format("%.1f",item.getPlace()?.getOverallRating())
                    val ratingBackground = ChangeRatingColor().getRatingColor(
                        rating)
                    holder.placeBinding.ratingBackground.setBackgroundColor(ratingBackground)

                }
                val cost = PlaceUtils().getCost(it.getCost(),holder.placeBinding.priceRange.context)
                if(cost == null){
                    holder.placeBinding.dot.visibility = View.GONE
                }else{
                    holder.placeBinding.priceRange.text = cost
                }
                if(it.getPlaceType().size>0){
                    val type = PlaceUtils().getShortPlaceType(it.getPlaceType()[0].getCategoryName())
                    holder.placeBinding.type.text = type
                }else{
                    holder.placeBinding.type.visibility = View.GONE
                }
                holder.placeBinding.address.text = it.getLandmark()
            }
            holder.placeBinding.distance.text = String.format("%.1f km", item.getDistance())



        } else {
            Toast.makeText(mCtx, "Item is null", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkIsFavourite(placeId: Int) : Boolean {
        var isFavourite = false
        if(favouriteData!=null){
            for( data in favouriteData!!){
                if(data.getPlaceId() == placeId) {
                    isFavourite = true
                    break
                }
            }
        }
        return isFavourite
    }


    inner class ItemViewHolder(val placeBinding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(placeBinding.root) {
            init {
                placeBinding.root.setOnClickListener {
                    val intent = Intent(mCtx,DetailsActivity::class.java)
                    val bundle = Bundle()
                    val item = getItem(bindingAdapterPosition)?.getPlace()
                    bundle.putParcelable(Constants.PLACE_RESPOSNE,item)
                    bundle.putBoolean(Constants.IS_FAVOURITE,placeBinding.favourite.isChecked)
                    intent.putExtras(bundle)
                    mCtx.startActivity(intent)
                }
                if(sharedPreferences.contains(Constants.USER_ID)) {
                    placeBinding.favourite.setOnClickListener {

                        val item = getItem(bindingAdapterPosition)?.getPlace()
                        listeenr?.onFavouriteClick(
                            placeBinding.favourite.isChecked,
                            item?.getPlaceId()
                        )

                    }
                }else{
                    placeBinding.favourite.visibility = View.GONE
                }

            }



    }

    private fun getFavourite() {
        if(userId!=null && token!=null) {
            val token = "Bearer $token"
            val pageNumber = 0
            val pageSize = 200
            val owner = (mCtx as AppCompatActivity)
            favouriteViewModel.getFavouriteData(userId.toInt(),pageNumber,pageSize,token).observe(owner,{
                if(it!=null) {
                    if (it.getStatus() == Constants.STATUS_OK) {
                        favouriteData = it.getData()
                        notifyDataSetChanged()
                    }
                }
            })
        }
    }

    fun setClickListener(listener: OnFavouriteCLickListener) {
        this.listeenr = listener
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<DataPlace> =
            object : DiffUtil.ItemCallback<DataPlace>() {
                override fun areItemsTheSame(
                    oldItem:DataPlace,
                    newItem: DataPlace
                ): Boolean {
                    return oldItem.getPlace()?.getPlaceId() == newItem.getPlace()?.getPlaceId()
                }

                override fun areContentsTheSame(
                    oldItem: DataPlace,
                    newItem: DataPlace
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

}