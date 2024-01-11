package com.example.myheliports

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

// ViewModel class to hold the selected photo Uri
class PhotoViewModel : androidx.lifecycle.ViewModel() {

    private val _photoLiveData = MutableLiveData<Uri>()
    val photoLiveData: LiveData<Uri> get() = _photoLiveData

    fun setPhotoUri(uri: Uri) {
        _photoLiveData.value = uri
    }
}