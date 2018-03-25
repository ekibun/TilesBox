package soko.ekibun.tilesbox.activity

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import android.app.AlertDialog
import android.content.Context
import soko.ekibun.tilesbox.R
import android.content.Intent
import android.widget.Toast
import android.media.projection.MediaProjectionManager
import android.content.ComponentName
import android.net.Uri
import android.graphics.Bitmap
import soko.ekibun.tilesbox.decoder.Decoder
import soko.ekibun.tilesbox.service.QrCaptureService
import soko.ekibun.tilesbox.service.RotationLockService
import soko.ekibun.tilesbox.util.AppUtil
import soko.ekibun.tilesbox.util.ImageUtil
import java.io.File
import soko.ekibun.tilesbox.util.PermissionUtil


class QrActionActivity : Activity() {
    private val requestCaptureCode = 2
    private val requestPhotoGallery = 3
    private val requestCropBitmap = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        if (intent.hasExtra("action")) {
            runAction(intent.getIntExtra("action", 0))
        }
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        //单击
        if (intent.hasExtra("click")) {
            runAction(Integer.parseInt(sp.getString("qr_action_click", "0")!!))
        }
        //长按
        if ("android.service.quicksettings.action.QS_TILE_PREFERENCES" == intent.action) {
            when {
                intent.extras.toString().contains("QrQuickTileService") -> runAction(Integer.parseInt(sp.getString("qr_action_longclick", "1")!!))
                intent.extras.toString().contains("RotationQuickTileService") && PermissionUtil.checkShowOverlay(this) -> {
                    if(!AppUtil.isServiceRunning(this, RotationLockService::class.java.name))
                        startService(Intent(this, RotationLockService::class.java))
                    else
                        stopService(Intent(this, RotationLockService::class.java))
                    this.finish()
                }
                else -> {
                    openSettings(this)
                    this.finish()
                }
            }
        }
        if (intent.hasExtra("bitmap")) {
            File(intent.getStringExtra("bitmap")).let{
                ImageUtil.cropImage(this, it, requestCropBitmap)
            }
        }
    }

    private var flagResult = false
    private fun runAction(action: Int) {
        when (action) {
            0 -> {
                openCamera()
                this.finish()
            }
            1 -> {
                flagResult = true
                openScreenShot()
            }
            2 -> {
                openWechat(this)
                this.finish()
            }
            3 -> {
                openAlipay(this)
                this.finish()
            }
            6 -> {
                openSettings(this)
                this.finish()
            }
            4 -> {
                openAliPayment()
                this.finish()
            }
            5 -> {
                flagResult = true
                openGallery()
            }
            7 -> openMenu()
        }
    }

    private fun openScreenShot() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(permissionIntent, requestCaptureCode)
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK)//ACTION_OPEN_DOCUMENT
        gallery.type = "image/*"
        startActivityForResult(gallery, requestPhotoGallery)
    }

    private fun openCamera(): Boolean {
        val intent = Intent(this, QrCameraActivity::class.java)
        this.startActivity(intent)
        return true
    }

    private fun openAliPayment(): Boolean {
        try {
            val uri = Uri.parse("alipayqr://platformapi/startapp?saId=20000056")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            return true
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.qr_toast_no_alipay), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

        return false
    }

    private fun openMenu() {
        val al = resources.getStringArray(R.array.qr_pref_action).toMutableList()
        al.removeAt(al.size - 1)

        val builder = AlertDialog.Builder(this)
        builder.setItems(al.toTypedArray())
        { _, which -> runAction(which) }
        builder.setOnDismissListener { if(!flagResult) this.finish() }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCaptureCode) {
            if (resultCode == Activity.RESULT_OK) {
                // 获得权限，启动Service开始录制
                val service = Intent(this, QrCaptureService::class.java)
                service.putExtra("code", resultCode)
                service.putExtra("data", data)
                startService(service)
            } else {
                Toast.makeText(this, R.string.qr_toast_no_permit, Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == requestPhotoGallery && data != null) {
            ImageUtil.getBitmapFromUri(data.data, this)?.let{
                processBitmap(this, it)
                //ImageUtil.cropImage(this, ImageUtil.imageToFile(this, it), requestCropBitmap)
            }
            //return
        } else if(requestCode == requestCropBitmap && data != null){
            Decoder.scanBitmap(ImageUtil.getBitmapFromUri(data.data, this), this)
        }
        this.finish()
    }

    companion object {
        fun processBitmap(context: Context, bitmap: Bitmap){
            val intent = Intent(context, QrActionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("bitmap", ImageUtil.imageToFile(context, bitmap).absolutePath)
            context.startActivity(intent)
        }

        fun processTileClick(context: Context){
            val intent = Intent(context, QrActionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("click", true)
            context.startActivity(intent)
        }

        fun openGallery(context: Context){
            val intent = Intent(context, QrActionActivity::class.java)
            intent.putExtra("action", 5)
            context.startActivity(intent)
        }


        fun openSettings(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }

        fun openWechat(context: Context): Boolean {
            try {
                val intent = Intent()
                intent.component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK//335544320
                intent.action = "android.intent.action.VIEW"
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.qr_toast_no_wechat), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
            return false
        }

        fun openAlipay(context: Context): Boolean {
            try {
                val uri = Uri.parse("alipayqr://platformapi/startapp?saId=10000007")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.qr_toast_no_alipay), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }

            return false
        }
    }
}
