package com.gemini.ai

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.drjacky.imagepicker.ImagePicker
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.java.GenerativeModelFutures
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.gemini.ai.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUi()
    }

    private fun setupUi() {
        binding.apply {
            uploadImage.setOnClickListener {
                openImagePickerDialog()
            }

            submitBtn.setOnClickListener {
                if (bitmap != null) {
                    submitBtn.startAnimation()
                    recogniseImage()
                }else{
                    Toast.makeText(this@MainActivity, "Please select an image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun recogniseImage() {
        val generativeModel = GenerativeModel(
            "gemini-1.5-flash-latest",
            BuildConfig.GEMINI_API_KEY
        )
        val model = GenerativeModelFutures.from(generativeModel)
        val content: Content = Content.Builder()
            .text("What is this image?")
            .image(bitmap!!)
            .build()
        val response: ListenableFuture<GenerateContentResponse> = model.generateContent(content)
        Futures.addCallback(response, object : FutureCallback<GenerateContentResponse?> {

            override fun onSuccess(result: GenerateContentResponse?) {
                binding.submitBtn.revertAnimation()
                result?.let {
                    val resultText = result.text
                    binding.resultTv.text = resultText
                }
            }

            override fun onFailure(t: Throwable) {
                binding.submitBtn.revertAnimation()
                binding.resultTv.text = t.toString()
            }
        }, this.mainExecutor)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = it.data?.data!!
                uri?.let {
                    setResultToImageView(it)
                }
            }
        }

    private fun setResultToImageView(imageUri: Uri) {
        try {
            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
            binding.uploadImage.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun openImagePickerDialog() {
        val popupDialog = PopupDialog(this)
        popupDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        popupDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupDialog.setCameraClickListener {
            launcher.launch(
                ImagePicker.with(this@MainActivity)
                    .crop()
                    .cropFreeStyle()
                    .cameraOnly()
                    .createIntent()
            )
        }
        popupDialog.setGalleryClickListener {
            launcher.launch(
                ImagePicker.with(this@MainActivity)
                    .crop()
                    .cropFreeStyle()
                    .galleryOnly()
                    .createIntent()
            )
        }

        popupDialog.show()
    }


}