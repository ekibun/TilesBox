package soko.ekibun.tilesbox.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import soko.ekibun.tilesbox.service.CornerService

class BootBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(Intent.ACTION_BOOT_COMPLETED == intent?.action && PreferenceManager.getDefaultSharedPreferences(context).getBoolean("corner_run_on_boot", false)){
            context?.let{ it.startService(Intent(it, CornerService::class.java)) }
        }
    }

}