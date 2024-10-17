package com.gemini.ai

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button

class PopupDialog(context: Context) : Dialog(context) {

    private var cameraClickListener: () -> Unit = {}
    private var galleryClickListener: () -> Unit = {}

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.image_picker_dialog, null)
        setContentView(view)

        view.findViewById<Button>(R.id.cameraButton).setOnClickListener {
            cameraClickListener()
            dismiss()
        }

        view.findViewById<Button>(R.id.galleryButton).setOnClickListener {
            galleryClickListener()
            dismiss()
        }
    }

    fun setCameraClickListener(listener: () -> Unit) {
        cameraClickListener = listener
    }

    fun setGalleryClickListener(listener: () -> Unit) {
        galleryClickListener = listener
    }
}