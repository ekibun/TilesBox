package soko.ekibun.tilesbox.service

import android.content.Intent
import android.service.quicksettings.TileService
import android.service.quicksettings.Tile
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import soko.ekibun.tilesbox.R


class CaffeineQuickTileService: TileService() {
    override fun onTileAdded() {
        refreshState()
    }

    override fun onStartListening() {
        refreshState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refreshState(false)
        return super.onStartCommand(intent, flags, startId)
    }

    override// 点击
    fun onClick() {
        if (Settings.System.canWrite(applicationContext)) {
            val lastTime = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            if (lastTime == Int.MAX_VALUE) {
                qsTile.state = Tile.STATE_INACTIVE
                //关闭服务
                CaffeineService.stopService(applicationContext)
            } else {
                qsTile.state = Tile.STATE_ACTIVE
                //启动服务
                CaffeineService.startService(applicationContext)
            }
        } else {
            qsTile.state = Tile.STATE_UNAVAILABLE
            Toast.makeText(this, R.string.toast_no_permit, Toast.LENGTH_LONG).show()
        }
        qsTile.updateTile()//更新Tile
    }

    private fun refreshState(setService: Boolean = true) {
        qsTile.label = PreferenceManager.getDefaultSharedPreferences(this).getString("caffeine_tile_label", getString(R.string.pref_caffeine_cate))
        if (Settings.System.canWrite(applicationContext)) {
            val lastTime = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
            if (lastTime == Int.MAX_VALUE) {
                if(setService) CaffeineService.startService(applicationContext)
                qsTile.state = Tile.STATE_ACTIVE
            } else {
                if(setService) CaffeineService.stopService(applicationContext)
                qsTile.state = Tile.STATE_INACTIVE
            }
        } else {
            qsTile.state = Tile.STATE_UNAVAILABLE
        }
        Log.v("status", qsTile.state.toString())
        qsTile.updateTile()//更新Tile
    }
}