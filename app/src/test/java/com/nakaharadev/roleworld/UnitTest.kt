package com.nakaharadev.roleworld

import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*

class UnitTest {
    @Test
    fun testMessageQueueController() {
        val message = Message()
        message.setRequestMode("auth")
        message.setRequestMode("sign_in")
        message.setUserId("0")
        message.setData(JSONObject().put("nickname", "nakaharadev"))

        val controller = MessageQueueController()
        controller.setCallback(object: MessageQueueController.MessageQueueControllerCallback {
            override fun onMessageReadyToSend(msg: Message) {
                assertEquals(message.toString(), msg.toString())
            }
        })
        controller.runControllerLoop()
        controller.setNetworkIsConnected(true)
        controller.addMessage(message)
    }
}