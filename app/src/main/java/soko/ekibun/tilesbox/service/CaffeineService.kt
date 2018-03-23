package soko.ekibun.tilesbox.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.preference.PreferenceManager
import android.os.PowerManager
import android.os.IBinder
import soko.ekibun.tilesbox.R
import soko.ekibun.tilesbox.util.AppUtil
import soko.ekibun.tilesbox.util.NotificationUtil


@Suppress("DEPRECATION")
class CaffeineService : Service() {
    private val mWakeLock: PowerManager.WakeLock by lazy{
        (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "keep_screen_on_tag")
    }

    private var myReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_SCREEN_OFF == intent.action) {//当按下电源键，屏幕变黑的时候
                releaseCaffeine()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(myReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startid: Int): Int {
        if (intent != null) {
            if (intent.getBooleanExtra("acquireCaffeine", false))
                acquireCaffeine()
            else if (intent.getBooleanExtra("notif_click", false))
                releaseCaffeine()
        }
        return super.onStartCommand(intent, flags, startid)
    }

    private fun acquireCaffeine() {
        mWakeLock.acquire(Long.MAX_VALUE)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        if (sp.getBoolean("caffeine_show_notif", true)) {
            val title = sp.getString("caffeine_notif_title", getString(R.string.caffeine_notif_title))
            val description = sp.getString("caffeine_notif_text", getString(R.string.caffeine_notif_text))
            val builder = NotificationUtil.builder(this, "caffeine", title, NotificationManager.IMPORTANCE_LOW)
                    .setSmallIcon(R.drawable.ic_caffeine_tile)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setContentIntent(PendingIntent.getService(this, 0, Intent(this, CaffeineService::class.java).putExtra("notif_click", true), 0))
            startForeground(233, builder.build())
        }
    }

    fun releaseCaffeine() {
        stopSelf()
    }

    override fun onDestroy() {
        mWakeLock.release()
        unregisterReceiver(myReceiver)
        stopForeground(true)
        super.onDestroy()
    }
    companion object {
        fun isServiceRunning(context: Context):Boolean {
            return AppUtil.isServiceRunning(context.applicationContext, CaffeineService::class.java.name)
        }
        fun startService(context: Context) {
            //if (isServiceRunning(context))
            //    return
            val intent = Intent(context, CaffeineService::class.java)
            intent.putExtra("acquireCaffeine", true)
            context.startService(intent)
        }
        fun stopService(context: Context) {
            val intent = Intent(context, CaffeineService::class.java)
            context.stopService(intent)
        }
    }
}