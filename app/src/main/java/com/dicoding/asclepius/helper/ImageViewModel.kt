package com.dicoding.asclepius.helper

import android.net.Uri
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    // Menyimpan Uri gambar yang dipilih
    var currentImageUri: Uri? = null
}
