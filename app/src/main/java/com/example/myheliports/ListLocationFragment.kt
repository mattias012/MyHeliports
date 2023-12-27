package com.example.myheliports

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ListLocationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var locationList: MutableList<Location> = mutableListOf()
    private lateinit var progressBar: ProgressBar
    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_listlocation, container, false)
        recyclerView = view.findViewById(R.id.listOfLocationsView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        progressBar = view.findViewById(R.id.progressBar)

        Log.d("!!!", "inflate")
        val db = Firebase.firestore
        progressBar.visibility = View.VISIBLE // Starta ProgressBar

        db.collection("locations")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("!!!", "Error getting documents.", error)
                    Toast.makeText(requireContext(), "Failed to load locations", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE  // Stoppa ProgressBar om det finns ett fel
                    return@addSnapshotListener
                }

                locationList.clear()
                for (document in snapshots!!) {
                    val location = document.toObject(Location::class.java)
                    locationList.add(location)
                }
                val adapter = LocationRecyclerAdapter(requireContext(), locationList)
                recyclerView.adapter = adapter

                progressBar.visibility = View.GONE  // Stoppa ProgressBar när data har hämtats
            }

        return view
    }

}