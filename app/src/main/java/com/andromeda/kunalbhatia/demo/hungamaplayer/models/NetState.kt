package com.andromeda.kunalbhatia.demo.hungamaplayer.models

class NetState {

    private var status: Status
    private var msg: String
    private var code: Int

    companion object {
        var LOADED: NetState = NetState(Status.FOUND, "FOUND")
        var LOADING: NetState = NetState(Status.FINDING, "FINDING")
    }

    constructor(status: Status, msg: String) {
        this.status = status
        this.msg = msg
        this.code = -1
    }


    constructor(status: Status, msg: String, code: Int) {
        this.status = status
        this.msg = msg
        this.code = code
    }

    fun getStatus(): Status {
        return status
    }

    fun getMsg(): String {
        return msg
    }

    fun getCode(): Int{
        return code;
    }

    enum class Status{
        INITIALIZING,
        FINDING,
        FOUND,
        ERROR,
        NOT_FOUND;
    }

}