package com.example.myheliports

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myheliports.databinding.ActivityFullscreenImageBinding

class FullscreenImageActivity : AppCompatActivity() {

    private lateinit var fullScreenImageView: ImageView

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var matrix = Matrix()
    private var savedMatrix = Matrix()

    private val NONE = 0
    private var mode = NONE
    private val DRAG = 1
    private val ZOOM = 2
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        fullScreenImageView = findViewById(R.id.fullscreen_content)

        val imageLink = intent.getStringExtra("imageLink")
        // Get and set Image
        if (imageLink != null) {
            Glide.with(this).load(imageLink)
                .into(fullScreenImageView)
        } else {
            fullScreenImageView.setImageResource(R.drawable.default1)
        }

        // Set scaleType programmatically
        fullScreenImageView.scaleType = ImageView.ScaleType.MATRIX

        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                matrix.postScale(detector.scaleFactor, detector.scaleFactor, detector.focusX, detector.focusY)
                fullScreenImageView.imageMatrix = matrix
                return true
            }
        })

        fullScreenImageView.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(matrix)
                    start.set(event.x, event.y)
                    mode = DRAG
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(event)
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix)
                        midPoint(mid, event)
                        mode = ZOOM
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG) {
                        matrix.set(savedMatrix)
                        matrix.postTranslate(event.x - start.x, event.y - start.y)
                    } else if (mode == ZOOM) {
                        val newDist = spacing(event)
                        if (newDist > 10f) {
                            matrix.set(savedMatrix)
                            val scale = newDist / oldDist
                            matrix.postScale(scale, scale, mid.x, mid.y)
                        }
                    }
                }
            }
            fullScreenImageView.imageMatrix = matrix
            return@setOnTouchListener true
        }
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt(x * x + y * y.toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }
}
