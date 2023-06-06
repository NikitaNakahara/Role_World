package com.nakaharadev.roleworld

import java.util.LinkedList
import java.util.Queue

class MessageQueueController {
    private var networkIsConnected = false

    private var queue: Queue<Message> = LinkedList()

    private var callback: MessageQueueControllerCallback? = null

    public fun setNetworkIsConnected(value: Boolean) {
        networkIsConnected = value
    }

    public fun setCallback(callback: MessageQueueControllerCallback) {
        this.callback = callback
    }

    public fun addMessage(msg: Message) {
        queue.add(msg)
    }

    public fun runControllerLoop() {
        Thread {
            while (true) {
                while (!networkIsConnected);

                while (true) {
                    if (queue.peek() == null) break
                    if (!networkIsConnected) break

                    callback?.onMessageReadyToSend(queue.remove())
                }
            }
        }.start()
    }

    public interface MessageQueueControllerCallback {
        public fun onMessageReadyToSend(msg: Message);
    }
}