@file:Suppress("DEPRECATION")

package soko.ekibun.tilesbox.activity

import android.Manifest
import android.app.Activity
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import soko.ekibun.tilesbox.R
import android.content.Context
import android.hardware.SensorManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import kotlinx.android.synthetic.main.activity_qr_camera.*
import soko.ekibun.tilesbox.decoder.Decoder
import android.content.res.ColorStateList
import android.view.View
import android.widget.LinearLayout
import soko.ekibun.tilesbox.decoder.DecodeThread
import soko.ekibun.tilesbox.util.AppUtil

class QrCameraActivity : Activity(), Camera.PreviewCallback, SensorEventListener {
    private val mSensorManager by lazy{
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val mAccel by lazy{
        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun initCrop(size: Camera.Size) {
        val cameraWidth = size.height
        val cameraHeight = size.width

        val location = IntArray(2)
        val loc = Rect()
        scan_frame.getGlobalVisibleRect(loc)
        scan_frame.getLocationInWindow(location)
        val containerWidth = camera_view.width
        val containerHeight = camera_view.height

        val x = loc.left * cameraWidth / containerWidth
        val y = loc.top * cameraHeight / containerHeight
        val width = loc.width() * cameraWidth / containerWidth
        val height = loc.height() * cameraHeight / containerHeight

        crop = Rect(y, x, height + y, width + x)
    }

    private fun requestFrame() {
        camera_view.camera?.setOneShotPreviewCallback(this)
    }
    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val cameraResolution = camera.parameters.previewSize
        if (crop.width() * crop.height() == 0)
            initCrop(cameraResolution)
        if (camera_view.camera != null && crop.width() * crop.height() != 0) {
            DecodeThread(data, cameraResolution.width, cameraResolution.height, crop){ i: Int, bundle: Bundle ->
                when (i) {
                    Decoder.DECODE_SUCCESS -> if (requireResult)
                        handleResult(bundle.getString(Decoder.BARCODE_RESULT, ""))
                    else
                        QrResultActivity.handleResult(this, bundle)
                    Decoder.DECODE_FAILED -> requestFrame()
                }
            }.start()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private var mLastX = 0f
    private var mLastY = 0f
    private var mLastZ = 0f
    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val deltaX = Math.abs(mLastX - x)
        val deltaY = Math.abs(mLastY - y)
        val deltaZ = Math.abs(mLastZ - z)

        if (deltaX > .5 || deltaY > .5 || deltaZ > .5) { //AUTOFOCUS (while it is not autofocusing) */
            camera_view.focus(-1f, -1f)//mAutoFocus = false;
            //mPreview.setCameraFocus(myAutoFocusCallback);
        }

        mLastX = x
        mLastY = y
        mLastZ = z
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_camera)

        if("com.google.zxing.client.android.SCAN" == intent.action)
            requireResult = true
        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            this.requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCameraPermission)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        initView()
    }

    private var crop = Rect()
    private var requireResult = false
    private val requestCameraPermission = 1

    private fun handleResult(result: String) {
        val intent = Intent(intent.action)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        intent.putExtra("SCAN_RESULT", result)
        intent.putExtra("SCAN_RESULT_FORMAT", "utf-8")
        setResult(Activity.RESULT_OK, intent)
        this.finish()
    }

    private var flashlight = false
    private fun initFab() {
        floatingActionButton.backgroundTintList = ColorStateList.valueOf(getColor(
                if (flashlight) R.color.colorPrimary else android.R.color.black))
    }

    private fun initView() {
        floatingActionButton.setOnClickListener {
            flashlight = !flashlight
            camera_view.setFlashLight(flashlight)
            it.backgroundTintList = ColorStateList.valueOf(getColor(
                    if (flashlight) R.color.colorPrimary else android.R.color.black))
        }

        val statusHeight = AppUtil.getStatusBarHeight(this)

        var llp = run_wechat.layoutParams as LinearLayout.LayoutParams
        llp.topMargin = -statusHeight
        run_wechat.layoutParams = llp
        run_wechat.setOnClickListener {
            if (QrActionActivity.openWechat(this))
                finish()
        }
        llp = run_alipay.layoutParams as LinearLayout.LayoutParams
        llp.topMargin = -statusHeight
        run_alipay.layoutParams = llp
        run_alipay.setOnClickListener {
            if (QrActionActivity.openAlipay(this))
                finish()
        }
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        var lp = scan_frame.layoutParams
        lp.width = rect.width() * 5 / 8
        lp.height = lp.width
        scan_frame.layoutParams = lp
        lp = mask_status.layoutParams
        lp.height = statusHeight
        mask_status.layoutParams = lp

        gallery.setOnClickListener {
            QrActionActivity.openGallery(this)
            finish()
        }
        settings.setOnClickListener {
            QrActionActivity.openSettings(this)
            finish()
        }
    }


    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI)
        camera_view.loadCamera()
        flashlight = false
        initFab()
        requestFrame()
    }

    override fun onPause() {
        super.onPause()
        camera_view.releaseCamera()              // release the camera immediately on pause event
    }

    public override fun onStop() {
        super.onStop()
        this.finish()
    }
}
