package com.boxqo.boxqolive

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.util.BitrateAdapter
import java.io.File


class MainActivity : ComponentActivity(), ConnectCheckerRtmp, View.OnClickListener,
    SurfaceHolder.Callback {
    private val currentDateAndTime = ""
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val surfaceView: SurfaceView? = null
    private val bitrateAdapter: BitrateAdapter? = null
    private var rtmpCamera1: RtmpCamera1? = null
    private var btnStartStop: Button? = null
    private var folder: File? = null
    private var etUrl: String = "rtmp://192.168.0.146/live/ring01"
    private var stopAttempts: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        btnStartStop = findViewById(R.id.b_start_stop)
        btnStartStop?.setOnClickListener(this)
        rtmpCamera1 = RtmpCamera1(surfaceView, this)
        rtmpCamera1!!.setReTries(3)
        surfaceView.holder.addCallback(this)
        permissionLauncherMultiple.launch(permissions)
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
            Toast.makeText(this@MainActivity, "Todos los servicios denegados", Toast.LENGTH_LONG).show()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.b_start_stop -> if (!rtmpCamera1!!.isStreaming) {
                if (rtmpCamera1!!.isRecording || rtmpCamera1!!.prepareAudio() && rtmpCamera1!!.prepareVideo()) {
                    rtmpCamera1!!.prepareVideo(
                        1024,
                        768,
                        30,
                        5000000,
                        0,
                        0
                    )
                    rtmpCamera1!!.disableAudio()
                    rtmpCamera1!!.prepareVideo()
                    rtmpCamera1!!.startStream(etUrl)
                    btnStartStop!!.setBackgroundColor(Color.parseColor("#D50222"))
                    btnStartStop!!.setText(R.string.stop_button)
                } else {
                    Toast.makeText(
                        this, "Error preparing stream, This device cant do it",
                        Toast.LENGTH_LONG
                    ).show()
                    btnStartStop!!.setBackgroundColor(Color.parseColor("#41D502"))
                    btnStartStop!!.setText(R.string.start_button)
                }
            } else {
                if(stopAttempts === 1) {
                    stopAttempts = 3
                    btnStartStop!!.setBackgroundColor(Color.parseColor("#41D502"))
                    btnStartStop!!.setText(R.string.start_button)
                    rtmpCamera1!!.stopStream()
                } else {
                    stopAttempts = stopAttempts - 1

                    if(stopAttempts !== 0){
                        Toast.makeText(
                            this@MainActivity,
                            "Tap ${stopAttempts} more times to stop",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else -> {}
        }
    }

    override fun onAuthErrorRtmp() {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Auth error", Toast.LENGTH_LONG).show()
            rtmpCamera1!!.stopStream()
            btnStartStop!!.setBackgroundColor(Color.parseColor("#41D502"))
            btnStartStop!!.setText(R.string.start_button)
        }
    }

    override fun onAuthSuccessRtmp() {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Auth success", Toast.LENGTH_LONG).show()
        }
    }

    override fun onConnectionFailedRtmp(reason: String) {
        runOnUiThread {
            if (rtmpCamera1!!.reTry(5000, reason, null)) {
                Toast.makeText(this@MainActivity, "Retry", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Connection failed. $reason",
                    Toast.LENGTH_LONG
                )
                    .show()
                rtmpCamera1!!.stopStream()
                btnStartStop!!.setBackgroundColor(Color.parseColor("#41D502"))
                btnStartStop!!.setText(R.string.start_button)

            }
        }
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {

    }

    override fun onConnectionSuccessRtmp() {
        runOnUiThread {
            Toast.makeText(
                this@MainActivity,
                "Connection success",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDisconnectRtmp() {
        runOnUiThread {
            Toast.makeText(this@MainActivity, "Disconnected", Toast.LENGTH_LONG).show()
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) { }

    override fun surfaceCreated(p0: SurfaceHolder) {

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) { }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        if (rtmpCamera1!!.isStreaming) {
            rtmpCamera1!!.stopStream()
            btnStartStop!!.setBackgroundColor(Color.parseColor("#41D502"))
            btnStartStop!!.setText(R.string.start_button)
        }
        rtmpCamera1!!.stopPreview()
    }

}