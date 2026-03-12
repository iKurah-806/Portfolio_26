package com.swack.hcs.bean;

import java.sql.Timestamp;

/**
 * チャットログ情報を管理するBean.
 * チャットの各メッセージに関する詳細情報を保持します。
 */
public class ChatLog {

    /**
     * チャットログID.
     * 各チャットメッセージを一意に識別します。
     */
    private int chatLogId;

    /**
     * ルームID.
     * このチャットメッセージが属するチャットルームを識別します。
     */
    private String roomId;

    /**
     * ユーザID.
     * メッセージの投稿者を識別します。
     */
    private String userId;

    /**
     * ユーザ名.
     * メッセージの投稿者の名前です。
     */
    private String userName;

    /**
     * ユーザ画像パス.
     * メッセージの投稿者のプロフィール画像のパスを格納します。
     * 画像がない場合は空文字列です。
     */
    private String userImgPath;

    /**
     * メッセージ.
     * チャットでのメッセージ内容です。
     */
    private String message;

    /**
     * 星付きメッセージ
     * ★がついたメッセージ
     */
    private String star;

    /**
     * 投稿日時.
     * メッセージが投稿された時刻です。
     */
    private Timestamp createdAt;

    private String imgPath;

    // コンストラクタ
    public ChatLog(int chatLogId, String roomId, String userId, String userName, String userImgPath, String message, String star, Timestamp createdAt, String imgPath) {
        this.chatLogId = chatLogId;
        this.roomId = roomId;
        this.userId = userId;
        this.userName = userName;
        this.userImgPath = userImgPath;
        this.message = message;
        this.star = star;
        this.createdAt = createdAt;
        this.imgPath = imgPath;
    }

    // getterメソッド
    public int getChatLogId() {
        return chatLogId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImgPath() {
        return userImgPath;
    }

    public String getMessage() {
        return message;
    }

    public String getStar() {
        return star;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getImgPath() {
        return imgPath;
    }

    // setterメソッド
    public void setChatLogId(int chatLogId) {
        this.chatLogId = chatLogId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserImgPath(String userImgPath) {
        this.userImgPath = userImgPath;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    // toStringメソッド
    @Override
    public String toString() {
        return "ChatLog{" +
                "chatLogId=" + chatLogId +
                ", roomId='" + roomId + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userImgPath='" + userImgPath + '\'' +
                ", message='" + message + '\'' +
                ", star='" + star + '\'' +
                ", createdAt=" + createdAt +
                ", imgPath=" + imgPath +
                '}';
    }
}
