package com.example.myheliports

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(private val context: Context, private val listener: OnInfoWindowElemTouchListener) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker): View? {
        // 1. Get tag
        val location = p0?.tag as? Location ?: return null

        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(context).inflate(R.layout.marker_info_contents, null)

        view.findViewById<TextView>(R.id.text_view_title).text = location.name
        view.findViewById<TextView>(R.id.text_view_address).text = location.description
        val link = view.findViewById<TextView>(R.id.text_view_link)

        link.text = "Click to view this spot"

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
    interface OnInfoWindowElemTouchListener {
        fun onLinkClicked(documentId: String)
    }
}