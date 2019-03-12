package soko.ekibun.tilesbox.util


import android.content.Context
import android.provider.Settings
import android.view.Surface
import android.view.WindowManager


object OrientationUtil {
    private const val ROTATION_PORT = Surface.ROTATION_0
    private const val ROTATION_PORT_REVERSE = Surface.ROTATION_180
    private const val ROTATION_LAND = Surface.ROTATION_90
    private const val ROTATION_LAND_REVERSE = Surface.ROTATION_270
    const val ROTATION_AUTO = -1

    fun getOppositeOrientation(orientation: Int, reverse: Boolean): Int {
        return when {
            isPortrait(orientation) -> if(reverse) ROTATION_LAND_REVERSE else ROTATION_LAND
            isLandscape(orientation) -> ROTATION_PORT
            else -> ROTATION_AUTO
        }
    }

    fun getCurrentOrientation(context: Context): Int {
        val hasAccelerometer = Settings.System
                .getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1

        return if (hasAccelerometer) {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
        } else {
            Settings.System.getInt(context.contentResolver, Settings.System.USER_ROTATION)
        }
    }

    fun isPortrait(orientation: Int): Boolean {
        return orientation == ROTATION_PORT || orientation == ROTATION_PORT_REVERSE
    }

    fun isLandscape(orientation: Int): Boolean {
        return orientation == ROTATION_LAND || orientation == ROTATION_LAND_REVERSE
    }
}