package com.example.myheliports

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Locale

class ShowLocationFragment : Fragment() {

    private lateinit var imageViewLocation: ImageView
    private lateinit var ratingBarLocation: RatingBar
    private lateinit var locationTitle: TextView
    private lateinit var descriptionLocation: TextView
    private lateinit var dateOfPhotoLocation: TextView
    private lateinit var latlongTextView: TextView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var addItemButton: FloatingActionButton
    private lateinit var addedByUser: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var materialDivider: MaterialDivider


    private lateinit var materialDividerComments2: MaterialDivider
    private lateinit var commentTextView: TextView
    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentWrapText: TextInputLayout
    private lateinit var commentThis: TextInputEditText
    private lateinit var commentButton: Button

    private var commentList: MutableList<Comment> = mutableListOf()

    private var lat: Double? = null
    private var long: Double? = null
    private var imageLink: String? = null
    private var titleLocationToShare: String? = null

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var viewsToFade: List<View>

    private var enableEdit = false

    companion object {
        fun newInstance(documentId: String): ShowLocationFragment {
            val fragment = ShowLocationFragment()

            val args = Bundle()
            args.putString("documentId", documentId)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        SharedData.fragment = ShowLocationFragment()

        val view = inflater.inflate(R.layout.fragment_showlocation, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE // Starta ProgressBar

        //Get data from the store
        val documentId = arguments?.getString("documentId")
        db = Firebase.firestore
        auth = Firebase.auth

        //Initialize all the views
        initializeViews(view)

        if (documentId != null) {

            getLocationTitle(documentId)

            Handler(Looper.getMainLooper()).postDelayed({
                getLocationData(documentId)
                //Stop progressbar and show ratingbar and materialdivders
                showHiddenViews()
                showComments(documentId)

                progressBar.visibility = View.GONE
            }, 500)

            //Setup or menu
            topBarAndMenuSetup(documentId)
            //New layoutmanager with number of columns
            recyclerViewComments.layoutManager = LinearLayoutManager(requireContext())
            commentButton.setOnClickListener {
                addComment(documentId)
                fadeViews(viewsToFade, true)
                hideKeyboard()
            }
            commentThis.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard()
                    addComment(documentId)
                    fadeViews(viewsToFade, true)
                    true
                } else {
                    false
                }
            }

            imageViewLocation.setOnClickListener {

                getLocationImage(documentId) { imageLink ->
                    val intent = Intent(requireContext(), FullscreenImageActivity::class.java)
                    intent.putExtra("imageLink", imageLink)
                    startActivity(intent)
                }
            }

        }
        return view
    }

    private fun showHiddenViews() {
        ratingBarLocation.visibility = View.VISIBLE
        materialDivider.visibility = View.VISIBLE
        materialDividerComments2.visibility = View.VISIBLE
        commentTextView.visibility = View.VISIBLE
        recyclerViewComments.visibility = View.VISIBLE
        commentWrapText.visibility = View.VISIBLE
        commentThis.visibility = View.VISIBLE
        commentButton.visibility = View.VISIBLE
    }

