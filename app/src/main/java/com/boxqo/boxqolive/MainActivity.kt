package com.boxqo.boxqolive

import android.Manifest
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.boxqo.boxqolive.utils.PathUtil
import com.pedro.rtmp.utils.ConnectCheckerRtmp
import com.pedro.rtplibrary.rtmp.RtmpCamera1
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

    private var rtmpCamera1: RtmpCamera1? = null
    private var btnStartStop: Button? = null
    private var folder: File? = null
    private var etUrl: String = "rtmp://192.168.18.240/live/ring01"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
            btnStartStop = findViewById(R.id.b_start_stop)
            rtmpCamera1 = RtmpCamera1(surfaceView, this)
            rtmpCamera1!!.setReTries(10)
            surfaceView.holder.addCallback(this)
            val camera = if (rtmpCamera1 !== null) "yes" else "no"
            Toast.makeText(this@MainActivity, camera, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Todos los servicios denegados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthErrorRtmp() {
        TODO("Not yet implemented")
    }

    override fun onAuthSuccessRtmp() {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailedRtmp(reason: String) {
        TODO("Not yet implemented")
    }

    override fun onConnectionStartedRtmp(rtmpUrl: String) {
        TODO("Not yet implemented")
    }

    override fun onConnectionSuccessRtmp() {
        TODO("Not yet implemented")
    }

    override fun onDisconnectRtmp() {
        TODO("Not yet implemented")
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        TODO("Not yet implemented")
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

    override fun surfaceCreated(p0: SurfaceHolder) {

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {

    }

}