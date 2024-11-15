package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari intent
        val label = intent.getStringExtra("LABEL") ?: "Unknown"
        val confidence = intent.getFloatExtra("CONFIDENCE", 0.0f)
        val imageUriString = intent.getStringExtra("IMAGE_URI")

        // Tampilkan gambar yang dipilih
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            binding.resultImage.setImageURI(imageUri)
        }

        // Tampilkan hasil klasifikasi
        val confidenceText = "%.2f".format(confidence * 100) + "%"
        binding.resultText.text = "Prediction: $label\nConfidence: $confidenceText"

        // Log hasil prediksi untuk debugging
        Log.d("ResultActivity", "Predicted label: $label, Confidence: $confidenceText")
    }
}

