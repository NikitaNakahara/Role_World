package com.nakaharadev.roleworld

import org.json.JSONObject

class Message {
    companion object {
        public val AUTH_REQ_TYPE = "auth"

        public val SIGN_IN_MODE = "sign_in"
        public val SIGN_UP_MODE = "sign_up"
    }

    private var requestType = ""
    private var requestMode = ""
    private var data        = ""
    private var userId      = ""

    constructor()

    constructor(requestType: String, requestMode: String, data: String, userId: String) {
        this.requestType = requestType
        this.requestMode = requestMode
        this.data = data
        this.userId = userId
    }

    constructor(requestType: String, requestMode: String, data: JSONObject, userId: String) {
        this.requestType = requestType
        this.requestMode = requestMode
        this.data = data.toString()
        this.userId = userId
    }

    constructor(msg: String) : this(JSONObject(msg))

    constructor(msg: JSONObject) {
        this.requestType = msg.getString("type")
        this.requestMode = msg.getString("mode")
        if (msg.getString("data").isNotEmpty()) this.data = JSONObject(msg.getString("data")).toString()
        this.userId = msg.getString("id")
    }


    public fun getRequestType(): String {
        return requestType
    }

    public fun getRequestMode(): String {
        return requestMode
    }

    public fun getData(): String {
        return data
    }

    public fun getUserId(): String {
        return userId
    }

    public fun setRequestType(value: String) {
        requestType = value
    }

    public fun setRequestMode(value: String) {
        requestMode = value
    }

    public fun setData(value: String) {
        data = value
    }

    public fun setData(value: JSONObject) {
        data = value.toString()
    }


    public fun setUserId(value: String) {
        userId = value
    }

    override fun toString(): String {
        val json = JSONObject()

        json.put("type", requestType)
        json.put("mode", requestMode)
        json.put("data", data)
        json.put("id", userId)

        return json.toString()
    }
}