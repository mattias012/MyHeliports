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
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationRecyclerAdapter(private val context: Context, private val locationList: List<Location>) :
    RecyclerView.Adapter<LocationRecyclerAdapter.ViewHolder>() {

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
        holder.itemView.tag = location.documentId
        holder.itemView.setOnClickListener {
            val documentId = it.tag as String
            (it.context as StartActivity).showLocationFragment(documentId)
        }
//        holder.descriptionView?.text = location.description
//
//        val date = location.dateOfPhoto?.toDate()
//        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val dateString = format.format(date)
//
//        holder.dateView?.text = dateString


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
        var titleView: TextView? = itemView.findViewById<TextView>(R.id.titleView)
//        var descriptionView: TextView? = itemView.findViewById<TextView>(R.id.descriptionView)
//        var dateView: TextView? = itemView.findViewById<TextView>(R.id.dateView)
        var imageView: ImageView = itemView.findViewById<ImageView>(R.id.imageView)
    }
}