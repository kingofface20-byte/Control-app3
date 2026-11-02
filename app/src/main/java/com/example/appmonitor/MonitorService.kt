package com.example.appmonitor

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.util.*

class MonitorService : Service() {
    private var botToken: String? = null
    private var chatId: String? = null
    private val targetPackage = "com.whatsapp"
    private var isRunning = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        botToken = intent?.getStringExtra("BOT_TOKEN")
        chatId = intent?.getStringExtra("CHAT_ID")

        Thread {
            while (isRunning) {
                checkForegroundApp()
                Thread.sleep(3000)
            }
        }.start()

        return START_STICKY
    }

    private fun checkForegroundApp() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
        
        if (stats != null) {
            val sortedMap = TreeMap<Long, android.app.usage.UsageStats>()
            for (usageStats in stats) {
                sortedMap[usageStats.lastTimeUsed] = usageStats
            }
            if (sortedMap.isNotEmpty()) {
                val currentApp = sortedMap[sortedMap.lastKey()]?.packageName
                if (currentApp == targetPackage) {
                    // دلته د واټس اپ د فعالېدو پر مهال د بوټ عملیات اجرا کېږي
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}
