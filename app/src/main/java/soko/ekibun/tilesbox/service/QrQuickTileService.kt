package soko.ekibun.tilesbox.service

import android.service.quicksettings.TileService
import android.preference.PreferenceManager
import soko.ekibun.tilesbox.R
import soko.ekibun.tilesbox.activity.QrActionActivity
import soko.ekibun.tilesbox.util.AppUtil


class QrQuickTileService: TileService(){
    override fun onClick() {
        AppUtil.collapseSystemDalogs(baseContext)

        QrActionActivity.processTileClick(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateQuickSettingsTile()
    }

    private fun updateQuickSettingsTile() {
        qsTile.label = PreferenceManager.getDefaultSharedPreferences(this).getString("qr_tile_label", getString(R.string.pref_qr_cate))
        qsTile.updateTile()
    }
}