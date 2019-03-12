package soko.ekibun.tilesbox.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.preference.PreferenceManager
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import soko.ekibun.tilesbox.R
import soko.ekibun.tilesbox.util.NotificationUtil

@Suppress("DEPRECATION")
class CaffeineService : Service() {
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
        if (Settings.System.canWrite(applicationContext)) {
            if (intent != null) {
                if (intent.getBooleanExtra("acquireCaffeine", false))
                    acquireCaffeine()
                else if (intent.getBooleanExtra("notifyClick", false))
                    releaseCaffeine()
            }
        } else {
            Toast.makeText(this, R.string.toast_no_permit, Toast.LENGTH_LONG).show()
        }
        return super.onStartCommand(intent, flags, startid)
    }

    private fun acquireCaffeine() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)

        val lastTime = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        if(lastTime != Int.MAX_VALUE){
            sp.edit().putString("caffeine_timeout", (lastTime / 1000).toString()).apply()
        }
        setTimeOut(Int.MAX_VALUE)

        if (sp.getBoolean("caffeine_show_notif", true)) {
            val title = sp.getString("caffeine_notif_title", getString(R.string.caffeine_notif_title))!!
            val description = sp.getString("caffeine_notif_text", getString(R.string.caffeine_notif_text))
            val builder = NotificationUtil.builder(this, "caffeine", title, NotificationManager.IMPORTANCE_LOW)
                    .setSmallIcon(R.drawable.ic_caffeine_tile)
                    .setContentTitle(title)
                    .setContentText(description)
                    .setContentIntent(PendingIntent.getService(this, 0, Intent(this, CaffeineService::class.java)
                            .putExtra("notifyClick", true), 0))
            startForeground(233, builder.build())
        }
    }

    private fun setTimeOut(value: Int){
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, value)
        startService(Intent(this, CaffeineQuickTileService::class.java))
    }

    fun releaseCaffeine() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        setTimeOut(sp.getString("caffeine_timeout", "30")!!.toInt() * 1000)
        stopSelf()
    }

    override fun onDestroy() {
        releaseCaffeine()
        unregisterReceiver(myReceiver)
        stopForeground(true)
        super.onDestroy()
    }
    companion object {
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