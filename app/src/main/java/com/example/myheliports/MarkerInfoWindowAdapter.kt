package com.example.myheliports

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker): View? {
        // 1. Get tag
        val location = p0?.tag as? Location ?: return null

        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(context).inflate(R.layout.marker_info_contents, null)

        Glide.with(context)
            .asBitmap()
            .load(location.imageLink)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // 1. Sätt bilden som en drawable i din vy
                    // 2. Uppdatera markören för att tvinga InfoWindow att rita om sig själv
                    p0.showInfoWindow()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // hantera här om något behövs när bilden rensas
                }
            })

        view.findViewById<TextView>(R.id.text_view_title).text = location.name
        view.findViewById<TextView>(R.id.text_view_address).text = location.description
        view.findViewById<TextView>(R.id.text_view_rating).text = "2"

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}