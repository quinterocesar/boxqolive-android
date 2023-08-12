package com.boxqo.boxqolive

import android.Manifest
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.util.BitrateAdapter
import java.io.File


class MainActivity : ComponentActivity(), ConnectCheckerRtmp, SurfaceHolder.Callback {

    private lateinit var socketHandler: SocketHandler
    private val currentDateAndTime = ""
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val surfaceView: SurfaceView? = null
    private val bitrateAdapter: BitrateAdapter? = null
    private var rtmpCamera1: RtmpCamera1? = null
    private var folder: File? = null
    private var deviceName: String = "ring01"
    private var etUrl: String = "rtmp://192.168.0.146/live/$deviceName"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionLauncherMultiple.launch(permissions)

        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        socketHandler = SocketHandler()

        rtmpCamera1 = RtmpCamera1(surfaceView, this)
        rtmpCamera1!!.setReTries(3)
        surfaceView.holder.addCallback(this)

        socketHandler.onNewEvent.observe(this) {
            val event = it
            if(event.type == "ACTION" && event.deviceName == deviceName){
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
            val event = GlassEvent(
                deviceName = deviceName,
                type = "APP_HEALTH",
                message = "ALL PERMISSIONS GRANTED"
            )
            socketHandler.emitEvent(event)
        } else {
            val event = GlassEvent(
                deviceName = deviceName,
                type = "APP_HEALTH",
                message = "ALL PERMISSIONS DENIED"
            )
            socketHandler.emitEvent(event)
        }
    }

    private fun onGlassEvent(event: GlassEvent){
        if (event.action == "START_STREAMING") {
            if (rtmpCamera1!!.isRecording || rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
                rtmpCamera1!!.prepareVideo(
                    1920,
                    1080,
                    60,
                    5000000,
                    0,
                    0
                )
                rtmpCamera1!!.disableAudio()
                rtmpCamera1!!.prepareVideo()
                rtmpCamera1!!.enableVideoStabilization()
                rtmpCamera1!!.startStream(etUrl)

                val event = GlassEvent(
                    deviceName = deviceName,
                    type = "INFO",
                    message = "STREAMING_STARTED"
                )
                socketHandler.emitEvent(event)

            } else {
                val event = GlassEvent(
                    deviceName = deviceName,
                    type = "ERROR",
                    message = "ERROR PREPARING STREAM, THIS DEVICE CANT DO IT"
                )
                socketHandler.emitEvent(event)
            }
        }

        if(event.action == "STOP_STREAMING") {
            if(rtmpCamera1!!.isRecording) {
                rtmpCamera1!!.stopStream()
                rtmpCamera1!!.stopPreview()
                rtmpCamera1!!.disableVideoStabilization()


                val event = GlassEvent(
                    deviceName = deviceName,
                    type = "INFO",
                    message = "STREAMING_STOPPED"
                )
                socketHandler.emitEvent(event)
            }
        }
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread {
            val event = GlassEvent(
                deviceName = deviceName,
                type = "ERROR",
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
                type = "INFO",
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
                    type = "ERROR",
                    message = "RTMP RETRY CONNECTION $reason"
                )
                socketHandler.emitEvent(event)
            } else {
                val event = GlassEvent(
                    deviceName = deviceName,
                    type = "ERROR",
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
                type = "INFO",
                message = "RTMP CONNECTION SUCCESS"
            )
            socketHandler.emitEvent(event)
        }
    }

    override fun onDisconnectRtmp() {
        runOnUiThread {
            val event = GlassEvent(
                deviceName = deviceName,
                type = "INFO",
                message = "RTMP DISCONNECTED"
            )
            socketHandler.emitEvent(event)
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) { }

    override fun surfaceCreated(p0: SurfaceHolder) { }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) { }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        if (rtmpCamera1!!.isStreaming) {
            rtmpCamera1!!.stopStream()
        }
        rtmpCamera1!!.stopPreview()
    }

    override fun onDestroy() {
        socketHandler.disconnectSocket()
        super.onDestroy()
    }

}