package com.nakaharadev.roleworld

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

class Network {
    private val HOST = "192.168.1.36"
    private val PORT = 3045

    private var socket: Socket? = null
    private var networkCallback: NetworkCallback? = null

    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null

    private var networkThread: Thread? = null

    public fun setNetworkCallback(callback: NetworkCallback) {
        networkCallback = callback
    }

    public fun run() {
        networkThread = Thread {
            do {
                createConnection()
                createIOStreams()

                networkCallback?.onConnected()


                while (socket != null) {
                    try {
                        val msg = input?.readUTF()
                        if (msg != null) {
                            networkCallback?.onGetMessage(msg)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        socket = null
                        networkCallback?.onDisconnected()
                    }
                }
            } while (true)
        }
        networkThread?.start()
    }

    public fun sendMsg(msg: String) {
        Thread {
            try {
                output?.writeUTF(msg)
            } catch (e: IOException) {
                e.printStackTrace()
                socket = null
                networkCallback?.onDisconnected()
            }
        }.start()
    }

    public fun close() {
        input?.close()
        output?.close()
        socket?.close()
        networkThread?.interrupt()
    }

    private fun createConnection() {
        do {
            try {
                socket = null
                socket = Socket(HOST, PORT)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } while (socket == null)
    }

    private fun createIOStreams() {
        try {
            input = DataInputStream(socket?.getInputStream())
            output = DataOutputStream(socket?.getOutputStream())
        } catch (e: IOException) {
            e.printStackTrace()

            createConnection()
            createIOStreams()
        }
    }

    public abstract class NetworkCallback {
        public abstract fun onConnected()
        public abstract fun onGetMessage(msg: String)
        public fun onDisconnected() {}
    }
}