package com.example.myheliports

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ListLocationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var locationList: MutableList<Location> = mutableListOf()
    private lateinit var progressBar: ProgressBar

    var columnsInGrid = 2

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_listlocation, container, false)
        recyclerView = view.findViewById(R.id.listOfLocationsView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(),columnsInGrid)
        progressBar = view.findViewById(R.id.progressBar)

    activity?.let {
        val topAppBar = it.findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.user -> {
                    val intent = Intent(requireContext(), ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.grid -> {
                    toggleViewMode(2)
                    true
                }
                R.id.list -> {
                    toggleViewMode(1)
                    true
                }
                else -> false
            }
        }
    }

    Log.d("!!!", "inflate")
        val db = Firebase.firestore
        progressBar.visibility = View.VISIBLE // Starta ProgressBar
        Handler(Looper.getMainLooper()).postDelayed({
            db.collection("locations")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w("!!!", "Error getting documents.", error)
                        Toast.makeText(
                            requireContext(),
                            "Failed to load locations",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressBar.visibility =
                            View.GONE  // Stoppa ProgressBar om det finns ett fel
                        return@addSnapshotListener
                    }

                    locationList.clear()
                    for (document in snapshots!!) {
                        val location = document.toObject(Location::class.java)
                        locationList.add(location)
                    }
                    //Set to adapter
                    val adapter = LocationRecyclerAdapter(requireContext(), locationList)
                    recyclerView.adapter = adapter
                    //Stop progressBar
                    progressBar.visibility = View.GONE
                }
        },500)

        return view
    }

    fun toggleViewMode(columns: Int) {
        //New layoutmanager with number of columns
        recyclerView.layoutManager = GridLayoutManager(requireContext(), columns)

        //Update adapter
        recyclerView.adapter?.notifyDataSetChanged()

        columnsInGrid = columns
    }

}