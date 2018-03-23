package soko.ekibun.tilesbox.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.WindowManager
import android.view.Gravity
import android.graphics.PixelFormat
import android.view.View
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import soko.ekibun.tilesbox.util.OrientationUtil


class RotationLockService : Service() {
    private val mWindowManager by lazy {
        application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    private val view by lazy{
        View(applicationContext)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent?.hasExtra("rotation") == true){
            val orientation = intent.getIntExtra("rotation", 0)
            mWindowManager.updateViewLayout(view, initWindowParams(orientation))
        }
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onCreate() {
        super.onCreate()
        val curOrientation = OrientationUtil.getCurrentOrientation(this)
        mWindowManager.addView(view, initWindowParams(curOrientation))
    }

    override fun onDestroy() {
        super.onDestroy()
        mWindowManager.removeView(view)
    }

    private fun initWindowParams(curOrientation: Int): WindowManager.LayoutParams {
        val wmParams = WindowManager.LayoutParams()
        @Suppress("DEPRECATION")
        wmParams.type = if(Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        wmParams.format = PixelFormat.TRANSPARENT
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        wmParams.gravity = Gravity.TOP
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        wmParams.screenOrientation = if(OrientationUtil.isPortrait(curOrientation)) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        return wmParams
    }
}
