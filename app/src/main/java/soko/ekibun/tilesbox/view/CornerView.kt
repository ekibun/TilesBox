package soko.ekibun.tilesbox.view

import android.content.Context
import android.graphics.*
import android.preference.PreferenceManager
import android.view.View

class CornerView(context: Context): View(context) {
    var cornerSize = 24f
            get() {
                val sp = PreferenceManager.getDefaultSharedPreferences(context)
                return sp.getString("corner_size", "24").toFloat()
            }
    var path: Path? = null
            get() {
                val path = Path()
                path.moveTo(0f, cornerSize)
                path.lineTo(0f, 0f)
                path.lineTo(cornerSize, 0f)
                path.arcTo(RectF(0f, 0f, 2 * cornerSize, 2 * cornerSize), -90f, -90f)
                path.close()
                path.moveTo(width - 0f, cornerSize)
                path.lineTo(width - 0f, 0f)
                path.lineTo(width - cornerSize, 0f)
                path.arcTo(RectF(width - 2 * cornerSize, 0f, width - 0f, 2 * cornerSize), -90f, 90f)
                path.close()
                path.moveTo(0f, height - cornerSize)
                path.lineTo(0f, height - 0f)
                path.lineTo(cornerSize, height - 0f)
                path.arcTo(RectF(0f, height - 2 * cornerSize, 2 * cornerSize, height - 0f), 90f, 90f)
                path.close()
                path.moveTo(width - 0f, height - cornerSize)
                path.lineTo(width - 0f, height - 0f)
                path.lineTo(width - cornerSize, height - 0f)
                path.arcTo(RectF(width - 2 * cornerSize, height - 2 * cornerSize, width - 0f, height - 0f), 90f, -90f)
                path.close()
                return path
            }
    private val paint by lazy{
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint
    }
    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        super.onDraw(canvas)
    }
}
