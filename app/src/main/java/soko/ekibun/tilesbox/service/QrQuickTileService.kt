package soko.ekibun.tilesbox.service

import android.service.quicksettings.TileService
import android.content.Intent
import soko.ekibun.tilesbox.activity.QrActionActivity
import soko.ekibun.tilesbox.util.AppUtil


class QrQuickTileService: TileService(){
    override fun onClick() {
        AppUtil.collapseSystemDalogs(baseContext)

        QrActionActivity.processTileClick(this)
    }
}