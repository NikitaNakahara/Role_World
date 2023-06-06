package com.nakaharadev.roleworld

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

    private var isConnected = false

    private val messageQueueController = MessageQueueController()

    init {
        messageQueueController.setCallback(object: MessageQueueController.MessageQueueControllerCallback {
            override fun onMessageReadyToSend(msg: Message) {
                try {
                    output?.writeUTF(msg.toString())
                    output?.flush()

                    networkCallback?.onSendMessage(msg)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        })

        messageQueueController.runControllerLoop()
    }

    public fun setNetworkCallback(callback: NetworkCallback) {
        networkCallback = callback
    }

    public fun run() {
        networkThread = Thread {
            do {
                isConnected = false
                messageQueueController.setNetworkIsConnected(false)

                createConnection()
                createIOStreams()

                networkCallback?.onConnected()


                while (socket != null) {
                    try {
                        val msg = input?.readUTF()
                        if (msg != null) {
                            networkCallback?.onGetMessage(Message(msg))
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

    public fun sendMsg(msg: Message) {
        messageQueueController.addMessage(msg)
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

            isConnected = true
            messageQueueController.setNetworkIsConnected(true)
        } catch (e: IOException) {
            e.printStackTrace()

            createConnection()
            createIOStreams()
        }
    }

    public interface NetworkCallback {
        public fun onConnected()
        public fun onGetMessage(msg: Message)
        public fun onSendMessage(msg: Message) {}
        public fun onDisconnected() {}
    }
}