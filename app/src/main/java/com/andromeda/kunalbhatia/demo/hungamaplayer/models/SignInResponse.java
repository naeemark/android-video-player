package com.andromeda.kunalbhatia.demo.hungamaplayer.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ahsan.hameed on 8/8/2016.
 */
public class SignInResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private SignInData data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public SignInData getData() {
        return data;
    }

    public void setData(SignInData data) {
        this.data = data;
    }
}
