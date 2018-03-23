@file:Suppress("DEPRECATION")

package soko.ekibun.tilesbox.util

import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.hardware.Camera
import java.io.ByteArrayOutputStream
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator



object CameraUtil {
    fun getBitmap(data: ByteArray, width: Int, height: Int, crop: Rect): ByteArray {
        val localYuvImage = YuvImage(data, 17, width, height, null)
        val localByteArrayOutputStream = ByteArrayOutputStream()
        localYuvImage.compressToJpeg(crop, 50, localByteArrayOutputStream)
        return localByteArrayOutputStream.toByteArray()
    }

    fun setFlashLight(open: Boolean, camera: Camera?): Boolean {
        if (camera == null) {
            return false
        }
        val parameters = camera.parameters ?: return false
        val flashModes = parameters.supportedFlashModes
        // Check if camera flash exists
        if (null == flashModes || 0 == flashModes.size) {
            // Use the screen as a flashlight (next best thing)
            return false
        }
        val flashMode = parameters.flashMode
        if (open) {
            if (Camera.Parameters.FLASH_MODE_TORCH == flashMode) {
                return true
            }
            // Turn on the flash
            return if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                camera.parameters = parameters
                true
            } else {
                false
            }
        } else {
            if (Camera.Parameters.FLASH_MODE_OFF == flashMode) {
                return true
            }
            // Turn on the flash
            return if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
                camera.parameters = parameters
                true
            } else {
                false
            }
        }
    }

    fun handleFocusMetering(x: Float, y: Float, camera: Camera?, width: Int, height: Int) {
        if (camera == null)
            return
        val focusRect = calculateTapArea(x, y, 1f, width, height)
        val meteringRect = calculateTapArea(x, y, 1.5f, width, height)

        camera.cancelAutoFocus()
        val params = camera.parameters
        if (params.maxNumFocusAreas > 0) {
            val focusAreas = ArrayList<Camera.Area>()
            focusAreas.add(Camera.Area(focusRect, 800))
            params.focusAreas = focusAreas
        }
        if (params.maxNumMeteringAreas > 0) {
            val meteringAreas = ArrayList<Camera.Area>()
            meteringAreas.add(Camera.Area(meteringRect, 800))
            params.meteringAreas = meteringAreas
        }
        val currentFocusMode = params.focusMode
        params.focusMode = Camera.Parameters.FOCUS_MODE_MACRO
        camera.parameters = params

        camera.autoFocus(Camera.AutoFocusCallback { _, it ->
            it.parameters.focusMode = currentFocusMode
            it.parameters = params
        })
    }

    fun handleZoom(zoom: Int, camera: Camera) {
        val params = camera.parameters
        if (params.isZoomSupported) {
            val maxZoom = params.maxZoom
            params.zoom = Math.max(0, Math.min(zoom, maxZoom))
            camera.parameters = params
        }
    }

    private fun calculateTapArea(x: Float, y: Float, coefficient: Float, width: Int, height: Int): Rect {
        val focusAreaSize = 300f
        val areaSize = java.lang.Float.valueOf(focusAreaSize * coefficient).toInt()
        val centerY = -(x / width * 2000 - 1000).toInt()
        val centerX = (y / height * 2000 - 1000).toInt()

        val halfAreaSize = areaSize / 2
        val rectF = RectF(clamp(centerX - halfAreaSize, -1000, 1000).toFloat(), clamp(centerY - halfAreaSize, -1000, 1000).toFloat(), clamp(centerX + halfAreaSize, -1000, 1000).toFloat(), clamp(centerY + halfAreaSize, -1000, 1000).toFloat())
        return Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom))
    }

    private fun clamp(x: Int, min: Int, max: Int): Int {
        return Math.max(min, Math.min(x, max))
    }

    fun setParameters(camera: Camera, width: Int, height: Int) {
        val parameters = camera.parameters
        val mCameraResolution = findCloselySize(width, height, parameters.supportedPreviewSizes)
        val mPictureResolution = findCloselySize(width, height, parameters.supportedPictureSizes)
        parameters.setPreviewSize(mCameraResolution.width, mCameraResolution.height)
        parameters.setPictureSize(mPictureResolution.width, mPictureResolution.height)
        camera.setDisplayOrientation(90)
        camera.parameters = parameters
    }

    private fun findCloselySize(surfaceWidth: Int, surfaceHeight: Int, preSizeList: List<Camera.Size>): Camera.Size {
        Collections.sort(preSizeList, SizeComparator(surfaceWidth, surfaceHeight))
        return preSizeList[0]
    }

    private class SizeComparator internal constructor(width: Int, height: Int) : Comparator<Camera.Size> {

        private val width: Int
        private val height: Int
        private val ratio: Float

        init {
            if (width < height) {
                this.width = height
                this.height = width
            } else {
                this.width = width
                this.height = height
            }
            this.ratio = this.height.toFloat() / this.width
        }

        override fun compare(size1: Camera.Size, size2: Camera.Size): Int {
            val width1 = size1.width
            val height1 = size1.height
            val width2 = size2.width
            val height2 = size2.height

            val ratio1 = Math.abs(height1.toFloat() / width1 - ratio)
            val ratio2 = Math.abs(height2.toFloat() / width2 - ratio)
            val result = java.lang.Float.compare(ratio1, ratio2)
            return if (result != 0) {
                result
            } else {
                val minGap1 = Math.abs(width - width1) + Math.abs(height - height1)
                val minGap2 = Math.abs(width - width2) + Math.abs(height - height2)
                minGap1 - minGap2
            }
        }
    }
}