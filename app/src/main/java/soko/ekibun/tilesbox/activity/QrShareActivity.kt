package soko.ekibun.tilesbox.activity

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import soko.ekibun.tilesbox.decoder.Decoder
import soko.ekibun.tilesbox.util.ImageUtil

class QrShareActivity : Activity() {
    private val requestCropBitmap = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val uri: Uri? =
                when {
                    Intent.ACTION_SEND == intent.action -> intent.getParcelableExtra(Intent.EXTRA_STREAM)
                    Intent.ACTION_VIEW == intent.action -> intent.data
                    else -> null
                }
        if (uri != null){
            ImageUtil.getBitmapFromUri(uri, this)?.let {
                QrActionActivity.processBitmap(this, it)
            }
        }
        this.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == requestCropBitmap && data != null){
            Decoder.scanBitmap(ImageUtil.getBitmapFromUri(data.data, this), this)
        }
        this.finish()
    }

    public override fun onStop() {
        super.onStop()
        this.finish()
    }
}
