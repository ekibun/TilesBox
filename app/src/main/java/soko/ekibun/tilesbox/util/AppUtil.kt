package soko.ekibun.tilesbox.util

import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo
import android.content.Context
import android.app.ActivityManager
import android.content.Intent

object AppUtil {
    fun getVersion(context: Context): String {
        var versionName = ""
        var versionCode = 0
        var isApkInDebug = false
        try {
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
            versionCode = pi.versionCode
            val info = context.applicationInfo
            isApkInDebug = info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return versionName + "-" + (if (isApkInDebug) "debug" else "release") + "(" + versionCode + ")"
    }

    fun collapseSystemDalogs(context: Context){
        context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
    }

    fun isServiceRunning(context: Context, service: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val serviceList = activityManager.getRunningServices(Integer.MAX_VALUE)
        if (serviceList.size <= 0) {
            return false
        }
        return serviceList.indices
                .map { serviceList[it].service }
                .any { it.className == service }
    }

    fun getStatusBarHeight(context: Context): Int {
        var sbar = 0
        try {
            val c = Class.forName("com.android.internal.R\$dimen")
            val obj = c!!.newInstance()
            val field = c.getField("status_bar_height")
            val x = Integer.parseInt(field!!.get(obj).toString())
            sbar = context.resources.getDimensionPixelSize(x)
        } catch (e1: Exception) {
            e1.printStackTrace()
        }

        return sbar
    }
}