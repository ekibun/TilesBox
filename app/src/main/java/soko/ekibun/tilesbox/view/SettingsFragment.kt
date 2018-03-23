package soko.ekibun.tilesbox.view

import android.content.ComponentName
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import soko.ekibun.tilesbox.R
import soko.ekibun.tilesbox.activity.SettingsActivity
import soko.ekibun.tilesbox.activity.SplashActivity
import soko.ekibun.tilesbox.util.AppUtil
import android.preference.ListPreference
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
            "permit_write_sys" -> PermissionUtil.requestWriteSystemSettings(activity)
            "permit_float_window" -> PermissionUtil.requestShowOverlay(activity)
        }
        return false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "hide_launcher") {
            val pkg = activity.packageManager
            if (sharedPreferences.getBoolean(key, false)) {
                pkg.setComponentEnabledSetting(ComponentName(activity, SplashActivity::class.java),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            } else {
                pkg.setComponentEnabledSetting(ComponentName(activity, SplashActivity::class.java),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
        }else if(key == "qr_hide_share") {
            val pkg = activity.packageManager
            if (sharedPreferences.getBoolean(key, false)) {
                pkg.setComponentEnabledSetting(ComponentName(activity, QrShareActivity::class.java),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            } else {
                pkg.setComponentEnabledSetting(ComponentName(activity, QrShareActivity::class.java),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
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

        val aboutPref = findPreference("version_code") as Preference
        aboutPref.summary = AppUtil.getVersion(activity)
    }
}
