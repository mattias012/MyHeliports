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
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
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

    private var photoFile: File? = null
    var dateOfPhotoTimestamp: Timestamp? = null
    var file: File? = null  // Definiera file här
    var fileURI: Uri? = null
    var rating: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        db = Firebase.firestore
        auth = Firebase.auth
        storage = Firebase.storage

        initializeViews()
        requestPermission()

        imageView.setOnClickListener {
            showImagePickerDialog()
        }

        showPhotoView()
        handleDate()

        saveLocationButton.setOnClickListener {
            saveLocation()
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
        // Create an intent to capture an image
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            // Create the file where the photo should go
            photoFile = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }

            // Continue only if the File was successfully created
            photoFile?.let {

                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.myheliports.fileprovider",
                    it
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, REQUEST_CAMERA)
            }
        }
    }

    private fun createImageFile(): File {

        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Create the file
        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )

        file = imageFile

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = imageFile.absolutePath
        Log.d("PhotoPath", "Current Photo Path: $currentPhotoPath")

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DATA, imageFile.absolutePath)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)


        return imageFile
    }

    private fun extractExifData(photoPath: String? = null, uri: Uri? = null) {

        val exifInterface = when {
            photoPath != null -> ExifInterface(photoPath)
            uri != null -> {
                val inputStream = contentResolver.openInputStream(uri)
                ExifInterface(inputStream!!)

            }

            else -> throw IllegalArgumentException("Both photoPath and uri cannot be null")
        }

        // Extract date
        val dateTaken = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)

        if (!dateTaken.isNullOrBlank()) {

            // Skapa ett SimpleDateFormat-objekt för att tolka datumet från ExifInterface
            val formatExif = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())

            // Konvertera dateTaken till ett Date-objekt
            val date = formatExif.parse(dateTaken)

            dateOfPhotoTimestamp = Timestamp(date)

            // Skapa ett annat SimpleDateFormat-objekt för att formatera datumet som du vill ha det
            val formatThis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Konvertera date till en sträng med önskat format
            val dateString = formatThis.format(date)
            dateOfPhoto.setText(dateString)
        }

        // Extract location
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
                        extractExifData(null, uri)
                        fileURI = uri
                    }
                }
            }

            REQUEST_CAMERA -> {
                if (resultCode == RESULT_OK) {
                    // Handle the photo taken by the camera
                    photoFile?.let {
                        val photoUri = Uri.fromFile(it)
                        photoViewModel.setPhotoUri(photoUri)
                        // Extract EXIF data from the camera image
                        extractExifData(it.absolutePath, null)
                    } ?: run {
                        // Handle the case where photoFile is null
                        Log.e("Camera", "Photo file is unexpectedly null")
                    }
                }
            }
        }
    }

    private fun saveLocation() {

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

        var fileLocation = if (file != null) {
            Uri.fromFile(file)
        } else {
            fileURI
        }

        if (fileLocation == null) {
            val defaultImageResId = R.raw.default1
            val inputStream = resources.openRawResource(defaultImageResId)
            val defaultFile = File(this.filesDir, "default1.jpg")
            inputStream.use { input ->
                defaultFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            fileLocation = Uri.fromFile(defaultFile)
        }

        val fileName = fileLocation?.lastPathSegment.toString()  // Detta ger dig filnamnet
        val storageRef = storage.reference.child("images/$fileName")

        val uploadTask = fileLocation?.let { storageRef.putFile(it) }

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        uploadTask?.addOnProgressListener { taskSnapshot ->
            // Beräkna uppladdningsprogressen
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount

            // Visa ProgressBar och uppdatera framsteg
            progressBar.visibility = View.VISIBLE
            fadeViews(viewsToFade, true)
            progressBar.progress = progress.toInt()
        }

        uploadTask?.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->

                val imageUrl = uri.toString()
                val user = auth.currentUser

                if (user == null) {
                    // Visa ett felmeddelande eller på annat sätt hantera situationen
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
        }
        uploadTask?.addOnFailureListener { exception ->
            // Logga felet för felsökning
            Log.w("!!!", "Upload failed: $exception")

            progressBar.visibility = View.GONE

            fadeViews(viewsToFade, false)
            saveLocationButton.isEnabled = true

            // Visa ett felmeddelande till användaren
            Toast.makeText(this, "Save failed: ${exception.message}", Toast.LENGTH_LONG).show()
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

        imageView = findViewById(R.id.imageView)

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