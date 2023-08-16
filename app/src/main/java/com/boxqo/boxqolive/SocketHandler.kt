package com.boxqo.boxqolive

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

class SocketHandler {

    private var socket: Socket? = null
    //private var socketId: String? = null

    private val _onNewEvent = MutableLiveData<GlassEvent>()
    val onNewEvent: LiveData<GlassEvent> get() = _onNewEvent

    init {
        try {
            socket = IO.socket(SOCKET_URL)
            socket?.connect()

            registerOnNewEvent()

        }catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun registerOnNewEvent() {
        socket?.on(EVENT_KEYS.BROADCAST) { args->
            args?.let { d ->
                if (d.isNotEmpty()) {
                    val data = d[0]
                    Log.d("DATA DEBUG","$data")
                    if (data.toString().isNotEmpty()) {
                        val event = Gson().fromJson(data.toString(), GlassEvent::class.java)
                        _onNewEvent.postValue(event)
                    }
                }

            }
        }
    }

    fun disconnectSocket() {
        socket?.disconnect()
        socket?.off()
    }


    fun emitEvent(event: GlassEvent) {

        val jsonStr = Gson().toJson(event, GlassEvent::class.java)
        socket?.emit(EVENT_KEYS.NEW_EVENT, jsonStr)
    }
    fun emitHealthEvent(event: GlassHealth) {

        val jsonStr = Gson().toJson(event, GlassHealth::class.java)
        socket?.emit(EVENT_KEYS.NEW_EVENT, jsonStr)
    }

    private object EVENT_KEYS {
        const val NEW_EVENT = "new_event"
        const val BROADCAST = "broadcast"
    }

    companion object{
        private const val SOCKET_URL = "http://192.168.0.146:3000/"
    }

}