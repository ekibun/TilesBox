package soko.ekibun.tilesbox.activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            SettingsActivity.startActivity(this)
            finish()
        }, 500)
    }
}
