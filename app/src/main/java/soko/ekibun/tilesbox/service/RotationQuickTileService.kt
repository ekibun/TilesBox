package soko.ekibun.tilesbox.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.preference.PreferenceManager
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import soko.ekibun.tilesbox.R
import soko.ekibun.tilesbox.util.AppUtil
import soko.ekibun.tilesbox.util.OrientationUtil
import soko.ekibun.tilesbox.util.PermissionUtil

class RotationQuickTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()

        updateTile()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateTile()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onClick() {
        if (Settings.System.canWrite(applicationContext)) {
            try {
                val newOrientation = toggleOrientation()
                updateQuickSettingsTile(newOrientation)
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, R.string.toast_no_permit, Toast.LENGTH_LONG).show()
        }
    }

    private fun toggleOrientation(): Int {
        val oldOrientation = OrientationUtil.getCurrentOrientation(this)
        val newOrientation = OrientationUtil.getOppositeOrientation(oldOrientation, PreferenceManager.getDefaultSharedPreferences(this).getBoolean("rotation_reverse_landscape", false))

        Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0)
        Settings.System.putInt(contentResolver, Settings.System.USER_ROTATION, newOrientation)
        if(AppUtil.isServiceRunning(this, RotationLockService::class.java.name)){
            val intent = Intent(this, RotationLockService::class.java)
            intent.putExtra("rotation", newOrientation)
            startService(intent)
        }
        return newOrientation
    }

    private fun updateTile() {
        try {
            updateQuickSettingsTile(OrientationUtil.getCurrentOrientation(this))
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun updateQuickSettingsTile(orientation: Int) {
        qsTile.state = when (PermissionUtil.checkWriteSystemSettings(this)) {
            true -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }

        val lock = AppUtil.isServiceRunning(this, RotationLockService::class.java.name)

        when {
            OrientationUtil.isLandscape(orientation) -> {
                qsTile.icon = Icon.createWithResource(applicationContext, if(lock) R.drawable.ic_screen_lock_landscape else R.drawable.ic_screen_landscape)
                qsTile.label = getString(R.string.rotation_landscape)
            }
            OrientationUtil.isPortrait(orientation) -> {
                qsTile.icon = Icon.createWithResource(applicationContext, if(lock) R.drawable.ic_screen_lock_portrait else R.drawable.ic_screen_portrait)
                qsTile.label = getString(R.string.rotation_portrait)
            }
            else -> {
                qsTile.icon = Icon.createWithResource(applicationContext, R.drawable.ic_screen_rotation)
                qsTile.label = getString(R.string.rotation_auto)
            }
        }

        qsTile.updateTile()
    }
}
