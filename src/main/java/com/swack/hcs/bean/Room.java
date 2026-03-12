package com.swack.hcs.bean;

/**
 * ルーム情報を管理するBean.
 * チャットルームの基本情報、ルームのタイプ、参加メンバー数などを保持します。
 */
public record Room(
    /**
     * ルームID.
     * 各チャットルームを一意に識別します。
     */
    String roomId,
    /**
     * ルーム名.
     * チャットルームの名前です。
     */
    String roomName,
    /**
     * ルーム作成者.
     * このルームを作成したユーザーのIDです
     */
    String createdUserId,
    /**
     * ダイレクトチャットか.
     * 個人間の直接チャットかどうかを示します。
     */
    boolean directed,
    /**
     * プライベートチャットか.
     * プライベートな（非公開の）チャットルームかどうかを示します。
     */
    boolean privated,
    /**
     * 参加メンバー数.
     * このチャットルームに参加しているユーザーの数を保持します。
     */
    int memberCount) {

  // 一覧表示用(参加メンバー数0名, ダイレクトチャットの場合はfalseで固定)
  public Room(String roomId, String roomName) {
    this(roomId, roomName, "", false, false, 0);
  }
}
