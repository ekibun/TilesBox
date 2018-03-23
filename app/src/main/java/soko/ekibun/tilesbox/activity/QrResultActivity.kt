package soko.ekibun.tilesbox.activity

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import soko.ekibun.tilesbox.R
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.preference.PreferenceManager
import android.view.View
import kotlinx.android.synthetic.main.activity_qr_result.*
import soko.ekibun.tilesbox.decoder.Decoder
import soko.ekibun.tilesbox.util.ImageUtil


class QrResultActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_result)

        val extras = intent.extras
        if (null != extras) {
            val result = extras.getString(Decoder.BARCODE_RESULT)
            result_text.text = result

            var barcode: Bitmap? = null
            val compressedBitmap = extras.getByteArray(Decoder.BARCODE_BITMAP)
            if (compressedBitmap != null) {
                barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.size, null)
                // Mutable copy:
                val degree = extras.getInt(Decoder.BARCODE_ROTATE, 0)
                barcode = ImageUtil.rotate(barcode, degree)
            }
            if (barcode != null) {
                result_image.setImageBitmap(barcode)
            } else
                result_image.visibility = View.GONE
            result_copy.setOnClickListener{
                copy(result)
            }
            result_share.setOnClickListener{
                share(result)
            }
            result_open.setOnClickListener{
                open(result)
            }
        }
    }

    public override fun onStop() {
        super.onStop()
        this.finish()
    }

    private fun copy(result: String) {
        val cmb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QrCode", result)
        cmb.primaryClip = clip
        Toast.makeText(this, String.format(getString(R.string.qr_toast_copied), result), Toast.LENGTH_SHORT).show()
    }

    private fun share(result: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, result)
        intent.type = "text/plain"
        startActivity(Intent.createChooser(intent, ""))
    }

    private fun open(result: String) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(result)
            startActivity(Intent.createChooser(intent, ""))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        fun handleResult(context: Context, bundle: Bundle) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("qr_open_direct", false)) {
                try {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(bundle.getString(Decoder.BARCODE_RESULT))
                    context.startActivity(Intent.createChooser(intent, ""))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else
                context.startActivity(Intent(context, QrResultActivity::class.java).putExtras(bundle))
        }
    }
}
