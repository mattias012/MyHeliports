package com.example.myheliports

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddLocationActivity : AppCompatActivity() {

    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    lateinit var backButton: AppCompatImageButton

    lateinit var nameOfLocation: TextInputEditText
    lateinit var descriptionOfLocation: TextInputEditText
    lateinit var latOfLocation: TextInputEditText
    lateinit var longOfLocation: TextInputEditText

    lateinit var nameOfLocationView: TextInputLayout
    lateinit var descriptionOfLocationView: TextInputLayout
    lateinit var latOfLocationView: TextInputLayout
    lateinit var longOfLocationView: TextInputLayout

    lateinit var saveLocationButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        db = Firebase.firestore
        auth = Firebase.auth

        backButton = findViewById(R.id.backButton)

        nameOfLocationView = findViewById(R.id.nameOfLocationView)
        descriptionOfLocationView = findViewById(R.id.descriptionOfLocationView)
        latOfLocationView = findViewById(R.id.latOfLocationView)
        longOfLocationView = findViewById(R.id.longOfLocationView)

        nameOfLocation = findViewById(R.id.nameOfLocation)
        descriptionOfLocation = findViewById(R.id.descriptionOfLocation)
        latOfLocation = findViewById(R.id.latOfLocation)
        longOfLocation = findViewById(R.id.longOfLocation)

        saveLocationButton = findViewById(R.id.saveLocationButton)


        backButton.setOnClickListener {
            finish()
        }

        saveLocationButton.setOnClickListener {
            saveLocation()
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