@file:Suppress("DEPRECATION")

package soko.ekibun.tilesbox.view

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import soko.ekibun.tilesbox.util.CameraUtil

class CameraView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    var camera: Camera? = null

    private var distZoom = 0f

    private val cameraInstance: Camera?
        get() {
            if (camera == null)
                try {
                    camera = Camera.open()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            return camera
        }

    init {
        init()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        loadCamera()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        loadCamera()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (camera == null)
            return false
        if (event.pointerCount == 1) {
            focus(event.x, event.y)//CameraUtils.handleFocusMetering(event.getX(), event.getY(), camera, getWidth(), getHeight());
        } else {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    val params = camera!!.parameters
                    distZoom = getFingerSpacing(event) - params.zoom * 10
                }
                MotionEvent.ACTION_MOVE -> {
                    val newDist = getFingerSpacing(event)
                    if (newDist > distZoom) {
                        CameraUtil.handleZoom((newDist - distZoom).toInt() / 10, camera!!)
                    }
                }
            }
        }
        return true
    }

    fun focus(x: Float, y: Float) {
        var x = x
        var y = y
        if (width == 0 || height == 0)
            return
        if (x == -1f)
            x = (width / 2).toFloat()
        if (y == -1f)
            y = (height / 2).toFloat()
        CameraUtil.handleFocusMetering(x, y, camera, width, height)
    }

    private fun init() {
        holder.setKeepScreenOn(true)//屏幕常亮
        holder.addCallback(this)
    }

    fun releaseCamera() {
        if (camera != null) {
            camera!!.setPreviewCallback(null)
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
    }

    fun loadCamera() {
        init()
        cameraInstance
        try {
            camera!!.parameters = camera!!.parameters
            camera!!.setPreviewDisplay(holder)
            CameraUtil.setParameters(camera!!, width, height)
            camera!!.startPreview()
        } catch (e: Exception) {
            Log.d(TAG, "Error setting camera preview: " + e.message)
        }

    }

    fun setFlashLight(open: Boolean): Boolean {
        return CameraUtil.setFlashLight(open, camera)
    }

    companion object {
        private const val TAG = "CameraView"

        private fun getFingerSpacing(event: MotionEvent): Float {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return Math.sqrt((x * x + y * y).toDouble()).toFloat()
        }
    }

}