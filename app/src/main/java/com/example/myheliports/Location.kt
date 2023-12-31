package com.example.myheliports

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Location(
    @DocumentId
    var documentId : String? = null,
    var dateOfPhoto: Timestamp? = null,
    var name: String? = null,
    var description: String? = null,
    var lat: Double? = null,
    var long: Double? = null,
    var rating: Int? = null,
    var imageLink: String? = null,
    var userId: String? = null,

    @ServerTimestamp
    var timestamp: Timestamp? = null,
    @ServerTimestamp
    var lastEdit: Timestamp? = null){

}