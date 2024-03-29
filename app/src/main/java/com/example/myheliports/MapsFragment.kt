package com.example.myheliports

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class MapsFragment : Fragment(), MarkerInfoWindowAdapter.OnInfoWindowElemTouchListener {
    companion object {
        private const val REQUEST_ALL_PERMISSIONS = 101
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var addItemButton: FloatingActionButton
    private var defaultLatLng = LatLng(57.0, 11.0)

    private lateinit var googleMap: GoogleMap

    private var badgeUpdater: BadgeUpdater? = null
    private lateinit var viewModel: MyViewModel

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
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(markerLatLng).title(locationName)
                    )
                    marker?.tag = location
                }
            }

            val safeContext = context
            if (safeContext != null) {
                val locationsAdapter = MarkerInfoWindowAdapter(requireContext(), this)
                googleMap.setInfoWindowAdapter(locationsAdapter)

                googleMap.setOnInfoWindowClickListener { marker ->
                    val location = marker.tag as? Location
                    val documentId = location?.documentId
                    if (documentId != null) {
                        onLinkClicked(documentId)
                    } else {
                        Toast.makeText(
                            context,
                            "Something went wrong when loading location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        val documentId = arguments?.getString("documentId")

        if (documentId != null) {

            moveCameraToLocation(documentId)
        } else {
            moveCameraToCurrentLocation()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        badgeUpdater = context as? BadgeUpdater
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        db = Firebase.firestore
        storage = Firebase.storage

        //Remember where user comes from
        SharedData.fragment = this
        SharedData.prevFragment = this

        viewModel = ViewModelProvider(this)[MyViewModel::class.java]
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        //Setup menu
        topBarAndMenuSetup()



        viewModel.locationListLiveData.observe(viewLifecycleOwner, Observer { locations ->
            val newSize = locations.size
            val difference = newSize - viewModel.oldSize

            if (difference > 0) {
                badgeUpdater?.updateBadge(difference)
            } else {
                badgeUpdater?.removeBadge()
            }
            viewModel.oldSize = newSize
        })


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun moveCameraToLocation(documentId: String) {
        if (documentId != null) {
            db.collection("locations").document(documentId).get()
                .addOnSuccessListener { document ->

                    val location = document.toObject(Location::class.java)
                    location?.let {

                        val currentLatLng = location.lat?.let { it1 ->
                            location.long?.let { it2 ->
                                LatLng(
                                    it1,
                                    it2
                                )
                            }
                        }
                        val cameraUpdate =
                            currentLatLng?.let { it1 ->
                                CameraUpdateFactory.newLatLngZoom(
                                    it1,
                                    12f
                                )
                            }
                        if (cameraUpdate != null) {
                            googleMap.moveCamera(cameraUpdate)
                        }

                    } ?: run {
                        Log.d("!!!", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("!!!", "GET failed with ", exception)
                }

            SharedData.prevFragment = this
        }
    }

    private fun moveCameraToCurrentLocation() {
        if (checkPermissions()) {

            try {
                var currentPositionUser =
                    LocationServices.getFusedLocationProviderClient(requireContext())
                currentPositionUser.lastLocation.addOnSuccessListener { location ->
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 7f)
                    googleMap.moveCamera(cameraUpdate)
                }
            } catch (e: SecurityException) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLatLng, 7f)
                googleMap.moveCamera(cameraUpdate)
            }
        } else {
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

    private fun checkPermissions(): Boolean {
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
        topAppBar.menu.clear(); // Clear old menu
        topAppBar.inflateMenu(R.menu.top_app_bar_map); // add new menu
        topAppBar.title = "Map"
        addItemButton = fragmentact.findViewById(R.id.addItemButton)
        // addItemButton.hide()
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
                        moveCameraToCurrentLocation()
                        true
                    }

                    R.id.help -> {

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Map View")
                            .setMessage(
                                "Browse the map, click on a marker to get more information of the location. At this time all users places are showed as a default setting.\n \n" +
                                        "If you get lost, hit the GPS icon to center map on your current location."
                            )
                            .setNeutralButton("OK") { dialog, which ->

                            }
                            .show()
                        true
                    }

                    else -> false
                }
            }
        }
        //Override androids backbutton
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goBackStuff()
                }
            })
    }

    private fun goBackStuff() {

        (activity as StartActivity).goBack(null, null)
        addItemButton.show()

    }
    override fun onLinkClicked(documentId: String) {
        //Navigate to ShowLocation with documentId
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, ShowLocationFragment.newInstance(documentId))
            .commit()
    }
}