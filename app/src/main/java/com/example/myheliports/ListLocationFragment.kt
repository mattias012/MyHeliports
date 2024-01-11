package com.example.myheliports

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
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
    var getAll = false

    private var safeContext: Context? = null
    private var badgeUpdater: BadgeUpdater? = null
    private lateinit var viewModel: MyViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        badgeUpdater = context as? BadgeUpdater
    }


    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        safeContext = context

        db = Firebase.firestore
        auth = Firebase.auth

        //Remember where user comes from
        SharedData.fragment = this
        SharedData.prevFragment = this

        viewModel = ViewModelProvider(this)[MyViewModel::class.java]

        val view = inflater.inflate(R.layout.fragment_listlocation, container, false)
        recyclerView = view.findViewById(R.id.listOfLocationView)

        val context = context
        if (context != null) {
            recyclerView.layoutManager = GridLayoutManager(context, columnsInGrid)
        }

        if (!getAll){
            observeList()
        }

        progressBar = view.findViewById(R.id.progressBar)
        searchView = view.findViewById(R.id.search)
        searchText = view.findViewById(R.id.searchText)

        activity?.let {
            val topAppBar = it.findViewById<MaterialToolbar>(R.id.topAppBar)
            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.user -> {
                        if (safeContext != null) {
                            val intent = Intent(requireContext(), ProfileActivity::class.java)
                            startActivity(intent)
                        }
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

                    R.id.person -> {

                        getAllData(columnsInGrid, false)
                        true
                    }

                    R.id.group -> {

                        getAllData(columnsInGrid, true)
                        true
                    }

                    else -> false
                }
            }
        }

        setupTextWatcher(searchText)

        //First search, show only your own places
        searchDataBase("", false)

        //Set to adapter
        if (safeContext != null) {
            val adapter = LocationRecyclerAdapter(requireContext(), locationList, object :
                LocationRecyclerAdapter.OnMapClickListener {
                override fun onMapClick(documentId: String) {
                    (activity as? StartActivity)?.showMapsFragment(documentId)
                }
            })

            recyclerView.adapter = adapter
            recyclerView.smoothScrollToPosition(SharedData.position)
        }

        return view
    }

    private fun observeList(){
        viewModel.locationListLiveData.observe(viewLifecycleOwner, Observer { locations ->
            val newSize = locations.size
            val difference = newSize - viewModel.oldSize

            if (difference > 0) {

                Handler(Looper.getMainLooper()).postDelayed({
                    badgeUpdater?.updateBadge(difference)
                }, 1000)

            } else {
                badgeUpdater?.removeBadge()
            }
            viewModel.oldSize = newSize
        })

    }
    private fun updateBadgeIfThisFragmentIsShown() {
//        viewModel.oldSize = 0
//        viewModel.locationListLiveData.value = locationList

        Log.d("!!!", "locationListSize: ${locationList.size} och old:")
        viewModel.locationListLiveData.value = locationList
        viewModel.oldSize = locationList.size
        badgeUpdater?.removeBadge()

    }

    private fun getAllData(numberOfColumns: Int, allData: Boolean) {
        if (safeContext != null) {
            getAll = allData
            searchDataBase("", allData)
            recyclerView.layoutManager = GridLayoutManager(requireContext(), numberOfColumns)

            updateBadgeIfThisFragmentIsShown()

            //Update adapter
            recyclerView.adapter?.notifyDataSetChanged()

            columnsInGrid = numberOfColumns
        }
    }

    private fun toggleViewMode(columns: Int) {

        //New layoutmanager with number of columns
        if (safeContext != null) {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), columns)

            //Update adapter
            recyclerView.adapter?.notifyDataSetChanged()

            columnsInGrid = columns
        }
    }

    override fun onPause() {
        super.onPause()

        val gridLayoutManager = (recyclerView.layoutManager as GridLayoutManager)
        gridLayoutManager.scrollToPositionWithOffset(SharedData.position, 0)
    }

    override fun onStart() {
        super.onStart()
//        recyclerView.smoothScrollToPosition(SharedData.position)
        val gridLayoutManager = (recyclerView.layoutManager as GridLayoutManager)
        gridLayoutManager.scrollToPositionWithOffset(SharedData.position, 0)

    }

    override fun onResume() {
        super.onResume()
        //Start progressBar
//        progressBar.visibility = View.VISIBLE

        searchText.setText("")

        Log.d("!!!", "onResume position: ${SharedData.position}")
        //Scroll to SharedData.position
//        recyclerView.smoothScrollToPosition(SharedData.position)
        val gridLayoutManager = (recyclerView.layoutManager as GridLayoutManager)
        gridLayoutManager.scrollToPositionWithOffset(SharedData.position, 0)
    }

    private fun setupTextWatcher(editText: TextInputEditText) {

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchDataBase(searchText.text.toString(), getAll)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchDataBase(searchText.text.toString(), getAll)
            }
        }
        editText.addTextChangedListener(textWatcher)
    }

    private fun handleError(error: Exception?) {
        Log.w("!!!", "Error getting documents.", error)

        if (safeContext != null) {
            Toast.makeText(
                requireContext(),
                "Failed to load locations",
                Toast.LENGTH_SHORT
            ).show()
        }
        progressBar.visibility = View.GONE  // Stoppa ProgressBar om det finns ett fel
    }

    private fun updateLocations(snapshots: List<DocumentSnapshot>?) {
        locationList.clear()
//        viewModel.oldSize = viewModel.locationListLiveData.value?.size ?: 0
        if (snapshots != null) {
            for (document in snapshots) {
                val location = document.toObject(Location::class.java)
                if (location != null) {
                    locationList.add(location)
                }
            }
            SharedData.previousSnapshotCount = locationList.size
        }

//        viewModel.getAllPlacesAtLogin()

        //Update adapter with changes
        recyclerView.adapter?.notifyDataSetChanged()

        //Stop progressBar
        progressBar.visibility = View.GONE
    }

    private fun searchDataBase(searchThisString: String?, getAll: Boolean) {
        progressBar.visibility = View.VISIBLE

        Log.d("!!!", "searching for this name")
        val query = if (searchThisString.isNullOrEmpty()) {

            if (getAll) {
                db.collection("locations").orderBy("timestamp", Query.Direction.DESCENDING)
            } else {
                val user = auth.currentUser
                if (user != null) {
                    db.collection("locations").whereEqualTo("userId", user.uid)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                } else {
                    db.collection("locations").orderBy("timestamp", Query.Direction.DESCENDING)
                }
            }
        } else {
            //Query firestore after ish-wildcard
            if (getAll) {
                db.collection("locations").orderBy("name").startAt(searchThisString)
                    .endAt(searchThisString + "\uf8ff")
            } else {
                val user = auth.currentUser
                if (user != null) {
                    db.collection("locations").whereEqualTo("userId", user.uid).orderBy("name")
                        .startAt(searchThisString)
                        .endAt(searchThisString + "\uf8ff")
                } else {
                    db.collection("locations").orderBy("name").startAt(searchThisString)
                        .endAt(searchThisString + "\uf8ff")
                }
            }

        }

        query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                handleError(error)
                return@addSnapshotListener
            }

            updateLocations(snapshots?.documents)
        }
    }
}