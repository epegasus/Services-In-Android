package com.funsol.technologies.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.funsol.technologie.R
import com.funsol.technologie.databinding.ActivityMainBinding
import com.funsol.technologies.MainApplication
import com.funsol.technologies.MainApplication.Companion.getMainApplication
import com.funsol.technologies.services.MyService
import com.funsol.technologies.viewModel.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mService: MyService? = null
    val mViewModel: MainActivityViewModel by viewModels()
    private lateinit var mainApplication: MainApplication

    var isChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mainApplication = getMainApplication(this)
        setContentView(binding.root)
        setObservers()
        listener()
    }

    private fun listener() {
        binding.toggleUpdates.setOnClickListener {
            toggleUpdates()

            isChecked = !isChecked

            mainApplication.showNotification(isChecked)
        }
    }

    private fun setObservers() {
        mViewModel.binder.observe(this) { myBinder ->
            if (myBinder == null) {
                Log.d(TAG, "onChanged: unbound from service")
            } else {
                Log.d(TAG, "onChanged: bound to service.")
                mService = myBinder.service
            }
        }
        mViewModel.isProgressBarUpdating.observe(this) { aBoolean ->
            val handler = Handler(Looper.getMainLooper())
            val runnable: Runnable = object : Runnable {
                override fun run() {
                    if (mViewModel.isProgressBarUpdating.value == true) {
                        mService?.let {
                            if (mViewModel.binder.value != null) { // meaning the service is bound
                                if (it.progress == it.maxValue) {
                                    mViewModel.setIsProgressBarUpdating(false)
                                }
                                binding.progressBar.progress = it.progress
                                binding.progressBar.max = it.maxValue
                                val progress: String = java.lang.String.valueOf(100 * it.progress / it.maxValue) + "%"
                                binding.textView.text = progress
                            }
                        }

                        handler.postDelayed(this, 100)
                    } else {
                        handler.removeCallbacks(this)
                    }
                }
            }

            // control what the button shows
            if (aBoolean == true) {
                binding.toggleUpdates.text = getString(R.string.pause)
                handler.postDelayed(runnable, 100)
            } else {
                if (mService?.progress == mService?.maxValue) {
                    binding.toggleUpdates.text = getString(R.string.restart)
                } else {
                    binding.toggleUpdates.text = getString(R.string.start)
                }
            }
        }
    }

    private fun toggleUpdates() {
        println("ServiceValue: is $mService")
        if (mService != null) {
            if (mService?.progress == mService?.maxValue) {
                mService!!.resetTask()
                binding.toggleUpdates.text = getString(R.string.start)
            } else {
                if (mService?.isPaused == true) {
                    mService!!.unPausePretendLongRunningTask()
                    mViewModel.setIsProgressBarUpdating(true)
                } else {
                    mService!!.pausePretendLongRunningTask()
                    mViewModel.setIsProgressBarUpdating(false)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startService()
    }

    override fun onStop() {
        super.onStop()
        unbindService(mViewModel.serviceConnection)
    }

    private fun startService() {
        val serviceIntent = Intent(this, MyService::class.java)
        startService(serviceIntent)
        bindService()
    }

    private fun bindService() {
        val serviceBindIntent = Intent(this, MyService::class.java)
        bindService(serviceBindIntent, mViewModel.serviceConnection, BIND_AUTO_CREATE)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}











