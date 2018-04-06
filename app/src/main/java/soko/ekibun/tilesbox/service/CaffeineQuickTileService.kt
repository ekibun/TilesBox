package soko.ekibun.tilesbox.service

import android.service.quicksettings.TileService
import android.service.quicksettings.Tile
import android.preference.PreferenceManager
import soko.ekibun.tilesbox.R


class CaffeineQuickTileService: TileService() {
    override fun onTileAdded() {
        refreshState()
    }

    override fun onStartListening() {
        refreshState()
    }

    override// 点击
    fun onClick() {
        if (CaffeineService.isServiceRunning(this)) {
            qsTile.state = Tile.STATE_INACTIVE
            //关闭服务
            CaffeineService.stopService(applicationContext)
        } else {
            qsTile.state = Tile.STATE_ACTIVE
            //启动服务
            CaffeineService.startService(applicationContext)
        }
        qsTile.updateTile()//更新Tile
    }

    private fun refreshState() {
        qsTile.label = PreferenceManager.getDefaultSharedPreferences(this).getString("caffeine_tile_label", getString(R.string.pref_caffeine_cate))
        if (CaffeineService.isServiceRunning(this)) {
            qsTile.state = Tile.STATE_ACTIVE
        } else {
            qsTile.state = Tile.STATE_INACTIVE
        }
        qsTile.updateTile()//更新Tile
    }
}