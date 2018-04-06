package soko.ekibun.tilesbox.service

import android.content.Intent
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import soko.ekibun.tilesbox.R
import soko.ekibun.tilesbox.util.AppUtil
import soko.ekibun.tilesbox.util.PermissionUtil

class CornerQuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()

        updateQuickSettingsTile()
    }

    override fun onClick() {
        if (PermissionUtil.checkShowOverlay(this)) {
            if(!AppUtil.isServiceRunning(this, CornerService::class.java.name))
                startService(Intent(this, CornerService::class.java))
            else
                stopService(Intent(this, CornerService::class.java))
            updateQuickSettingsTile()
        } else {
            Toast.makeText(this, R.string.toast_no_permit, Toast.LENGTH_LONG).show()
        }
    }



    private fun updateQuickSettingsTile() {
        qsTile.label = PreferenceManager.getDefaultSharedPreferences(this).getString("corner_tile_label", getString(R.string.pref_corner_cate))
        qsTile.state = when ( AppUtil.isServiceRunning(this, CornerService::class.java.name)) {
            true -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }
}
