package com.example.myheliports

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*


class AddLocationActivity : AppCompatActivity() {


    companion object {
        private const val REQUEST_ALL_PERMISSIONS = 100
        private const val REQUEST_GALLERY = 200
        private const val REQUEST_CAMERA = 300
        private const val REQUEST_MEDIA_LOCATION = 400
        private const val REQUEST_READ = 500
        private const val REQUEST_WRITE = 600

    }

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var storage: FirebaseStorage

    lateinit var nameOfLocation: TextInputEditText
    lateinit var dateOfPhoto: TextInputEditText
    lateinit var descriptionOfLocation: TextInputEditText
    lateinit var latOfLocation: TextInputEditText
    lateinit var longOfLocation: TextInputEditText
    lateinit var ratingView: RatingBar

    lateinit var nameOfLocationView: TextInputLayout
    lateinit var dateOfPhotoView: TextInputLayout
    lateinit var descriptionOfLocationView: TextInputLayout
    lateinit var latOfLocationView: TextInputLayout
    lateinit var longOfLocationView: TextInputLayout

    lateinit var viewsToFade: List<View>

    private lateinit var photoViewModel: PhotoViewModel
    lateinit var imageView: ImageView

    private lateinit var saveLocationButton: Button

    private lateinit var currentPhotoPath: String

    private var photoUri: Uri? = null
    private var dateOfPhotoTimestamp: Timestamp? = null
    var file: File? = null  // Definiera file här
    private var fileURI: Uri? = null
    private var fileName: String? = null
    private var rating: Int? = null

//    private var defaultFile: File? = null  // Definiera file här
//    private var defaultFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        db = Firebase.firestore
        auth = Firebase.auth
        storage = Firebase.storage

//        defaultFile = File(this.filesDir, "default1.jpg")
//        defaultFileUri = Uri.fromFile(defaultFile)

        val documentId = intent.getStringExtra("documentId")

        if (documentId != null) {
            getLocationData(documentId)
        }

        initializeViews()
        requestPermission()

        imageView.setOnClickListener {
            showImagePickerDialog()
        }

        showPhotoView()
        handleDate()

        saveLocationButton.setOnClickListener {
            saveLocation(documentId)
        }

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.user -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.help -> {

                    MaterialAlertDialogBuilder(this@AddLocationActivity)
                        .setTitle("How to add a location")
                        .setMessage("Add a new spot to your logbook. If you select a photo we'll extract the data from the image to help you out.")
                        .setNeutralButton("OK") { dialog, which ->
                            // Respond to neutral button press
                        }
                        .show()
                    true
                }

