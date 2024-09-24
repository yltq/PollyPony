package com.bubble.pollypony.start

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ViewParams {
    var visible: Boolean = false

    companion object {
        var ponyCount: Long = 0
        var ponyCountJob: Job? = null
        var ponyCountLivedata: MutableLiveData<Long> = MutableLiveData()
        fun ponyCountBegin(count: Boolean) {
            ponyCountJob?.cancel()
            if (!count) return
            ponyCountJob = CoroutineScope(Dispatchers.IO).launch {
                ponyCount = 0
                do {
                    delay(1000)
                    ponyCount += 1
                    ponyCountLivedata.postValue(ponyCount)
                } while (PonyParams.ponyConnected)
            }
        }

        fun Context.ponyMain(): Boolean {
            val currentProcessId = android.os.Process.myPid()
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val processInfo = activityManager.runningAppProcesses.find { it.pid == currentProcessId }
            return processInfo?.processName == packageName
        }
    }
}