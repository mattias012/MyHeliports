package com.example.myheliports

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationRecyclerAdapter(val context: Context, val locationList : List<Location>) : RecyclerView.Adapter<LocationRecyclerAdapter.ViewHolder>() {

    private var layoutInflater = LayoutInflater.from(context)

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
        Log.d("!!!", "Binding location: $location")
        // resten av din kod här...

    }

    override fun getItemCount(): Int {
        return locationList.size
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleView: TextView? = itemView.findViewById<TextView>(R.id.titleView)
        var descriptionView: TextView? = itemView.findViewById<TextView>(R.id.descriptionView)
        var dateView: TextView? = itemView.findViewById<TextView>(R.id.dateView)
        var imageView: ImageView? = itemView.findViewById<ImageView>(R.id.imageView)
    }
}