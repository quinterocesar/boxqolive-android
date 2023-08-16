package com.boxqo.boxqolive

import android.Manifest
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.util.BitrateAdapter
import java.io.File


class MainActivity : ComponentActivity(), ConnectCheckerRtmp, SurfaceHolder.Callback,
    GlassGestureDetector.OnGestureListener {

    private lateinit var socketHandler: SocketHandler

    private val currentDateAndTime = ""
    private val surfaceView: SurfaceView? = null
    private val bitrateAdapter: BitrateAdapter? = null
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var glassGestureDetector: GlassGestureDetector? = null
    private var folder: File? = null
    private var rtmpCamera1: RtmpCamera1? = null
    private var name: String = "GG-01"
    private var deviceName: String = "GG01"
    private var serverIpAddress: String = "192.168.18.240"
    private var etUrl: String = "rtmp://$serverIpAddress/live/$deviceName"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionLauncherMultiple.launch(permissions)

        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        glassGestureDetector = GlassGestureDetector(this, this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        socketHandler = SocketHandler()

        rtmpCamera1 = RtmpCamera1(surfaceView, this)
        rtmpCamera1!!.setReTries(3)
        surfaceView.holder.addCallback(this)

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                clientHealth()
                mainHandler.postDelayed(this, 5000)
            }
        })

        clientConf()

        socketHandler.onNewEvent.observe(this) {
            val event = it
            if (event.type == ACTION && event.deviceName == deviceName) {
                onGlassEvent(event)
            }
        }

    }

    private val permissionLauncherMultiple = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        var allAreGranted = true
        for (isGranted in result.values) {
            allAreGranted = allAreGranted && isGranted
        }

        if (allAreGranted) {

        } else {
            val event = GlassEvent(
                deviceName = deviceName,
                type = INFO,
                message = "ALL PERMISSIONS DENIED"
            )
            socketHandler.emitEvent(event)
        }
    }

    private fun onGlassEvent(event: GlassEvent) {
        if (event.action == "START_STREAMING") {
            if (rtmpCamera1!!.isRecording || rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
                rtmpCamera1!!.prepareVideo(
                    1280,
                    720,
                    60,
                    5000000,
                    1,
                    0
                )
                rtmpCamera1!!.disableAudio()
                rtmpCamera1!!.prepareVideo()
                rtmpCamera1!!.enableVideoStabilization()
                rtmpCamera1!!.startStream(etUrl)

                val glassEvent = GlassEvent(
                    deviceName = deviceName,
                    type = INFO,
                    message = "STREAMING_STARTED"
                )
                socketHandler.emitEvent(glassEvent)

            } else {
                val glassEvent = GlassEvent(
                    deviceName = deviceName,
                    type = ERROR,
                    message = "ERROR PREPARING STREAM, THIS DEVICE CANT DO IT"
                )
                socketHandler.emitEvent(glassEvent)
            }
        }

        if (event.action == "STOP_STREAMING") {
            if (!rtmpCamera1!!.isRecording) {
                rtmpCamera1!!.stopStream()
                rtmpCamera1!!.stopPreview()
                rtmpCamera1!!.disableVideoStabilization()


                val glassEvent = GlassEvent(
                    deviceName = deviceName,
                    type = INFO,
                    message = "STREAMING_STOPPED"
                )
                socketHandler.emitEvent(glassEvent)
            }
        }
    }

    private fun clientConf() {
        val event = GlassEvent(
            deviceName = deviceName,
            type = CLIENT_CONF,
        )
        socketHandler.emitEvent(event)
    }

    private fun clientHealth() {
        val bm = applicationContext.getSystemService(BATTERY_SERVICE) as BatteryManager
        val healthEvent = GlassHealth(
            deviceName = deviceName,
            type = APP_HEALTH,
            name = name,
            isConnected = true,
            batteryLife = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
            isStreaming = rtmpCamera1!!.isStreaming,
            isAppOpen = true
        )
        socketHandler.emitHealthEvent(healthEvent)
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread {
            val event = GlassEvent(
                deviceName = deviceName,
                type = ERROR,
                message = "RTMP AUTH FAILED"
            )
            socketHandler.emitEvent(event)

            rtmpCamera1!!.stopStream()
        }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread {
            val event = GlassEvent(
                deviceName = deviceName,
                type = INFO,
                message = "RTMP AUTH SUCCESS"
            )
            socketHandler.emitEvent(event)
        }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            if (rtmpCamera1!!.reTry(5000, reason, null)) {
                val event = GlassEvent(
                    deviceName = deviceName,
                    type = ERROR,
                    message = "RTMP RETRY CONNECTION $reason"
                )
                socketHandler.emitEvent(event)
            } else {
                val event = GlassEvent(
                    deviceName = deviceName,
                    type = ERROR,
                    message = "RTMP CONNECTION FAILED"
                )
                socketHandler.emitEvent(event)

                rtmpCamera1!!.stopStream()
            }
        }
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {

    }

    override fun onConnectionSuccessRtmp() {
        runOnUiThread {
            val event = GlassEvent(
                deviceName = deviceName,
                type = INFO,
                message = "RTMP CONNECTION SUCCESS"
            )
            socketHandler.emitEvent(event)
        }
    }

    override fun onDisconnectRtmp() {
        runOnUiThread {
            val event = GlassEvent(
                deviceName = deviceName,
                type = INFO,
                message = "RTMP DISCONNECTED"
            )
            socketHandler.emitEvent(event)
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {}

    override fun surfaceCreated(p0: SurfaceHolder) {}

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        if (rtmpCamera1!!.isStreaming) {
            rtmpCamera1!!.stopStream()
        }
        rtmpCamera1!!.stopPreview()
    }

    override fun onStop() {
        super.onStop()
        val event = GlassEvent(
            deviceName = deviceName,
            type = INFO,
            message = "APP_CLOSED"
        )
        socketHandler.emitEvent(event)
    }

    override fun onDestroy() {
        socketHandler.disconnectSocket()
        super.onDestroy()
    }

    override fun onGesture(gesture: GlassGestureDetector.Gesture?): Boolean {
        Log.d("DATA DEBUG", "$gesture")
        return false
    }

    companion object EVENT_TYPE {
        private const val CLIENT_CONF = "CLIENT_CONF"
        private const val APP_HEALTH = "APP_HEALTH"
        private const val ACTION = "ACTION"
        private const val ERROR = "ERROR"
        private const val INFO = "INFO"
    }


}