                else -> false
            }
        }
    }


    private fun getLocationData(documentId: String) {
        db.collection("locations").document(documentId).get()
            .addOnSuccessListener { document ->
                val location = document.toObject(Location::class.java)
                location?.let {

                    //Get and set ratingBar
                    if (location.rating != null) {
                        ratingView.rating = location.rating!!.toFloat()
                        ratingView.isEnabled = true
                    }

                    //For full view, full name of lcation should be printed
                    nameOfLocation.setText(location.name)
                    //Full description
                    descriptionOfLocation.setText(location.description)

                    //Fix date of photo
                    if (location.dateOfPhoto != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateString = sdf.format((location.dateOfPhoto!!.toDate()))
                        dateOfPhoto.setText(dateString)
                    }
                    //Print Lat/Long
                    latOfLocation.setText(location.lat.toString())
                    longOfLocation.setText(location.long.toString())

                    //Get and set Image
                    if (location.imageLink != null) {
                        Glide.with(this)
                            .load(location.imageLink)
                            .into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.default1);
                    }

                } ?: run {
                    Log.d("!!!", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("!!!", "GET failed with ", exception)
            }
    }

    private fun handleDate() {

        dateOfPhotoView.setEndIconOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                    .setTitleText("Select date")
                    .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateString = sdf.format(Date(selection))
                dateOfPhoto.setText(dateString)

                // Skapa en Timestamp för Firestore
                dateOfPhotoTimestamp = Timestamp(Date(selection))
            }
            datePicker.show(supportFragmentManager, "tag")
        }
    }

    private fun showPhotoView() {
        photoViewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)

        // Observe changes in the photoLiveData
        photoViewModel.photoLiveData.observe(this) { uri ->
            // Update the imageView with the selected or captured image
            imageView.setImageURI(uri)
            // Set the ScaleType to fit the width or height of the image
            imageView.scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    private fun requestPermission() {
        // Check for camera and storage permissions
        if (!checkPermissions()) {

            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_MEDIA_LOCATION
                ),
                REQUEST_ALL_PERMISSIONS
            )
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_MEDIA_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showImagePickerDialog() {
        // Create an array of options (camera and gallery)
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        // Create a dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera() // Take Photo
                1 -> openGallery() // Choose from Gallery
                2 -> dialog.dismiss() // Cancel
            }
        }

        // Show the dialog
        builder.show()
    }

    private fun openGallery() {
        // Create an intent to pick an image from the gallery
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    private fun openCamera() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            photoUri = createImageFile()
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(takePictureIntent, REQUEST_CAMERA)
        }
    }

    private fun createImageFile(): Uri {

        val displayName = "myImage_${System.currentTimeMillis()}"
        fileName = displayName
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyAppImages")
        }

        // Skapa en ny fil i MediaStore och få dess URI.
        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        fileURI = uri
        return uri ?: throw IOException("Failed to create new MediaStore record.")
    }

    private fun extractExifData(uri: Uri? = null) {

        val exifInterface = when {

            uri != null -> {
                val inputStream = contentResolver.openInputStream(uri)
                ExifInterface(inputStream!!)
            }

            else -> throw IllegalArgumentException("uri cannot be null")
        }

        // Extract date
        val dateTaken = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)

        if (!dateTaken.isNullOrBlank()) {

            //Create a SimpleDateFormat object to interpret ExifInterface
            val formatExif = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())

            //Convert to Date
            val date = formatExif.parse(dateTaken)
            dateOfPhotoTimestamp = Timestamp(date)

            //Convert again
            val formatThis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = formatThis.format(date)
            dateOfPhoto.setText(dateString)
        }

        //Extract location
        val latLong = FloatArray(2)
        val hasLatLong = exifInterface.getLatLong(latLong)

        if (hasLatLong) {

            val latitude = latLong[0]
            val longitude = latLong[1]

            latOfLocation.setText(latitude.toString())
            longOfLocation.setText(longitude.toString())

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_GALLERY -> {
                if (resultCode == RESULT_OK) {
                    // Handle the selected image from the gallery
                    data?.data?.let { uri ->
                        photoViewModel.setPhotoUri(uri)
                        extractExifData(uri)
                        fileURI = uri
                    }
                }
            }

            REQUEST_CAMERA -> {
                if (resultCode == RESULT_OK) {
                    photoUri?.let { uri ->
                        photoViewModel.setPhotoUri(uri)
                        extractExifData(uri)
                        fileURI = uri
                    }
                }
            }
        }
    }

    private fun uploadImage(
        uri: Uri,
        fileName: String,
        documentId: String?,
        latDouble: Double,
        longDouble: Double
    ): Task<Uri> {

//        if (uri != defaultFileUri) {
        //if not standard file it means that user HAS changed the photo and we need to upload it

        val storageRef = storage.reference.child("images/$fileName")

        val inputStream = contentResolver.openInputStream(uri)
        val data = inputStream?.readBytes()

        if (data == null) {
            Log.w("!!!", "Failed to read file data")
            return Tasks.forException(IOException("Failed to read file data"))
        }

        val uploadTask = storageRef.putBytes(data)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            progressBar.visibility = View.VISIBLE
            progressBar.progress = progress.toInt()
        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val downloadUri = task.result
                val imageUrl = downloadUri.toString()

                if (documentId != null){
                    updateLocationData(documentId, latDouble, longDouble, imageUrl)
                }
                else {
                    addLocation(latDouble, longDouble, imageUrl)
                }
            } else {

                Log.w("!!!", "Upload failed")

                progressBar.visibility = View.GONE

                fadeViews(viewsToFade, false)
                saveLocationButton.isEnabled = true

                //Show fail message
                Toast.makeText(this, "Save failed", Toast.LENGTH_LONG).show()
            }
        }
        return uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {

                Toast.makeText(this, "Location added", Toast.LENGTH_LONG).show()
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }
//        }
//
//        return Tasks.forResult(uri)
    }

    private fun saveLocation(documentId: String?) {

        saveLocationButton.isEnabled = false

        val lat = latOfLocation.text.toString()
        val long = longOfLocation.text.toString()

        val checkCoordinates = checkLatLong(lat, long)

        if (!checkCoordinates) {
            latOfLocationView.error = getString(R.string.error)
            longOfLocationView.error = getString(R.string.error)
            saveLocationButton.isEnabled = true
            return
        } else {
            latOfLocationView.error = null
            longOfLocationView.error = null
        }

        val latDouble = lat.toDouble()
        val longDouble = long.toDouble()

        rating = ratingView.numStars

        //In case of a new location
        if (documentId == null) {
            if (fileURI == null) {
                val defaultImageResId = R.raw.default1
                val inputStream = resources.openRawResource(defaultImageResId)
                val defaultFile = File(this.filesDir, "default1.jpg")
                inputStream.use { input ->
                    defaultFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                fileURI = Uri.fromFile(defaultFile)
            }

            val fileName: String? =
                fileURI?.let {
                    contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        cursor.getString(nameIndex)
                    }
                }

            val mimeType = fileURI?.let { contentResolver.getType(it) }
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

            val fullFileName = "$fileName.$extension"

            fileURI?.let {
                uploadImage(it, fullFileName, null, latDouble, longDouble)
            }
        }
        //In case of an edit
        else {

            //If fileURI is null that means that no new image has been selected and we don't upload a new one
            if (fileURI == null) {

                updateLocationData(documentId, latDouble, longDouble, null)
            }

            //else update location with new data and new photo
            else {

                val fileName: String? =
                    fileURI?.let {
                        contentResolver.query(it, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            cursor.moveToFirst()
                            cursor.getString(nameIndex)
                        }
                    }

                val mimeType = fileURI?.let { contentResolver.getType(it) }
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)

                val fullFileName = "$fileName.$extension"

                fileURI?.let {
                    uploadImage(it, fullFileName, documentId, latDouble, longDouble)

                }
            }
        }
    }
    private fun updateLocationData(documentId: String, latDouble: Double, longDouble: Double, imageUrl: String?) {

        val docRef = db.collection("locations").document(documentId)

        if (imageUrl != null)
        docRef
            .update("name", nameOfLocation.text.toString(), "dateOfPhoto", dateOfPhotoTimestamp, "description", descriptionOfLocation.text.toString(), "rating", rating, "lat", latDouble, "long", longDouble, "lastEdit", null, "imageLink", imageUrl)

            .addOnSuccessListener {
                Log.d("!!!", "DocumentSnapshot successfully updated!")
                finish()
            }
            .addOnFailureListener { e -> Log.w("!!!", "Error updating document", e) }
        else {
            docRef
                .update("name", nameOfLocation.text.toString(), "dateOfPhoto", dateOfPhotoTimestamp, "description", descriptionOfLocation.text.toString(), "rating", rating, "lat", latDouble, "long", longDouble, "lastEdit", null)

                .addOnSuccessListener {
                    Log.d("!!!", "DocumentSnapshot successfully updated!")
                    finish()
                }
                .addOnFailureListener { e -> Log.w("!!!", "Error updating document", e) }
        }

    }

    private fun addLocation(latDouble: Double, longDouble: Double, imageUrl: String) {
        val user = auth.currentUser

        if (user == null) {
            //Error handling in case user  is null
            Log.w("!!!", "User not signed in")
        } else {
            //Create Location Object
            val location = Location(
                name = nameOfLocation.text.toString(),
                dateOfPhoto = dateOfPhotoTimestamp,
                description = descriptionOfLocation.text.toString(),
                lat = latDouble,
                long = longDouble,
                rating = rating,
                imageLink = imageUrl,
                userId = user.uid
            )

            db.collection("locations").add(location)
                .addOnSuccessListener { documentReference ->
                    Log.d("!!!", "DocumentSnapshot added with ID: ${documentReference.id}")
                    val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                    progressBar.visibility = View.GONE
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w("!!!", "Error adding document", e)

                    fadeViews(viewsToFade, false)
                    saveLocationButton.isEnabled = true
                }
        }
    }

    private fun checkLatLong(latStr: String?, longStr: String?): Boolean {

        try {
            val lat = latStr?.toDouble()
            val long = longStr?.toDouble()

            if (lat == null || long == null) {
                return false
            }

            if (lat !in -90.0..90.0 || long !in -180.0..180.0) {
                return false
            }
            return true
        } catch (e: NumberFormatException) {
            //Catch errors..
        }
        return false
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

    private fun initializeViews() {
        nameOfLocationView = findViewById(R.id.nameOfLocationView)
        dateOfPhotoView = findViewById(R.id.dateOfPhotoView)
        descriptionOfLocationView = findViewById(R.id.descriptionOfLocationView)
        latOfLocationView = findViewById(R.id.latOfLocationView)
        longOfLocationView = findViewById(R.id.longOfLocationView)

        nameOfLocation = findViewById(R.id.nameOfLocation)
        dateOfPhoto = findViewById(R.id.dateOfPhoto)
        descriptionOfLocation = findViewById(R.id.descriptionOfLocation)
        latOfLocation = findViewById(R.id.latOfLocation)
        longOfLocation = findViewById(R.id.longOfLocation)
        ratingView = findViewById(R.id.ratingBar)

        saveLocationButton = findViewById(R.id.saveLocationButton)

        imageView = findViewById(R.id.imageViewUser)

        viewsToFade = listOf(
            nameOfLocationView,
            dateOfPhotoView,
            imageView,
            descriptionOfLocationView,
            latOfLocationView,
            longOfLocationView,
            nameOfLocation,
            dateOfPhoto,
            descriptionOfLocation,
            latOfLocation,
            longOfLocation,
            ratingView
        )

    }
}