    private fun topBarAndMenuSetup(documentId: String) {
        //Setup topbar etc
        activity?.let {
            setupThisFragment(it)

            topAppBar.setNavigationOnClickListener {
                goBackStuff()
            }

            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.user -> {
                        val intent = Intent(requireContext(), ProfileActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    R.id.showmap -> {
                        (requireContext() as StartActivity).showMapsFragment(documentId)
                        true
                    }

                    R.id.edit -> {
                        if (enableEdit) {
                            val intent = Intent(requireContext(), AddLocationActivity::class.java)
                            intent.putExtra("documentId", documentId)
                            startActivity(intent)
                        } else {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Edit location")
                                .setMessage("Nope. You can only edit and delete your own locations.")
                                .setNeutralButton("OK") { dialog, which ->

                                }
                                .show()
                        }
                        true
                    }

                    R.id.share -> {
                        shareLocation()
                        true
                    }

                    R.id.delete -> {
                        if (enableEdit) {
                            deleteLocation(documentId)
                        } else {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Delete location")
                                .setMessage("Nope. You can only edit and delete your own locations.")
                                .setNeutralButton("OK") { dialog, which ->

                                }
                                .show()
                        }
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
        var dateString = ""
        if (date != null) {
            dateString = format.format(date)
        }

        dateOfPhotoLocation.text = dateString
    }

    private fun fixTitleOfLocation(checkTitle: String): String {

        var titleOfLocationIs = checkTitle

        if (titleOfLocationIs.length > 13) {
            titleOfLocationIs = titleOfLocationIs.take(12) + "..."
        }

        return titleOfLocationIs

    }

    private fun Fragment.hideKeyboard() {
        val inputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val currentFocusedView = activity?.currentFocus
        currentFocusedView?.let {
            inputMethodManager?.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    private fun addComment(locationId: String) {
        val user = auth.currentUser

        val commentThisString = commentThis.text.toString()

        progressBar.visibility = View.VISIBLE

        commentButton.isEnabled = false
        commentThis.isEnabled = false
        commentThis.setText("")

        if (user == null) {
            // Visa ett felmeddelande eller på annat sätt hantera situationen
            Log.w("!!!", "User not signed in")
        } else {
            //Create Comment Object
            val comment = Comment(
                userId = user.uid,
                comment = commentThisString,
                locationId = locationId
            )
            Log.d("!!!", "adding comment")
            db.collection("comments").add(comment)
                .addOnSuccessListener { documentReference ->
                    Log.d("!!!", "DocumentSnapshot added with ID: ${documentReference.id}")

                    progressBar.visibility = View.GONE
                    showComments(locationId)
                    recyclerViewComments.adapter?.notifyDataSetChanged()
                    fadeViews(viewsToFade, false)
                    commentButton.isEnabled = true
                    commentThis.isEnabled = true
                }
                .addOnFailureListener { e ->
                    Log.w("!!!", "Error adding comment", e)
                    fadeViews(viewsToFade, false)
                    commentButton.isEnabled = true
                }

        }
    }

    private fun showComments(locationId: String) {

        commentList.clear()
        db.collection("comments").whereEqualTo("locationId", locationId)
            .orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val comment = document.toObject(Comment::class.java)
                    commentList.add(comment)
                }
                //Set to adapater
                val adapter = CommentRecyclerAdapter(requireContext(), commentList)
                recyclerViewComments.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.d("!!!", "GET failed with ", exception)
            }
    }

    private fun getLocationData(documentId: String) {
        db.collection("locations").document(documentId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("!!!", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val location = snapshot.toObject(Location::class.java)

                if (location != null) {

                    //Get user info userId
                    val userId = location.userId
                    if (userId != null) {
                        getUserName(userId, location)

                    }

                    //Get and set ratingBar
                    if (location.rating != null) {

                        ratingBarLocation.rating = location.rating!!.toFloat()
                        ratingBarLocation.isEnabled = false
                    }

                    //For full view, full name of lcation should be printed
                    locationTitle.text = location.name
                    //Full description
                    descriptionLocation.text = location.description

                    //Fix date of photo
                    fixPhotoDate(location)

                    //Print Lat/Long
                    latlongTextView.text = "@ ${location.lat} / ${location.long}"

                    //Get and set Image
                    if (location.imageLink != null) {
                        Glide.with(requireContext()).load(location.imageLink).centerCrop()
                            .into(imageViewLocation)
                    } else {
                        imageViewLocation.setImageResource(R.drawable.default1)
                    }

                    setForShare(location)

                    val user = auth.currentUser
                    if (location.userId == user?.uid) {
                        enableEdit = true
                    }
                }
            } else {
                Log.d("!!!", "Current data: null")
            }
        }
    }

    private fun getLocationTitle(documentId: String) {

        db.collection("locations").document(documentId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("!!!", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val checkTitle = snapshot.getString("name") ?: "Location"
                val titleOfLocationIs = fixTitleOfLocation(checkTitle)
                topAppBar.title = titleOfLocationIs
            } else {
                Log.d("!!!", "Current data: null")
            }
        }
    }

    private fun getLocationImage(documentId: String, callback: (String) -> Unit) {
        db.collection("locations").document(documentId).get()
            .addOnSuccessListener { document ->
                val imageLink = document.getString("imageLink") ?: "Location"
                callback(imageLink)
            }
            .addOnFailureListener { exception ->
                Log.d("!!!", "GET failed with ", exception)
            }
    }

    private fun getUserName(userId: String, location: Location) {
        db.collection("users").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { userDocument ->
                //Handle on success
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

    private fun deleteLocation(documentId: String) {

        val imageLinked = getLocationImage(documentId) { imageLink ->


            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete this location")
                .setMessage("Are you sure you want to delete this location and all data attached to it?")
                .setNeutralButton("Cancel") { dialog, which ->
                    // Respond to neutral button press
                }
                .setPositiveButton("Yes, I want to delete this location") { dialog, which ->
                    //Delete the location and comments and images related to this place


                            val batch = db.batch()

                            val locationRef = db.collection("locations").document(documentId)
                            batch.delete(locationRef)

                            db.collection("comments").whereEqualTo("locationId", documentId).get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        batch.delete(document.reference)
                                    }
                                }

                            // Kör batchen
                            batch.commit()
                                .addOnSuccessListener {
                                    val storage = Firebase.storage

                                    val imageRef = storage.getReferenceFromUrl(imageLink)

                                    imageRef.delete()
                                        .addOnSuccessListener {
                                            activity?.let {
                                                (activity as StartActivity).showFragment(
                                                    R.id.container,
                                                    ListLocationFragment(),
                                                    false
                                                )
                                            }
                                            Toast.makeText(
                                                requireContext(),
                                                "Location deleted",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        .addOnFailureListener { e ->
                                            // Det uppstod ett fel när bilden skulle raderas
                                        }
                                }
                                .addOnFailureListener { e ->
                                    // Visa ett felmeddelande till användaren
                                }
                }
                .show()
        }
    }

    private fun setForShare(location: Location) {
        lat = location.lat
        long = location.long
        imageLink = location.imageLink
        titleLocationToShare = location.name
    }

    private fun shareLocation() {

        // Check if Google Maps app is installed
        val mapsAppPackage = "com.google.android.apps.maps"
        val mapsIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat%2C$long")
        )
        mapsIntent.setPackage(mapsAppPackage)

        var googleMapsLink = ""
        if (mapsIntent.resolveActivity(requireContext().packageManager) != null) {
//             googleMapsLink = "geo:$lat,$long"
            googleMapsLink = "https://www.google.com/maps/search/?api=1&query=$lat%2C$long"
        } else {
            googleMapsLink = "https://www.google.com/maps/search/?api=1&query=$lat%2C$long"
        }

        val imageUri = imageLink

        val shareString = "$titleLocationToShare - View it on Google Maps: "

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, googleMapsLink)
//            putExtra(Intent.EXTRA_TEXT, shareString)
//            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun goBackStuff() {
        Log.d("!!!", "${SharedData.prevFragment}")
        when (SharedData.prevFragment) {
            is MapsFragment -> {
                (activity as StartActivity).goBack(R.id.item_2, SharedData.prevFragment)
            }

            is ListLocationFragment -> {
                (activity as StartActivity).goBack(R.id.item_1, SharedData.prevFragment)
            }

            else -> {
                (activity as StartActivity).goBack(R.id.item_1, SharedData.prevFragment)
            }
        }
        addItemButton.show()

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

    private fun fadeViews(views: List<View>, fadeOut: Boolean) {
        val alphaValue = if (fadeOut) 0.5f else 1f

        views.forEach { view ->
            view.animate()
                .alpha(alphaValue)
                .setDuration(2000)
                .setListener(null)
        }
    }

    private fun initializeViews(view: View) {
        imageViewLocation = view.findViewById(R.id.imageViewLocation)
        ratingBarLocation = view.findViewById(R.id.ratingBarLocation)
        locationTitle = view.findViewById(R.id.titleLocation)
        descriptionLocation = view.findViewById(R.id.descriptionLocationTextView)
        dateOfPhotoLocation = view.findViewById(R.id.dateOfPhotoTextView)
        latlongTextView = view.findViewById(R.id.latlongTextView)
        addedByUser = view.findViewById(R.id.addedByUserTextView)
        materialDivider = view.findViewById(R.id.materialDivider)


        materialDividerComments2 = view.findViewById(R.id.materialDividerComments2)
        commentTextView = view.findViewById(R.id.commentTextView)
        recyclerViewComments = view.findViewById(R.id.recyclerViewComments)
        commentWrapText = view.findViewById(R.id.commentWrapTextView)
        commentThis = view.findViewById(R.id.commentThis)
        commentButton = view.findViewById(R.id.commentButton)

        viewsToFade = listOf(commentWrapText, commentThis)

    }
}
