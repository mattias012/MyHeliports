package com.example.myheliports

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class LocationRecyclerAdapter(private val context: Context, private val locationList: List<Location>, private val onMapClickListener: OnMapClickListener) :
    RecyclerView.Adapter<LocationRecyclerAdapter.ViewHolder>() {

    private var layoutInflater = LayoutInflater.from(context)

    interface OnMapClickListener {
        fun onMapClick(documentId: String)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationRecyclerAdapter.ViewHolder {

        val itemView = layoutInflater.inflate(R.layout.item_listlocation, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locationList[position]

        holder.titleView?.text = location.name
        holder.itemView.tag = location.documentId
        holder.itemView.setOnClickListener {
            val documentId = it.tag as String
            //Remember last position in list
            Log.d("!!!", "onBindViewHolder position: $position")
            SharedData.position = position
            (it.context as StartActivity).showLocationFragment(documentId)
        }

        holder.viewOnMap.text = "Map"
        holder.viewOnMap.tag = location.documentId
        holder.viewOnMap.setOnClickListener {
            val documentId = it.tag as String
            onMapClickListener.onMapClick(documentId)
        }

        if(location.imageLink != null) {
            Glide.with(holder.itemView.context).load(location.imageLink).into(holder.imageView)
        } else {
            holder.imageView?.setImageResource(R.drawable.default1)
        }
    }

    override fun getItemCount(): Int {
        return locationList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleView: TextView? = itemView.findViewById<TextView>(R.id.titleViewUser)
        var imageView: ImageView = itemView.findViewById<ImageView>(R.id.imageViewUser)
        var viewOnMap: MaterialButton = itemView.findViewById<MaterialButton>(R.id.viewLocationOnMap)
    }
}