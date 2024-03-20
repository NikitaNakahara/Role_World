package com.nakaharadev.roleworld

import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket


class Network {
    private val HOST = "192.168.1.33"
    private val PORT = 4035

    private var socket: Socket? = null
    private var networkCallback: NetworkCallback? = null

    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null

    private var networkThread: Thread? = null

    private var isConnected = false

    private val messageQueueController = MessageQueueController()

    private val getCallbacksList = ArrayList<GetMessageCallbackData>()

    init {
        messageQueueController.setCallback(object: MessageQueueController.MessageQueueControllerCallback {
            override fun onMessageReadyToSend(msg: Message) {
                try {
                    if (msg.toString().length > 30000) {
                        val strMsg = msg.toString()
                        var len = strMsg.length
                        var offset = 0
                        var count = 0

                        val strings = ArrayList<String>()

                        while (len > 30000) {
                            val strBuilder = StringBuilder()

                            for (i: Int in 0 until 30000) {
                                strBuilder.append(strMsg[i + offset])
                            }

                            strings.add(strBuilder.toString())
                            offset += 30000
                            count++
                            len -= 30000
                        }

                        if (len != 0) {
                            val strBuilder = StringBuilder()

                            for (i: Int in 0 until len) {
                                strBuilder.append(strMsg[i + offset])
                            }

                            strings.add(strBuilder.toString())
                            count++
                        }

                        val sysMsg = Message()
                        sysMsg.setRequestMode("msg_is_multiblock")
                        sysMsg.setRequestType("sys")

                        output?.writeUTF(sysMsg.toString())
                        output?.flush()

                        for (i: Int in 0 until strings.size) {
                            output?.writeUTF(strings[i])
                            output?.flush()
                        }

                        output?.writeUTF("\$END$")
                        output?.flush()
                    } else {
                        output?.writeUTF(msg.toString())
                        output?.flush()
                    }

                    networkCallback?.onSendMessage(msg)
                } catch (e: IOException) {
                    //e.printStackTrace()
                }
            }
        })

        messageQueueController.runControllerLoop()
    }

    public fun isConnected(): Boolean {
        return this.isConnected
    }

    public fun addGetMessageCallback(request: String, callback: (Message) -> Unit) {
        val data = GetMessageCallbackData()
        data.request = request
        data.callback = callback

        getCallbacksList.add(data)
    }

    public fun sendMessageAndAddCallback(message: Message, request: String, callback: (Message) -> Unit) {
        sendMsg(message)
        addGetMessageCallback(request, callback)
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
                        var msgObject = Message(msg.toString())

                        if (msgObject.getRequestType() == "sys") {
                            if (msgObject.getRequestMode() == "msg_is_multiblock") {
                                val result = StringBuilder()
                                var inputString: String?
                                while (input!!.readUTF().also { inputString = it } != "\$END$") {
                                    result.append(inputString)
                                }
                                Log.i("input", result.toString())
                                msgObject = Message(result.toString())
                            }
                        }

                        for (callbackData: GetMessageCallbackData in getCallbacksList) {
                            if (callbackData.request == JSONObject(msgObject.getData()).getString("request")) {
                                callbackData.callback!!(msgObject)
                            }
                        }

                        networkCallback?.onGetMessage(msgObject)
                    } catch (e: IOException) {
                        //e.printStackTrace()
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

    public fun stop() {
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
                //e.printStackTrace()
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
            //e.printStackTrace()

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

    private class GetMessageCallbackData {
        public var request = ""
        public var callback: ((Message) -> Unit)? = null
    }
}