package com.example.apps.firebase.chat.beans;

import java.io.Serializable;

/**
 * Created by PriyamSaikia on 05-12-2016.
 */
public class ChatMessageBean implements Serializable {
    public ChatMessageBean() {

    }

    private String msg;
    private String senderName;
    private String photoUrl;

    public ChatMessageBean(String msg, String userName, String photoUrl) {
        this.msg = msg;
        this.senderName = userName;
        this.photoUrl = photoUrl;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
