package com.example.myheliports

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MapsFragment : Fragment() {
    companion object {
        private const val REQUEST_ALL_PERMISSIONS = 101
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var addItemButton: FloatingActionButton
    private var defaultLatLng = LatLng(57.0, 11.0)

    private lateinit var googleMap: GoogleMap


    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap

        getLocationsToMap { allLocations ->
            for (location in allLocations) {
                val lat = location.lat
                val long = location.long
                val documentIdOfLocation = location.documentId
                val locationName = location.name

                if (lat != null && long != null) {
                    val markerLatLng = LatLng(lat, long)
                    val marker = googleMap.addMarker(MarkerOptions().position(markerLatLng).title(locationName))
                    marker?.tag = location
                }
            }

            val locationsAdapter = MarkerInfoWindowAdapter(requireContext())
            googleMap.setInfoWindowAdapter(locationsAdapter)
        }
        moveCameraToCurrentLocation()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        db = Firebase.firestore
        storage = Firebase.storage

        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        //Setup menu
        topBarAndMenuSetup()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun moveCameraToCurrentLocation() {
        if (checkPermissions()) {

            try {
                var currentPositionUser =
                    LocationServices.getFusedLocationProviderClient(requireContext())
                currentPositionUser.lastLocation.addOnSuccessListener { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f)
                    googleMap.moveCamera(cameraUpdate)
                }
            } catch (e: SecurityException) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLatLng, 10f)
                googleMap.moveCamera(cameraUpdate)
            }
        }
        else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        // Check for camera and storage permissions
        if (!checkPermissions()) {

            // Request permissions
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                MapsFragment.REQUEST_ALL_PERMISSIONS
            )
        }
    }

     fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocationsToMap(callback: (List<Location>) -> Unit) {

        var listOfLocations = mutableListOf<Location>()

        val query = db.collection("locations")

        query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                handleError(error)
                return@addSnapshotListener
            }

            listOfLocations = updateLocations(snapshots?.documents, listOfLocations)
            callback(listOfLocations)
        }
    }

    private fun handleError(error: Exception?) {
        Log.w("!!!", "Error getting documents.", error)
        Toast.makeText(
            requireContext(),
            "Failed to load locations",
            Toast.LENGTH_SHORT
        ).show()

    }

    private fun updateLocations(
        snapshots: List<DocumentSnapshot>?,
        listOfLocations: MutableList<Location>
    ): MutableList<Location> {

        for (document in snapshots!!) {
            val location = document.toObject(Location::class.java)
            if (location != null) {
                listOfLocations.add(location)
            }
        }
        //Update with changes
        return listOfLocations
    }


    private fun setupThisFragment(fragmentact: FragmentActivity) {

        topAppBar = fragmentact.findViewById(R.id.topAppBar)
        topAppBar.menu.clear(); // Rensa den gamla menyn
        topAppBar.inflateMenu(R.menu.top_app_bar_map); // Lägg till den nya menyn
        addItemButton = fragmentact.findViewById(R.id.addItemButton)
        addItemButton.hide()
        topAppBar.navigationIcon = null
    }

    private fun topBarAndMenuSetup() {
        //Setup topbar etc
        activity?.let {
            setupThisFragment(it)

            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.user -> {
                        val intent = Intent(requireContext(), ProfileActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.current -> {
                        true
                    }

                    R.id.help -> {
                        true
                    }

                    else -> false
                }
            }
        }
    }
}