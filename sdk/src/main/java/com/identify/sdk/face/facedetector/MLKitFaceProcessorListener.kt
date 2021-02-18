package com.identify.sdk.face.facedetector

import com.google.mlkit.vision.face.Face

interface MLKitFaceProcessorListener {
    fun success(face: MutableList<Face>)
}