package com.dicoding.asclepius.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.helper.ImageViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var imageViewModel: ImageViewModel  // Tambahkan ViewModel
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageViewModel.currentImageUri = uri  // Simpan Uri ke ViewModel
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
            showToast("Tidak ada gambar yang dipilih")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi ViewModel
        imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)

        // Inisialisasi ImageClassifierHelper
        imageClassifierHelper = ImageClassifierHelper(this)

        // Set onClick listener untuk tombol buka galeri
        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        // Set tombol "Analyze" untuk memulai klasifikasi
        binding.analyzeButton.setOnClickListener {
            imageViewModel.currentImageUri?.let { uri ->
                analyzeImage(uri)
            } ?: showToast("Pilih gambar terlebih dahulu.")
        }

        // Menampilkan gambar jika ada Uri yang sudah disimpan
        imageViewModel.currentImageUri?.let { uri ->
            showImage()
        }
    }

    private fun startGallery() {
        // Meluncurkan intent galeri
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImage() {
        imageViewModel.currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(imageUri: Uri) {
        try {
            val bitmap = getBitmapFromUri(imageUri)  // Konversi Uri ke Bitmap
            val (label, confidence) = imageClassifierHelper.classifyStaticImage(bitmap)  // Kirim Bitmap ke helper

            // Log hasil klasifikasi untuk debugging
            Log.d("ImageClassifier", "Predicted label: $label, Confidence: $confidence")

            // Kirim hasil ke ResultActivity
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("LABEL", label)
                putExtra("CONFIDENCE", confidence)
                putExtra("IMAGE_URI", imageUri.toString())
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Gagal memproses gambar.")
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream) ?: throw Exception("Gagal mengonversi gambar.")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


