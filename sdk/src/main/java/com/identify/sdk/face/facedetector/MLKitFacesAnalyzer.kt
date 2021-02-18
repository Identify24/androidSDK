package com.identify.sdk.face.facedetector;

import android.graphics.*
import android.util.Log
import android.widget.ImageView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetector


class MLKitFacesAnalyzer internal constructor(
    private val faceDetector: FaceDetector,
    private val tv : PreviewView,
    private val iv : ImageView
) : ImageAnalysis.Analyzer {
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var dotPaint: Paint? = null
    private var linePaint: Paint? = null
    private var widthScaleFactor = 1.0f
    private var heightScaleFactor = 1.0f
    private var fbImage: InputImage? = null


     var faceProcessorListener : MLKitFaceProcessorListener ?= null




    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
           fbImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)

        }
        initDrawingUtils()
        detectFaces(image)

    }




    private fun initDrawingUtils() {
        bitmap = Bitmap.createBitmap(
            tv.width,
            tv.height,
            Bitmap.Config.ARGB_8888
        )
        canvas = Canvas(bitmap!!)
        dotPaint = Paint()
        dotPaint!!.color = Color.RED
        dotPaint!!.style = Paint.Style.FILL
        dotPaint!!.strokeWidth = 2f
        dotPaint!!.isAntiAlias = true
        linePaint = Paint()
        linePaint!!.color = Color.GREEN
        linePaint!!.style = Paint.Style.STROKE
        linePaint!!.strokeWidth = 2f
        widthScaleFactor = 1f
        heightScaleFactor = 0.97f
    }


    private fun detectFaces(image: ImageProxy) {
        faceDetector.process(fbImage)
            .addOnSuccessListener { firebaseVisionFaces ->
                if (firebaseVisionFaces.isNotEmpty()) {
                    processFaces(firebaseVisionFaces)
                    faceProcessorListener?.success(firebaseVisionFaces)
                } else {
                    iv.setImageDrawable(null);
                }
            }.addOnFailureListener { e ->
                Log.i(
                    TAG,
                    e.toString()
                )
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    private fun processFaces(faces: List<Face>) {
        for (face in faces) {
            face.getContour(FaceContour.FACE)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.LEFT_EYEBROW_BOTTOM)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.RIGHT_EYEBROW_BOTTOM)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.LEFT_EYE)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.RIGHT_EYE)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.LEFT_EYEBROW_TOP)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.RIGHT_EYEBROW_TOP)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.LOWER_LIP_BOTTOM)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.LOWER_LIP_TOP)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.UPPER_LIP_TOP)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.NOSE_BRIDGE)?.let {
                drawContours(it.points)
            }
            face.getContour(FaceContour.NOSE_BOTTOM)?.let {
                drawContours(it.points)
            }

        }
        iv.setImageBitmap(bitmap)
    }

    private fun drawContours(points: List<PointF>) {
        var counter = 0
        for (point in points) {
            if (counter != points.size - 1) {
                canvas?.drawLine(
                    translateX(point.x),
                    translateY(point.y),
                    translateX(points[counter + 1].x),
                    translateY(points[counter + 1].y),
                    linePaint!!
                )
            } else {
                canvas?.drawLine(
                    translateX(point.x),
                    translateY(point.y),
                    translateX(points[0].x),
                    translateY(points[0].y),
                    linePaint!!
                )
            }
            counter++
            canvas!!.drawCircle(
                translateX(point.x), translateY(point.y), 6f,
                dotPaint!!
            )
        }
    }

    private fun translateY(y: Float): Float {
        return y * heightScaleFactor
    }

    private fun translateX(x: Float): Float {
        val scaledX = x * widthScaleFactor
        return canvas?.width!! - scaledX

    }



    companion object {
        private const val TAG = "MLKitFacesAnalyzer"
    }


}
