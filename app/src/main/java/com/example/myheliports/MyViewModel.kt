package com.example.myheliports

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyViewModel : ViewModel() {
    var locationListLiveData: MutableLiveData<List<Location>> = MutableLiveData()
    var oldSize: Int = 0

    init {
        getAllPlacesAtLogin()
    }

    private fun updateLocations(snapshots: List<DocumentSnapshot>?) {
        val locationList: MutableList<Location> = mutableListOf()

        snapshots?.let {
            for (document in it) {
                val location = document.toObject(Location::class.java)
                location?.let { loc ->
                    locationList.add(loc)
                }
            }
        }

        oldSize = locationListLiveData.value?.size ?: 0
        locationListLiveData.value = locationList

    }

    private fun getSnapshot() {
        val db = Firebase.firestore
        db.collection("locations").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {

                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    updateLocations(snapshots.documents)
                }
            }
    }

    private fun getAllPlacesAtLogin() {
        val db = Firebase.firestore
        db.collection("locations").orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                val locationListFirst: MutableList<Location> = mutableListOf()
                for (document in documents) {
                    val location = document.toObject(Location::class.java)
                    locationListFirst.add(location)
                }
                // Set amount of locations on first login
                oldSize = locationListFirst.size
                locationListLiveData.value = locationListFirst

                // Call getSnapshot() after fetching initial data
                getSnapshot()
            }
            .addOnFailureListener {
//                oldSize = 0
            }
    }
    }