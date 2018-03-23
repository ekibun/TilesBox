package soko.ekibun.tilesbox.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import soko.ekibun.tilesbox.R

import java.net.URISyntaxException

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun showInfo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.about_dialog_title))
        builder.setMessage(getString(R.string.about_dialog_message))
        builder.setNeutralButton(R.string.about_dialog_github) { _, _ ->
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://github.com/acaoairy/caffeine")
            startActivity(Intent.createChooser(intent, null))
        }
        builder.setNegativeButton(R.string.about_dialog_support) { _, _ ->
            val intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                    "qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX04432XWNQIFV2UDCR64#Intent;" +
                    "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"
            try {
                val intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME)
                startActivity(intent)
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }
        builder.setPositiveButton(R.string.about_dialog_button, null)
        builder.show()
    }

    companion object{
        fun startActivity(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }
}