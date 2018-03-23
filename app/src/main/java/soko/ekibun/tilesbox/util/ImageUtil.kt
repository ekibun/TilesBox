package soko.ekibun.tilesbox.util

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.net.Uri
import java.io.BufferedInputStream
import java.io.File
import android.graphics.Bitmap
import java.io.FileOutputStream
import android.provider.MediaStore
import android.content.ContentValues
import android.content.Intent


object ImageUtil {
    fun getBitmapFromUri(uri: Uri, context: Context): Bitmap? {
        try {
            val input = context.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(BufferedInputStream(input))
            //return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun converBitmap(bmp: Bitmap): Bitmap {
        val w = (bmp.width + 1) / 2 * 2
        val h = (bmp.height + 1) / 2 * 2
        val config = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(w, h, config)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        canvas.drawBitmap(bmp, 0f, 0f, null)
        return bitmap
    }

    fun rotate(bmp: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    }

    fun getYUV420sp(scaled: Bitmap): ByteArray {
        val inputWidth = scaled.width
        val inputHeight = scaled.height
        val argb = IntArray(inputWidth * inputHeight)
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight)
        return yuv
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var y: Int
        var u: Int
        var v: Int
        var yIndex = 0
        var uvIndex = frameSize

        var r: Int
        var g: Int
        var b: Int
        var argbIndex = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                // a is not used obviously
                //a = (argb[argbIndex] & 0xff000000) >> 24;
                r = argb[argbIndex] and 0xff0000 shr 16
                g = argb[argbIndex] and 0xff00 shr 8
                b = argb[argbIndex] and 0xff
                argbIndex++
                // well known RGB to YUV algorithm
                y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                y = Math.max(0, Math.min(y, 255))
                u = Math.max(0, Math.min(u, 255))
                v = Math.max(0, Math.min(v, 255))
                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                // meaning for every 4 Y pixels there are 1 V and 1 U. Note the sampling is every other
                // pixel AND every other scanline.
                // ---Y---
                yuv420sp[yIndex++] = y.toByte()
                // ---UV---
                if (j % 2 == 0 && i % 2 == 0) {
                    yuv420sp[uvIndex++] = v.toByte()
                    yuv420sp[uvIndex++] = u.toByte()
                }
            }
        }
    }

    fun cropImage(context: Activity, f: File, REQUEST_CROP_BITMAP: Int){
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(ImageUtil.getImageContentUri(context, f.absolutePath), "image/*")
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", true)
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
        intent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.startActivityForResult(intent, REQUEST_CROP_BITMAP)
    }

    fun imageToFile(context: Context, bitmap: Bitmap): File{
        val f = File(context.externalCacheDir, "pic.jpg")
        try {
            val out = FileOutputStream(
                    f)
            bitmap.compress(Bitmap.CompressFormat.JPEG,
                    100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return f
    }

    private fun getImageContentUri(context: Context, path: String): Uri {
        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                MediaStore.Images.Media.DATA + "=? ",
                arrayOf(path), null)
        cursor.use {
            return if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndex(MediaStore.Images.Media._ID))
                val baseUri = Uri.parse("content://media/external/images/media")
                Uri.withAppendedPath(baseUri, "" + id)
            } else {
                val contentValues = ContentValues(1)
                contentValues.put(MediaStore.Images.Media.DATA, path)
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            }
        }
    }
}