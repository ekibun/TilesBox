package soko.ekibun.tilesbox.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

object PermissionUtil{
    fun checkWriteSystemSettings(context: Context): Boolean{
        return Settings.System.canWrite(context.applicationContext)
    }

    fun requestWriteSystemSettings(context: Context){
        context.startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + context.packageName)))
    }

    fun checkShowOverlay(context: Context): Boolean{
        return try {
            val clazz = Settings::class.java
            val canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
            canDrawOverlays.invoke(null, context) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    fun requestShowOverlay(context: Context){
        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName)))
    }
}