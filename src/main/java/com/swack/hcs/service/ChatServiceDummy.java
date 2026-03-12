package com.swack.hcs.service;
import com.swack.hcs.bean.ChatLog;
import com.swack.hcs.bean.Room;
import com.swack.hcs.util.Loggable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * チャット機能を実行するクラス(ダミーデータ返却).
 */
@Transactional
@Service
public class ChatServiceDummy implements Loggable {

  /**
   * 指定されたルームIDとユーザーIDに基づいたチャットルーム情報取得(ダミー).
   *
   * @param roomId チャットルームのID
   * @param userId ユーザーのID
   * @return 取得したチャットルームの情報
   */
  public Room getRoom(String roomId, String userId) {
    // dummy
    log().info("[getRoom] " + roomId + " " + userId);
    Room room = new Room("R0000", "d_everyone", "d_UXXXX", false, false, 4);

    return room;
  }

  /**
   * 指定されたユーザーIDに関連するすべてのチャットルームリスト取得(ダミー).
   *
   * @param userId ユーザーのID
   * @return チャットルームのリスト
   */
  public ArrayList<Room> getRoomList(String userId) {
    // dummy
    log().info("[getRoomList] " + userId);
    ArrayList<Room> list = new ArrayList<Room>();
    list.add(new Room("R0000", "d_everyone", "d_UXXXX", false, false, 4));
    list.add(new Room("R0001", "d_ramdom", "d_UXXXX", false, false, 3));
    list.add(new Room("R0002", "d_連絡板", "d_UXXXX", false, false, 2));

    return list;
  }

  /**
   * 指定されたユーザーIDに関連するダイレクトチャットリスト取得(ダミー).
   *
   * @param userId ユーザーのID
   * @return ダイレクトチャットのリスト
   */
  public ArrayList<Room> getDirectList(String userId) {
    log().info("[getDirectList] " + userId);
    ArrayList<Room> list = new ArrayList<Room>();
    list.add(new Room("R0003", "d_情報花子", "d_UXXXX", false, false, 2));
    list.add(new Room("R0005", "d_情報次郎", "d_UXXXX", false, false, 2));

    return list;
  }

  /**
   * 指定されたルームIDに関連するチャットログリスト取得(ダミー).
   *
   * @param roomId チャットルームのID
   * @return チャットログのリスト
   */
  public List<ChatLog> getChatlogList(String roomId) {
    // dummy
    log().info("[getChatlogList] " + roomId);
    ArrayList<ChatLog> list = new ArrayList<ChatLog>();
    list.add(
        new ChatLog(
            1,
            "R0000",
            "U0001",
            "ダミー太郎",
            "",
            "どうもダミーです",
            null,
            new Timestamp(System.currentTimeMillis()),
                null));
    list.add(
        new ChatLog(
            2,
            "R0000",
            "U0002",
            "ダミー花子",
            "",
            "私もダミーです",
            null,
            new Timestamp(System.currentTimeMillis()),
                null));
    list.add(
        new ChatLog(
            3,
            "R0000",
            "U0003",
            "ダミー次郎",
            "",
            "奇遇ですね、私もダミーです",
            null,
            new Timestamp(System.currentTimeMillis()),
                null));
    list.add(
        new ChatLog(
            4,
            "R0000",
            "U0001",
            "ダミー太郎",
            "",
            "ダミーさんこんにちは",
            null,
            new Timestamp(System.currentTimeMillis()),null));
    list.add(
        new ChatLog(
            5,
            "R0000",
            "U0002",
            "ダミー次郎",
            "",
            "私は元気です",
            null,
            new Timestamp(System.currentTimeMillis()),
                null));

    return list;
  }

  /**
   * チャットログ保存(ダミー).
   *
   * @param roomId  チャットルームのID
   * @param userId  ユーザーのID
   * @param message メッセージの内容
   */
  public void saveChatLog(String roomId, String userId, String message) {
    // dummy
    log().info("[saveChatLog] " + roomId + " " + userId + " " + message);
  }
}
