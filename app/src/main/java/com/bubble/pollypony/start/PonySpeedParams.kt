package com.bubble.pollypony.start

import android.util.Base64
import android.util.Log
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.net.InetAddress
import kotlin.system.measureTimeMillis

class PonySpeedParams {
    companion object {
        private var testDownload: SpeedTestSocket = SpeedTestSocket()
        private var testUpload: SpeedTestSocket = SpeedTestSocket()
        private var downloadJob: Job? = null
        private var uploadJob: Job? = null
        private var pingJob: Job? = null
        private var downSpeeds: MutableList<Double> = mutableListOf()
        private var upSpeeds: MutableList<Double> = mutableListOf()

        fun octet2kMB(octet: Double): String {
            val kbps = (octet * 8) / 1000
            val mbps = (octet * 8) / 1000000

            return when {
                mbps >= 1 -> String.format("%.2f Mb/s", mbps)
                kbps >= 1 -> String.format("%.2f kb/s", kbps)
                else -> String.format("%.2f b/s", octet * 8)
            }
        }

        fun ponyDownload(download: (String) -> Unit, upload: (String) -> Unit) {
            downloadJob?.cancel()
            downloadJob = CoroutineScope(Dispatchers.IO).launch {
                testDownload.clearListeners()
                downSpeeds.clear()
                testDownload.addSpeedTestListener(object : ISpeedTestListener {
                    override fun onCompletion(report: SpeedTestReport?) {
                        if (report == null) return
                        if (downSpeeds.isEmpty()) {
                            if (report.transferRateOctet.toDouble() > 10000000) {
                                return
                            }
                            val speed = octet2kMB(report.transferRateOctet.toDouble())
                            download.invoke(speed)
                            speedTestUpload(upload)
                        } else {
                            val speed = octet2kMB(downSpeeds.max())
                            download.invoke(speed)
                            speedTestUpload(upload)
                        }
                    }

                    override fun onProgress(percent: Float, report: SpeedTestReport?) {
                        if (report == null) return
                        if (report.transferRateOctet.toDouble() > 10000000) {
                            return
                        }
                        downSpeeds.add(report.transferRateOctet.toDouble())
                    }

                    override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                        CoroutineScope(Dispatchers.Main).launch {
                            download.invoke("0kb/s")
                            upload.invoke("0kb/s")
                        }
                    }

                })
                testDownload.startDownload(String(Base64.decode("aHR0cHM6Ly9oaWwucHJvb2Yub3ZoLnVzL2ZpbGVzLzFNYi5kYXQ=", 0)), 10000)
            }
        }

        private fun speedTestUpload(upload: (String) -> Unit) {
            uploadJob?.cancel()
            uploadJob = CoroutineScope(Dispatchers.IO).launch {
                testUpload.clearListeners()
                upSpeeds.clear()
                testUpload.addSpeedTestListener(object : ISpeedTestListener {
                    override fun onCompletion(report: SpeedTestReport?) {
                        if (report == null) return
                        if (upSpeeds.isEmpty()) {
                            if (report.transferRateOctet.toDouble() > 10000000) {
                                return
                            }

                            val speed = octet2kMB(report.transferRateOctet.toDouble())
                            upload.invoke(speed)
                        } else {
                            val speed = octet2kMB(upSpeeds.max())
                            upload.invoke(speed)
                        }
                    }

                    override fun onProgress(percent: Float, report: SpeedTestReport?) {
                        if (report == null) return
                        if (report.transferRateOctet.toDouble() > 10000000) {
                            return
                        }
                        upSpeeds.add(report.transferRateOctet.toDouble())
                    }

                    override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                        upload.invoke("0kb/s")
                    }

                })
                testUpload.startUpload(String(Base64.decode("aHR0cHM6Ly9oaWwucHJvb2Yub3ZoLnVzL2ZpbGVzLw==", 0)), 1048576, 10000)
            }
        }

        fun ponyPing(ping: (String) -> Unit) {
            pingJob?.cancel()
            pingJob = CoroutineScope(Dispatchers.IO).launch {
                kotlin.runCatching {
                    val address = InetAddress.getByName("google.com")
                    val latency = measureTimeMillis {
                        address.isReachable(2000)
                    }
                    val ms = latency.toString() + "MS"
                    ping.invoke(ms)
                }
            }
        }

        fun endAllPingSpeed() {
            downloadJob?.cancel()
            uploadJob?.cancel()
            pingJob?.cancel()
        }
    }
}