package soko.ekibun.tilesbox.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.graphics.Bitmap
import android.os.Looper
import soko.ekibun.tilesbox.activity.QrActionActivity
import soko.ekibun.tilesbox.util.ScreenCapture
import java.lang.Thread.sleep


class QrCaptureService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private var screenCapture: ScreenCapture? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (intent.hasExtra("code") && intent.hasExtra("data")) {
            screenCapture = ScreenCapture(this, intent)
            Thread(Runnable {
                val time = System.currentTimeMillis()
                var bmp: Bitmap? = null
                while (bmp == null) {
                    try {
                        sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    //超时
                    if (System.currentTimeMillis() - time > 10000)
                        return@Runnable
                    bmp = screenCapture?.capture()
                }
                Looper.prepare()
                scanBitmap(bmp)
                finish()
                Looper.loop()
            }).start()
        }
        return Service.START_NOT_STICKY
    }

    private fun scanBitmap(bitmap: Bitmap?) {
        bitmap?.let{
            QrActionActivity.processBitmap(this, it)
        }
    }

    private fun finish() {
        screenCapture?.destroy()
        stopSelf()
    }
}
