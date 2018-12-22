package soko.ekibun.tilesbox.view

import android.content.ComponentName
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.*
import android.util.Log
import soko.ekibun.tilesbox.R
import soko.ekibun.tilesbox.activity.SettingsActivity
import soko.ekibun.tilesbox.activity.SplashActivity
import soko.ekibun.tilesbox.util.AppUtil
import soko.ekibun.tilesbox.activity.QrShareActivity
import soko.ekibun.tilesbox.util.PermissionUtil


class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        // 加载xml资源文件
        addPreferencesFromResource(R.xml.preferences)
        refreshSummary()
    }



    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
        when(preference.key) {
            "version_code"  -> (activity as SettingsActivity).showInfo()
            "caffeine_permit_write_system",
            "rotation_permit_write_system" -> PermissionUtil.requestWriteSystemSettings(activity)
            "corner_permit_float_window",
            "rotation_permit_float_window" -> PermissionUtil.requestShowOverlay(activity)
        }
        refreshSummary()
        return false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when(key){
            "hide_launcher" ->{
                val pkg = activity.packageManager
                if (sharedPreferences.getBoolean(key, false)) {
                    pkg.setComponentEnabledSetting(ComponentName(activity, SplashActivity::class.java),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                } else {
                    pkg.setComponentEnabledSetting(ComponentName(activity, SplashActivity::class.java),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
                }
            }
            "qr_hide_share" ->{
                val pkg = activity.packageManager
                if (sharedPreferences.getBoolean(key, false)) {
                    pkg.setComponentEnabledSetting(ComponentName(activity, QrShareActivity::class.java),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
                } else {
                    pkg.setComponentEnabledSetting(ComponentName(activity, QrShareActivity::class.java),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
                }
            }
        }
        refreshSummary()
    }

    override fun onResume() {
        super.onResume()

        refreshSummary()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    private fun refreshSummary() {
        var textPref = findPreference("caffeine_tile_label") as EditTextPreference
        textPref.summary = textPref.text
        textPref = findPreference("caffeine_notif_title") as EditTextPreference
        textPref.summary = textPref.text
        textPref = findPreference("caffeine_notif_text") as EditTextPreference
        textPref.summary = textPref.text

        textPref = findPreference("qr_tile_label") as EditTextPreference
        textPref.summary = textPref.text
        var listPref = findPreference("qr_action_click") as ListPreference
        listPref.summary = listPref.entry
        listPref = findPreference("qr_action_longclick") as ListPreference
        listPref.summary = listPref.entry

        textPref = findPreference("corner_tile_label") as EditTextPreference
        textPref.summary = textPref.text
        textPref = findPreference("corner_size") as EditTextPreference
        textPref.summary = textPref.text

        textPref = findPreference("caffeine_timeout") as EditTextPreference
        textPref.summary = textPref.text

        var permitPref = findPreference("rotation_permit_float_window") as SwitchPreference
        permitPref.isChecked = PermissionUtil.checkShowOverlay(context)
        permitPref = findPreference("rotation_permit_write_system") as SwitchPreference
        permitPref.isChecked = PermissionUtil.checkWriteSystemSettings(context)
        permitPref = findPreference("caffeine_permit_write_system") as SwitchPreference
        permitPref.isChecked = PermissionUtil.checkWriteSystemSettings(context)
        permitPref = findPreference("corner_permit_float_window") as SwitchPreference
        permitPref.isChecked = PermissionUtil.checkShowOverlay(context)

        val aboutPref = findPreference("version_code") as Preference
        aboutPref.summary = AppUtil.getVersion(activity)
    }
}
