package com.funsol.technologies.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log


class MyService : Service() {
    private val mBinder: IBinder = MyBinder()
    private var mHandler: Handler? = null
    var progress = 0
        private set

    var maxValue = 0
        private set

    var isPaused: Boolean? = null
        private set

    override fun onCreate() {
        super.onCreate()
        initValues()
    }

    private fun initValues() {
        mHandler = Handler(Looper.myLooper()!!)
        progress = 0
        isPaused = true
        maxValue = 5000
    }


    override fun onBind(intent: Intent): IBinder = mBinder


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
        super.onDestroy()
        Log.d(TAG, "onDestroy: called.")
    }

    companion object {
        private const val TAG = "MyService"
    }
}
