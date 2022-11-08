package com.funsol.technologies.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi


@Suppress("DEPRECATION")
class MyService : Service() {

    companion object {
        private const val TAG = "MyService"
        const val EXTRA_NOTIFICATION_REQUEST_CODE = "EXTRA_NOTIFICATION_REQUEST_CODE"
        const val EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION"

        fun showNotification(context: Context, requestCode: Int, notification: Notification): Boolean {
            val intent = Intent(context, MyService::class.java)
            intent.putExtra(EXTRA_NOTIFICATION_REQUEST_CODE, requestCode)
            intent.putExtra(EXTRA_NOTIFICATION, notification)
            return startService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MyService::class.java)
            context.stopService(intent)
        }

        private fun startService(context: Context, intent: Intent): Boolean {
            //
            // Similar to ContextCompat.startForegroundService(context, intent)
            //
            val componentName: ComponentName? = if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            return componentName != null
        }

        @RequiresApi(api = 26)
        fun createNotificationChannel(
            context: Context,
            id: String, name: String, importance: Int,
            description: String
        ) {
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            createNotificationChannel(context, channel)
        }

        @Suppress("MemberVisibilityCanBePrivate")
        @RequiresApi(api = 26)
        fun createNotificationChannel(
            context: Context,
            channel: NotificationChannel
        ) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val mBinder: IBinder = MyBinder()
    private var mHandler: Handler? = null
    var progress = 0
        private set

    var maxValue = 0
        private set

    var isPaused: Boolean? = null
        private set

    override fun onCreate() {
        Log.d(TAG, "+onCreate()")
        super.onCreate()
        Log.d(TAG, "-onCreate()")
        initValues()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Log.d(TAG, "+onStartCommand(...)")
            if (intent != null) {
                val extras = intent.extras
                if (extras != null) {
                    if (extras.containsKey(EXTRA_NOTIFICATION)) {
                        val notification = extras.getParcelable<Parcelable>(EXTRA_NOTIFICATION)
                        if (notification is Notification) {
                            if (extras.containsKey(EXTRA_NOTIFICATION_REQUEST_CODE)) {
                                val requestCode = extras.getInt(EXTRA_NOTIFICATION_REQUEST_CODE)
                                startForeground(requestCode, notification)
                            }
                        }
                    }
                }
            }
            return START_NOT_STICKY
        } finally {
            Log.d(TAG, "-onStartCommand(...)")
        }
    }


    private fun initValues() {
        mHandler = Handler(Looper.myLooper()!!)
        progress = 0
        isPaused = true
        maxValue = 5000
    }


    override fun onBind(intent: Intent): IBinder {
        try {
            Log.d(TAG, "+onBind(...)")
            return mBinder
        } finally {
            Log.d(TAG, "-onBind(...)")
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        try {
            Log.d(TAG, "+onUnbind(...)")
            return true
        } finally {
            Log.d(TAG, "-onUnbind(...)")
        }
    }


    inner class MyBinder : Binder() {
        val service: MyService get() = this@MyService
    }

    fun pausePretendLongRunningTask() {
        isPaused = true
    }

    fun unPausePretendLongRunningTask() {
        isPaused = false
        startPretendLongRunningTask()
    }

    private fun startPretendLongRunningTask() {
        val runnable: Runnable = object : Runnable {
            override fun run() {
                if (progress >= maxValue || isPaused == true) {
                    Log.d(TAG, "run: removing callbacks")
                    mHandler!!.removeCallbacks(this) // remove callbacks from runnable
                    pausePretendLongRunningTask()
                } else {
                    Log.d(TAG, "run: progress: $progress")
                    progress += 100 // increment the progress
                    mHandler!!.postDelayed(this, 100) // continue incrementing
                }
            }
        }
        mHandler!!.postDelayed(runnable, 100)
    }

    fun resetTask() {
        progress = 0
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: called.")
        stopSelf()
    }

    override fun onDestroy() {
        Log.d(TAG, "+onDestroy()")
        //PbLog.s(TAG, PbStringUtils.separateCamelCaseWords("onDestroy"));
        super.onDestroy()
        stopForeground(true)
        Log.d(TAG, "-onDestroy()")
    }
}
