package soko.ekibun.tilesbox.util

import android.content.Context
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjectionManager
import android.view.WindowManager
import android.content.Intent
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection

class ScreenCapture(context: Context, intent: Intent) {
    private val mResultCode: Int by lazy{
        intent.getIntExtra("code", -1)
    }
    private val mResultData: Intent by lazy{
        intent.getParcelableExtra("data") as Intent
    }
    private val wm: WindowManager by lazy{
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private var screenDensity: Int = 0
    private var windowWidth: Int = 0
    private var windowHeight: Int = 0

    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mImageReader: ImageReader? = null

    init {
        checkOrientation()
        val maxDist = Math.max(windowWidth, windowHeight)
        mImageReader = ImageReader.newInstance(maxDist, maxDist, 0x1, 2)

        mMediaProjection = (context.applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).getMediaProjection(mResultCode, mResultData)
        mVirtualDisplay = mMediaProjection!!.createVirtualDisplay("capture_screen", windowWidth, windowHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader!!.surface, null, null)
    }

    private fun checkOrientation() {
        val metric = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(metric)

        windowWidth = metric.widthPixels
        windowHeight = metric.heightPixels
        screenDensity = metric.densityDpi

        if (mVirtualDisplay != null)
            mVirtualDisplay!!.resize(windowWidth, windowHeight, screenDensity)
    }

    fun destroy() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay!!.release()
            mVirtualDisplay = null
        }
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    fun capture(): Bitmap? {
        if (mImageReader == null)
            return null
        val image = mImageReader!!.acquireLatestImage() ?: return null

        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        var bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, windowWidth, windowHeight).copy(Bitmap.Config.RGB_565, true)
        image.close()
        return bitmap
    }
}