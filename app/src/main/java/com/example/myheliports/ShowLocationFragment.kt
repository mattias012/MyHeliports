package com.example.myheliports

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShowLocationFragment : Fragment() {

    lateinit var imageViewLocation: ImageView
    lateinit var ratingBarLocation: RatingBar
    lateinit var locationTitle: TextView
    lateinit var descriptionLocation: TextView
    lateinit var dateOfPhotoLocation: TextView
    lateinit var latlongTextView: TextView
    lateinit var topAppBar: MaterialToolbar
    lateinit var addItemButton: FloatingActionButton
    lateinit var addedByUser: TextView

    var lat: Double? = null
    var long: Double? = null
    var imageLink: String? = null
    var titleLocationToShare: String? = null

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_showlocation, container, false)

        //Initialize all the views
        initializeViews(view)

        //Setup or menu
        topBarAndMenuSetup()

        //Get data from the store
        val documentId = arguments?.getString("documentId")
        db = Firebase.firestore

        if (documentId != null) {
            getLocationData(db, documentId)
        }

        return view
    }

    private fun topBarAndMenuSetup() {
        //Setup topbar etc
        activity?.let {
            setupThisFragment(it)

            topAppBar.setNavigationOnClickListener {
                goBackStuff()
            }

            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.user -> {
                        true
                    }

                    R.id.edit -> {
                        true
                    }

                    R.id.share -> {
                        shareLocation()
                        true
                    }

                    else -> false
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
    }

    private fun fixPhotoDate(location: Location) {
        val date = location.dateOfPhoto?.toDate()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = format.format(date)

        dateOfPhotoLocation.text = dateString
    }

    private fun fixTitleOfLocation(checkTitle: String, location: Location): String {

        var titleOfLocationIs = checkTitle

        if (titleOfLocationIs.length > 13) {
            titleOfLocationIs = titleOfLocationIs.take(12) + "..."
        }
        if (titleOfLocationIs == null) {
            titleOfLocationIs = "Unknown Location"
        }

        return titleOfLocationIs
    }

    private fun getLocationData(db: FirebaseFirestore, documentId: String) {
        db.collection("locations").document(documentId).get()
            .addOnSuccessListener { document ->
                val location = document.toObject(Location::class.java)
                location?.let {

                    //Get user info userId
                    val userId = location.userId
                    if (userId != null) {
                        getUserName(userId, it)
                    }

                    //Get and set ratingBar
                    if (location.rating != null) {

                        ratingBarLocation.rating = location.rating!!.toFloat()
                        ratingBarLocation.isEnabled = false
                    }

                    //Fix Title
                    val checkTitle = location.name ?: "Location"
                    val titleOfLocationIs = fixTitleOfLocation(checkTitle, it)
                    topAppBar.title = titleOfLocationIs

                    //For full view, full name of lcation should be printed
                    locationTitle.text = location.name
                    //Full description
                    descriptionLocation.text = location.description

                    //Fix date of photo
                    fixPhotoDate(it)

                    //Print Lat/Long
                    latlongTextView.text = "@ ${location.lat} / ${location.long}"

                    //Get and set Image
                    if (location.imageLink != null) {
                        Glide.with(requireContext()).load(location.imageLink).centerCrop()
                            .into(imageViewLocation)
                    } else {
                        imageViewLocation.setImageResource(R.drawable.default1)
                    }

                    setForShare(it)


                } ?: run {
                    Log.d("!!!", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("!!!", "GET failed with ", exception)
            }
    }

    private fun getUserName(userId: String, location: Location) {
        db.collection("users").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { userDocument ->
                // Kontrollera om frågan returnerade något dokument
                if (!userDocument.isEmpty) {

                    val document = userDocument.documents[0]
                    // Convert to a User object
                    val user = document.toObject(User::class.java)
                    user?.let {

                        // Set date added of location to the by line
                        val date = location.timestamp?.toDate()
                        val format = SimpleDateFormat(
                            "yyyy-MM-dd HH:mm",
                            Locale.getDefault()
                        )
                        val dateString = format.format(date)

                        addedByUser.text = "by ${user.userName} on $dateString"
                    }
                }
            }
    }

    private fun setForShare(location: Location) {
        lat = location.lat
        long = location.long
        imageLink = location.imageLink
        titleLocationToShare = location.name
    }

    private fun shareLocation() {
        // Skapa en Google Maps länk
        val googleMapsLink = "http://maps.google.com/maps?q=$lat,$long"

        val imageUri = imageLink

        val shareString = "$titleLocationToShare - View it on Google Maps: \n$googleMapsLink"

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareString)
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun goBackStuff() {
        (activity as StartActivity).goBack()
    }

    private fun setupThisFragment(fragmentact: FragmentActivity) {

        topAppBar = fragmentact.findViewById(R.id.topAppBar)
        topAppBar.menu.clear(); // Rensa den gamla menyn
        topAppBar.inflateMenu(R.menu.top_app_bar_showlocation); // Lägg till den nya menyn
        addItemButton = fragmentact.findViewById(R.id.addItemButton)
        addItemButton.hide()
        topAppBar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.baseline_arrow_back_ios_24)
    }

    private fun initializeViews(view: View) {
        imageViewLocation = view.findViewById(R.id.imageViewLocation)
        ratingBarLocation = view.findViewById(R.id.ratingBarLocation)
        locationTitle = view.findViewById(R.id.titleLocation)
        descriptionLocation = view.findViewById(R.id.descriptionLocationTextView)
        dateOfPhotoLocation = view.findViewById(R.id.dateOfPhotoTextView)
        latlongTextView = view.findViewById(R.id.latlongTextView)
        addedByUser = view.findViewById(R.id.addedByUserTextView)
    }
}
