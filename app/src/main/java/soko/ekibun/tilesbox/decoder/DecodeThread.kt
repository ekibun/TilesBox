package soko.ekibun.tilesbox.decoder

import android.graphics.Rect
import android.os.Bundle
import soko.ekibun.tilesbox.util.CameraUtil


class DecodeThread(private val data: ByteArray, private val width: Int, private val height: Int, private val crop: Rect, private val callback: (Int, Bundle)->Unit) : Thread() {

    override fun run() {
        val result = Decoder.decode(data, width, height, crop)
        if (result != null) {
            val bundle = Bundle()
            bundle.putInt(Decoder.BARCODE_ROTATE, 90)
            bundle.putByteArray(Decoder.BARCODE_BITMAP, CameraUtil.getBitmap(data, width, height, crop))
            bundle.putString(Decoder.BARCODE_RESULT, Decoder.fixString(result.text))
            callback(Decoder.DECODE_SUCCESS, bundle)
        } else {
            callback(Decoder.DECODE_FAILED, Bundle())
        }
    }
}