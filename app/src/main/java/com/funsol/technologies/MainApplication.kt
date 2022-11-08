package com.funsol.technologies

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.funsol.technologie.R
import com.funsol.technologies.activities.MainActivity
import com.funsol.technologies.services.MainService


class App : Application() {

    companion object {
        private const val TAG = "MainApplication"
        private const val NOTIFICATION_REQUEST_CODE = 100
        private const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
        private var instance: App? = null
        fun getContext(): App = instance!!
    }

    private lateinit var notification: Notification
    var isNotificationShowing: Boolean = false
        private set


    override fun onCreate() {
        Log.d(TAG, "+onCreate()")
        super.onCreate()
        Log.d(TAG, "-onCreate()")
        instance = this@App
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val appName = getString(R.string.app_name)
            val channelName = "$appName channel name"
            val channelImportance = NotificationManager.IMPORTANCE_LOW
            val channelDescription = "$appName channel description"

            MainService.createNotificationChannel(
                this,
                NOTIFICATION_CHANNEL_ID,
                channelName,
                channelImportance,
                channelDescription
            )
        }
        notification = createOngoingNotification(R.drawable.ic_notification)
    }


    private fun createOngoingNotification(icon: Int): Notification {

        val contentIntent = Intent(getContext(), MainActivity::class.java)
            .setAction(Intent.ACTION_MAIN)
            .addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            )

        val contentPendingIntent = PendingIntent.getActivity(
            getContext(),
            NOTIFICATION_REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )

        return NotificationCompat.Builder(getContext(), NOTIFICATION_CHANNEL_ID)
            .setOngoing(true)
            .setSmallIcon(icon)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Content Text").setContentIntent(contentPendingIntent).build()
    }

    fun showNotification(show: Boolean) {
        if (show) {
            MainService.showNotification(this, NOTIFICATION_REQUEST_CODE, notification)
            isNotificationShowing = true
        } else {
            isNotificationShowing = false
            MainService.stop(this)
        }
    }
}