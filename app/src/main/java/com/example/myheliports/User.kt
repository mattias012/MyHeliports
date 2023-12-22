package com.example.myheliports

import com.google.firebase.firestore.DocumentId

class User(var userId: DocumentId,
var userName : String? = null) {
}