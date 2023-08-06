package com.boxqo.boxqolive

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionLauncherMultiple.launch(permissions)
    }

    private val permissionLauncherMultiple = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        //here we will check if permissions were now (from permission request dialog) or already granted or not

        var allAreGranted = true
        for (isGranted in result.values) {
            allAreGranted = allAreGranted && isGranted
        }

        if (allAreGranted) {
            //All Permissions granted now do the required task here or call the function for that
            Toast.makeText(this@MainActivity, "Todos los permisos concedidos", Toast.LENGTH_SHORT).show()
        } else {
            //All or some Permissions were denied so can't do the task that requires that permission
            Toast.makeText(this@MainActivity, "Todos los servicios denegados", Toast.LENGTH_SHORT).show()
        }
    }


}