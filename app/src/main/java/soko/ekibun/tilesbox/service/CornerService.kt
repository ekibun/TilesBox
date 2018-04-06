package soko.ekibun.tilesbox.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import soko.ekibun.tilesbox.view.CornerView

class CornerService : Service() {
    private val mWindowManager by lazy {
        application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val view by lazy{
        val view = CornerView(applicationContext)
        view
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mWindowManager.addView(view, initWindowParams())
    }

    override fun onDestroy() {
        super.onDestroy()
        mWindowManager.removeView(view)
    }

    private fun initWindowParams(): WindowManager.LayoutParams {
        val wmParams = WindowManager.LayoutParams()
        @Suppress("DEPRECATION")
        wmParams.type = if(Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        wmParams.format = PixelFormat.TRANSPARENT
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        wmParams.gravity = Gravity.TOP
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT
        return wmParams
    }
}
