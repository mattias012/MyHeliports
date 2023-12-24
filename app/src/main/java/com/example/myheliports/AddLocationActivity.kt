package com.example.myheliports

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
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
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner

import androidx.lifecycle.observe
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import java.util.*


class AddLocationActivity : AppCompatActivity() {


    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_GALLERY = 200
        private const val REQUEST_CAMERA = 300
    }

    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

//    lateinit var backButton: AppCompatImageButton

    lateinit var nameOfLocation: TextInputEditText
    lateinit var descriptionOfLocation: TextInputEditText
    lateinit var latOfLocation: TextInputEditText
    lateinit var longOfLocation: TextInputEditText

    lateinit var nameOfLocationView: TextInputLayout
    lateinit var descriptionOfLocationView: TextInputLayout
    lateinit var latOfLocationView: TextInputLayout
    lateinit var longOfLocationView: TextInputLayout

    private lateinit var photoViewModel: PhotoViewModel
    lateinit var imageView: ImageView

    lateinit var saveLocationButton: Button

    private lateinit var currentPhotoPath: String

    private var photoFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        db = Firebase.firestore
        auth = Firebase.auth

//        backButton = findViewById(R.id.backButton)

        nameOfLocationView = findViewById(R.id.nameOfLocationView)
        descriptionOfLocationView = findViewById(R.id.descriptionOfLocationView)
        latOfLocationView = findViewById(R.id.latOfLocationView)
        longOfLocationView = findViewById(R.id.longOfLocationView)

        nameOfLocation = findViewById(R.id.nameOfLocation)
        descriptionOfLocation = findViewById(R.id.descriptionOfLocation)
        latOfLocation = findViewById(R.id.latOfLocation)
        longOfLocation = findViewById(R.id.longOfLocation)

        saveLocationButton = findViewById(R.id.saveLocationButton)

        imageView = findViewById(R.id.imageView)

        requestPermission()

        imageView.setOnClickListener {
            showImagePickerDialog()
        }

        photoViewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)

        // Observe changes in the photoLiveData
        photoViewModel.photoLiveData.observe(this) { uri ->
            // Update the imageView with the selected or captured image
            imageView.setImageURI(uri)
            // Set the ScaleType to fit the width or height of the image
            imageView.scaleType = ImageView.ScaleType.FIT_XY
        }


//        backButton.setOnClickListener {
//            finish()
//        }

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
                    // Handle favorite icon press
                    true
                }
                else -> false

            }
        }

    }
    fun requestPermission() {
        // Check for camera and storage permissions
        if (checkPermissions()) {
            // Show a dialog to let the user choose between camera and gallery
            showImagePickerDialog()
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_CAMERA_PERMISSION
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
                    "com.example.android.fileprovider",
                    it
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, REQUEST_CAMERA)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Create the file
        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )

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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_GALLERY -> {
                if (resultCode == RESULT_OK) {
                    // Handle the selected image from the gallery
                    data?.data?.let { uri ->
                        photoViewModel.setPhotoUri(uri)
                    }
                }
            }
            REQUEST_CAMERA -> {
                if (resultCode == RESULT_OK) {
                    // Handle the photo taken by the camera
                    // Since you already provided the URI when starting the camera intent,
                    // you don't need to get it from the data, but can use the one you already have.
                    photoFile?.let {
                        val photoUri = Uri.fromFile(it)
                        photoViewModel.setPhotoUri(photoUri)
                    } ?: run {
                        // Handle the case where photoFile is null
                        Log.e("Camera", "Photo file is unexpectedly null")
                    }
                }
            }
        }
    }

    fun saveLocation() {

        val lat = latOfLocation.text.toString()
        val long = longOfLocation.text.toString()

        val checkCoordinates = checkLatLong(lat, long)

        val colorStateListRed = ColorStateList.valueOf(Color.RED)
        val colorStateListWhite = ColorStateList.valueOf(Color.WHITE)

        if (!checkCoordinates) {
            latOfLocationView.error = getString(R.string.error)
            longOfLocationView.error = getString(R.string.error)
            return
        }
        else {
            latOfLocationView.error = null
            longOfLocationView.error = null
        }

        val latDouble = lat.toDouble()
        val longDouble = long.toDouble()

        //Create Location Object
        val location = Location(
            name= nameOfLocation.text.toString(),
            description = descriptionOfLocation.text.toString(),
            lat = latDouble,
            long = longDouble,
            rating = 3,
            imageLink = "greenland"
        )

        val user = auth.currentUser ?: return

        //db.collection("users").document(user.uid).collection("locations").add(location)
    }

    fun checkLatLong(latStr: String?, longStr: String?): Boolean {

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
}