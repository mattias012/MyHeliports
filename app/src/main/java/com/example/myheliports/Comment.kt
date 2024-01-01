package com.example.myheliports

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Comment(
    @DocumentId
    var documentId : String? = null,
    var userId: String? = null,
    var comment : String? = null,
    var locationId : String? = null,
    @ServerTimestamp
    var timestamp: Timestamp? = null) {
}