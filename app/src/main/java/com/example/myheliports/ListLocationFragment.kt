package com.example.myheliports

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.search.SearchBar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ListLocationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var locationList: MutableList<Location> = mutableListOf()
    private lateinit var progressBar: ProgressBar
    private lateinit var searchView: TextInputLayout
    private lateinit var searchText: TextInputEditText

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    //standard setting is grid view
    var columnsInGrid = 2
    var position = 0
    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        db = Firebase.firestore
        auth = Firebase.auth

        val view = inflater.inflate(R.layout.fragment_listlocation, container, false)
        recyclerView = view.findViewById(R.id.listOfLocationsView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(),columnsInGrid)
        progressBar = view.findViewById(R.id.progressBar)
        searchView = view.findViewById(R.id.search)
        searchText = view.findViewById(R.id.searchText)



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
        setupTextWatcher(searchText)
        //Start progresBar
        progressBar.visibility = View.VISIBLE

        //Add a short delay, otherwise it goes too fast...
        Handler(Looper.getMainLooper()).postDelayed({
            db.collection("locations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
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
                    // Scroll to THIS result
                    position = SharedData.position

                    recyclerView.adapter = adapter
                    recyclerView.smoothScrollToPosition(position)
                    //Stop progressBar
                    progressBar.visibility = View.GONE
                }

        },500)

        return view
    }

    private fun toggleViewMode(columns: Int) {

        //New layoutmanager with number of columns
        recyclerView.layoutManager = GridLayoutManager(requireContext(), columns)

        //Update adapter
        recyclerView.adapter?.notifyDataSetChanged()

        columnsInGrid = columns
    }

    override fun onPause() {
        super.onPause()

        // Nollställ positionen när du lämnar ListLocationsFragment
//        SharedData.position = 0
    }
    override fun onStart() {
        super.onStart()

        // Scroll to SharedData.position
        recyclerView.smoothScrollToPosition(SharedData.position)
    }
    override fun onResume() {
        super.onResume()

        // Scroll to SharedData.position
        recyclerView.smoothScrollToPosition(SharedData.position)
    }
    private fun setupTextWatcher(editText: TextInputEditText) {

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                search(searchText.text.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //search


            }
        }
        editText.addTextChangedListener(textWatcher)
    }
    private fun search(searchThisString: String?){


        if (searchThisString.isNullOrEmpty()) {
            progressBar.visibility = View.VISIBLE
            db.collection("locations")

                .orderBy("timestamp", Query.Direction.DESCENDING)
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

                    recyclerView.adapter?.notifyDataSetChanged()

                    //Stop progressBar
                    progressBar.visibility = View.GONE
                }
        }
        else {
            //Start progresBar
            progressBar.visibility = View.VISIBLE
            db.collection("locations")
                .whereEqualTo("name", searchThisString)
                .orderBy("timestamp", Query.Direction.DESCENDING)
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

                    recyclerView.adapter?.notifyDataSetChanged()

                    //Stop progressBar
                    progressBar.visibility = View.GONE
                }
        }
    }
}