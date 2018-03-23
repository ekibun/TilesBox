package soko.ekibun.tilesbox.decoder

import android.widget.Toast
import android.graphics.Bitmap
import android.os.Bundle
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.common.HybridBinarizer
import android.content.Context
import android.graphics.Rect
import com.google.zxing.*
import soko.ekibun.tilesbox.R
import soko.ekibun.tilesbox.util.ImageUtil
import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import soko.ekibun.tilesbox.activity.QrResultActivity


object Decoder{
    const val DECODE_SUCCESS = 1
    const val DECODE_FAILED = 0
    const val BARCODE_BITMAP = "barcode_bitmap"
    const val BARCODE_RESULT = "barcode_result"
    const val BARCODE_ROTATE = "barcode_rotate"

    fun decode(data: ByteArray, width: Int, height: Int, crop: Rect): Result? {
        var result: Result? = null
        try {
            val hints = Hashtable<DecodeHintType, Any>()
            hints[DecodeHintType.CHARACTER_SET] = "ISO-8859-1"
            hints[DecodeHintType.TRY_HARDER] = java.lang.Boolean.TRUE
            hints[DecodeHintType.POSSIBLE_FORMATS] = BarcodeFormat.QR_CODE

            val source = PlanarYUVLuminanceSource(data, width, height, crop.left, crop.top, crop.width(), crop.height(), false)
            val bitmap1 = BinaryBitmap(HybridBinarizer(source))
            result = QRCodeReader().decode(bitmap1, hints)
        } catch (e: NotFoundException) {
            //e.printStackTrace();
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }

    fun scanBitmap(bmp: Bitmap?, context: Context) {
        if(bmp == null) return
        //var bitmap: Bitmap = bitmap ?: return
        try {
            val bitmap = ImageUtil.converBitmap(bmp)
            val w = bitmap.width
            val h = bitmap.height
            val data = ImageUtil.getYUV420sp(bitmap)
            val result = decode(data, w, h, Rect(0, 0, w, h))
            if (result != null) {
                val bundle = Bundle()
                //bundle.putInt(Decoder.BARCODE_ROTATE, 90);
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                bundle.putByteArray(Decoder.BARCODE_BITMAP, stream.toByteArray())
                bundle.putString(Decoder.BARCODE_RESULT, Decoder.fixString(result.text))
                QrResultActivity.handleResult(context, bundle)
            } else {
                Toast.makeText(context, context.getString(R.string.qr_toast_no_result), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun fixString(resultStr: String): String {
        var utfStr = ""
        var gbStr = ""
        var isCn = false
        try {
            utfStr = String(resultStr.toByteArray(charset("ISO-8859-1")), Charset.forName("UTF-8"))
            isCn = isChineseCharacter(utfStr)
            //防止有人特意使用乱码来生成二维码来判断的情况
            val b = isSpecialCharacter(resultStr)
            if (b) isCn = true
            if (!isCn)
                gbStr = String(resultStr.toByteArray(charset("ISO-8859-1")), Charset.forName("GB2312"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return if (isCn)
            utfStr
        else
            gbStr
    }

    private fun isChineseCharacter(chineseStr: String): Boolean {
        val charArray = chineseStr.toCharArray()
        for (i in charArray.indices)
        //是否是Unicode编码,除了"�"这个字符.这个字符要另外处理
            return if (charArray[i] in '\u0000'..'￼' || charArray[i] in '￾'..'￾')
                continue
            else
                false
        return true
    }

    private fun isSpecialCharacter(str: String): Boolean {
        //是"�"这个特殊字符的乱码情况
        return str.contains("ï¿½")
    }
}