package com.swack.hcs.repository;

import com.swack.hcs.bean.ChatLog;
import com.swack.hcs.bean.Room;
import com.swack.hcs.util.AppConstants;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * チャットリポジトリ
 */
@Repository
public class ChatRepository {

  @Autowired
  private NamedParameterJdbcTemplate jdbc;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * チャットログリスト取得.
   *
   * @param roomId 部屋ID
   * @return チャットログリスト
   */
  public List<ChatLog> getChatlogList(String roomId) {

    final String sql = "SELECT CHATLOGID, U.USERID AS USERID, U.USERNAME AS USERNAME, U.USERIMGPATH AS USERIMGPATH, MESSAGE, C.STAR AS STAR, CREATED_AT, IMGPATH FROM CHATLOG C JOIN USERS U ON C.USERID = U.USERID WHERE ROOMID = :roomId ORDER BY CREATED_AT ASC";

    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);

    List<Map<String, Object>> resultList = jdbc.queryForList(sql, params);

    List<ChatLog> chatLogList = new ArrayList<ChatLog>();

    for (Map<String, Object> map : resultList) {
      int chatLogId = Integer.parseInt(String.valueOf(map.get("CHATLOGID")));
      String userId = (String) map.get("USERID");
      String userName = (String) map.get("USERNAME");
      String userImgPath = (String) map.get("USERIMGPATH");
      String message = (String) map.get("MESSAGE");
      String star = (String) map.get("STAR");
      Object createdAtObj = map.get("CREATED_AT");
      String imgPath = (String) map.get("IMGPATH");

      // Timestamp型へ変換
      Timestamp createdAt;
      if (createdAtObj instanceof Timestamp) {
        createdAt = (Timestamp) createdAtObj;
      } else {
        createdAt = Timestamp.valueOf((String) createdAtObj);
      }

      ChatLog chatLog = new ChatLog(
        chatLogId,
        roomId,
        userId,
        userName,
        userImgPath,
        message,
        star,
        createdAt,
        imgPath);
      chatLogList.add(chatLog);
    }

    return chatLogList;
  }


  //メッセージ削除
  public boolean deleteMessage(int chatLogId) {
    final String sqlDeleteMessage = "DELETE FROM CHATLOG WHERE CHATLOGID = :chatLogId";

    Map<String, Object> params = new HashMap<>();
    params.put("chatLogId", chatLogId);

    int result = jdbc.update(sqlDeleteMessage, params);

    if (result == 1) {
      return true;
    }

    return false;
  }

  //メッセージ編集
  public boolean updateMessage(int chatLogId, String newMessage) {
    final String sqlUpdateMessage = "UPDATE CHATLOG SET MESSAGE = :newMessage WHERE CHATLOGID = :chatLogId";

    Map<String, Object> params = new HashMap<>();
    params.put("chatLogId", chatLogId);
    params.put("newMessage", newMessage);

    int result = jdbc.update(sqlUpdateMessage, params);

    if (result == 1) {
      return true;

    }

    return false;

  }

  //chatLogIdからuserIdを取得

  public String getUserIdFromChatLogId(int chatLogId) {
    final String sqlGetUserId = "SELECT USERID FROM CHATLOG WHERE CHATLOGID = :chatLogId";

    Map<String, Object> params = new HashMap<>();
    params.put("chatLogId", chatLogId);

    List<Map<String, Object>> resultList = jdbc.queryForList(sqlGetUserId, params);

    return (String) resultList.get(0).get("userId");
  }

  // ★付きメッセージ
  public boolean starMessage(int chatLogId) {

    // star現在の状態
    String sql = "SELECT star FROM chatLog WHERE chatLogId = :chatLogId";
    Map<String, Object> params = new HashMap<>();
    params.put("chatLogId", chatLogId);
    // starの状態取得
    String starNow = jdbc.queryForObject(sql, params, String.class);

    // ★追加/削除
    String sqlUpdate;
    if (starNow == null) {
      sqlUpdate = "UPDATE chatLog SET star = '★' WHERE chatLogId = :chatLogId";
    } else {
      sqlUpdate = "UPDATE chatLog SET star = NULL WHERE chatLogId = :chatLogId";
    }

    int result = jdbc.update(sqlUpdate, params);

    if (result == 1) {
      return true;

    }
    return false;

  }

  /**星付きチャットログ取得
   *
   * @param userId
   * @return
   */
  public List<ChatLog> getStarredChatlogList(String userId) {

    // 星がついているメッセージだけ一覧表示する
    final String sql = "SELECT CHATLOGID, C.ROOMID, U.USERID AS USERID, U.USERNAME AS USERNAME, U.USERIMGPATH AS USERIMGPATH, MESSAGE, C.STAR AS STAR, CREATED_AT, IMGPATH FROM CHATLOG C JOIN USERS U ON C.USERID = U.USERID JOIN JOINROOM J ON C.ROOMID = J.ROOMID WHERE J.USERID = :userId AND C.STAR IS NOT NULL ORDER BY CREATED_AT DESC";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    List<Map<String, Object>> resultList = jdbc.queryForList(sql, params);

    List<ChatLog> chatLogList = new ArrayList<>();
    for (Map<String, Object> map : resultList) {
      int chatLogId = Integer.parseInt(String.valueOf(map.get("CHATLOGID")));
      String roomId = (String) map.get("ROOMID");
      String userId2 = (String) map.get("USERID");
      String userName = (String) map.get("USERNAME");
      String userImgPath = (String) map.get("USERIMGPATH");
      String message = (String) map.get("MESSAGE");
      String star = (String) map.get("STAR");
      Object createdAtObj = map.get("CREATED_AT");
      String imgPath = (String) map.get("IMGPATH");

      Timestamp createdAt;
      if (createdAtObj instanceof Timestamp) {
        createdAt = (Timestamp) createdAtObj;
      } else {
        createdAt = Timestamp.valueOf((String) createdAtObj);
      }

      ChatLog chatLog = new ChatLog(
          chatLogId,
          roomId,
          userId2,
          userName,
          userImgPath,
          message,
          star,
          createdAt,
          imgPath);
      chatLogList.add(chatLog);
    }

    return chatLogList;
  }

  /**
   * ルーム情報取得.
   *
   * @param roomId 部屋ID
   * @param userId ユーザID
   * @return ルーム情報
   */
  public Room getRoom(String roomId) {

    final String sqlGetRoom = "SELECT R.ROOMID, R.ROOMNAME, R.CREATEDUSERID, R.DIRECTED, R.PRIVATED, COUNT(*) AS MEMBER_COUNT FROM ROOMS R JOIN JOINROOM J ON R.ROOMID = J.ROOMID WHERE R.ROOMID = :roomId GROUP BY R.ROOMID, R.ROOMNAME, R.DIRECTED";

    Map<String, Object> paramsGetRoom = new HashMap<>();
    paramsGetRoom.put("roomId", roomId);

    List<Map<String, Object>> resultGetRoom = jdbc.queryForList(sqlGetRoom, paramsGetRoom);

    String roomName = "";
    String createdUserId = "";
    boolean directed = false;
    boolean privated = false;
    int memberCount = 0;

    for (Map<String, Object> map : resultGetRoom) {
      roomName = (String) map.get("ROOMNAME");
      createdUserId = (String) map.get("CREATEDUSERID");
      directed = (boolean) map.get("DIRECTED");
      privated = (boolean) map.get("PRIVATED");
      memberCount = Integer.parseInt(String.valueOf(map.get("MEMBER_COUNT")));
    }

    Room room = new Room(roomId, roomName, createdUserId, directed, privated, memberCount);

    return room;
  }

  /**
   * ダイレクト用の追加部屋情報取得.
   *
   * @param room   ルーム情報
   * @param userId ユーザID
   * @return ダイレクト用の追加部屋情報
   */
  public Room getDirect(Room room, String userId) {

    final String sql = "SELECT U.USERNAME AS ROOMNAME FROM JOINROOM R JOIN USERS U ON R.USERID = U.USERID WHERE R.USERID <> :userId AND ROOMID = :roomId";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    params.put("roomId", room.roomId());

    List<Map<String, Object>> resultGetDirectRoom = jdbc.queryForList(sql, params);

    String roomName = "";
    for (Map<String, Object> map : resultGetDirectRoom) {
      roomName = (String) map.get("ROOMNAME");
    }

    Room direct = new Room(
      room.roomId(),
      roomName,
      room.createdUserId(),
      true,
      true,
      2 // 2人で固定
    );

    return direct;
  }

  /**
   * ユーザが紐づくルーム情報リスト取得.
   *
   * @param userId ユーザID
   * @return ルーム情報リスト
   */
  public ArrayList<Room> getRoomList(String userId) {

    final String sql = "SELECT R.ROOMID, R.ROOMNAME, R.CREATEDUSERID, R.DIRECTED, R.PRIVATED FROM JOINROOM J JOIN ROOMS R ON J.ROOMID = R.ROOMID WHERE J.USERID = :userId AND R.DIRECTED = FALSE ORDER BY R.ROOMNAME ASC";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    List<Map<String, Object>> resultList = jdbc.queryForList(sql, params);

    ArrayList<Room> roomlist = new ArrayList<Room>();

    for (Map<String, Object> map : resultList) {
      String roomId = (String) map.get("ROOMID");
      String roomName = (String) map.get("ROOMNAME");
      String createdUserId = (String) map.get("CREATEDUSERID");
      boolean directed = (boolean) map.get("DIRECTED");
      boolean privated = (boolean) map.get("PRIVATED");
      roomlist.add(new Room(roomId, roomName, createdUserId, directed, privated, 0));
    }

    return roomlist;
  }

  /**
   * ユーザが紐づくダイレクトルーム情報リスト取得.
   *
   * @param userId ユーザID
   * @return ダイレクトルーム情報リスト
   */
  public ArrayList<Room> getDirectList(String userId) {

    final String sql = "SELECT R.ROOMID, U.USERNAME AS ROOMNAME FROM JOINROOM R JOIN USERS U ON R.USERID = U.USERID WHERE R.USERID <> :userId1 AND ROOMID IN (SELECT R.ROOMID FROM JOINROOM J JOIN ROOMS R ON J.ROOMID = R.ROOMID WHERE J.USERID = :userId2 AND R.DIRECTED = TRUE) ORDER BY R.USERID";

    Map<String, Object> params = new HashMap<>();
    params.put("userId1", userId);
    params.put("userId2", userId);

    List<Map<String, Object>> resultList = jdbc.queryForList(sql, params);

    ArrayList<Room> roomlist = new ArrayList<Room>();

    for (Map<String, Object> map : resultList) {
      String roomId = (String) map.get("ROOMID");
      String roomName = (String) map.get("ROOMNAME");
      roomlist.add(new Room(roomId, roomName));
    }

    return roomlist;
  }

  /**
   * チャットログ保存.
   *
   * @param roomId  部屋ID
   * @param userId  ユーザID
   * @param message メッセージ
   * @param imgpath
   * @return 保存結果
   */
  public boolean saveChatlog(String roomId, String userId, String message, String imgpath) {

    final String sql = "INSERT INTO CHATLOG (CHATLOGID, ROOMID, USERID, MESSAGE, CREATED_AT, IMGPATH) VALUES (nextval('CHATLOGID_SEQ'), :roomId, :userId, :message, CURRENT_TIMESTAMP, :imgpath)";

    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);
    params.put("userId", userId);
    params.put("message", message);
    params.put("imgpath", imgpath);

    int row = jdbc.update(sql, params);

    if (row != AppConstants.EXPECTED_UPDATE_COUNT) {
      // 更新件数が異常な場合
      throw new IncorrectResultSizeDataAccessException( "更新に失敗しました", AppConstants.EXPECTED_UPDATE_COUNT);
    }

    return true;
  }

  public ArrayList<Room> getRoomList2(String userId) {

    final String sql = "SELECT R.ROOMID, R.ROOMNAME, R.CREATEDUSERID, R.DIRECTED, R.PRIVATED FROM ROOMS R LEFT JOIN JOINROOM J ON R.ROOMID = J.ROOMID AND J.USERID = :userId WHERE R.PRIVATED = false AND J.ROOMID IS NULL ORDER BY R.ROOMNAME ASC";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    List<Map<String, Object>> resultList = jdbc.queryForList(sql, params);

    ArrayList<Room> roomlist = new ArrayList<Room>();

    for (Map<String, Object> map : resultList) {
      String roomId = (String) map.get("ROOMID");
      String roomName = (String) map.get("ROOMNAME");
      String createdUserId = (String) map.get("CREATEDUSERID");
      boolean directed = (boolean) map.get("DIRECTED");
      boolean privated = (boolean) map.get("PRIVATED");
      roomlist.add(new Room(roomId, roomName, createdUserId, directed, privated, 0));
    }

    return roomlist;
  }

  /**
   * ユーザの特定ルームにおける未読メッセージ数を取得
   * room_last_reatテーブルに記録された最終閲覧時刻
   * 一度もログインしていないユーザは全権未読扱い
   * @param userId
   * @param roomId
   * @return 未読メッセージ数
   */
  public int getUnreadCount(String userId, String roomId) {

    // 最後にルームを閲覧した時間以降のメッセージ数を取得
    String sql =
        "SELECT COUNT(*) FROM chatlog WHERE roomid = ? AND created_at > COALESCE((SELECT last_read_at FROM room_last_read WHERE user_id = ? AND room_id = ?), '1970-01-01 00:00:00') AND userid != ?";

    try {
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
          roomId, userId, roomId, userId);
      return count != null ? count : 0; //

    } catch (Exception e) {
      e.printStackTrace();
      return 0; // エラー時は0を返して「未読０件」として処理を継続
    }
  }

  /**
   * ユーザの全ルームにおける未読メッセージ数をmapで取得
   *
   * @param userID
   * @return ルームIDをキー、未読数を値とするマップ
   */
  public Map<String, Integer> getUnreadCountMap(String userId) {

    // 自分以外のユーザが送信した未読メッセージ数を取得
    String sql =
    "SELECT rm.roomid AS room_id, SUM(CASE WHEN c.userid != ? AND c.created_at > COALESCE(r.last_read_at, '1970-01-01 00:00:00') THEN 1 ELSE 0 END) AS unread_count FROM joinroom rm LEFT JOIN chatlog c ON c.roomid = rm.roomid LEFT JOIN room_last_read r ON r.room_id = rm.roomid AND r.user_id = ? WHERE rm.userid = ? GROUP BY rm.roomid;";

    try {
      Map<String, Integer> unreadMap = new HashMap<>();
      jdbcTemplate.query(sql, rs -> {
        // 各行のroom_Idとunread_countをmapに追加
        unreadMap.put(rs.getString("room_id"), rs.getInt("unread_count"));
      }, userId, userId, userId);
      return unreadMap;

    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap<>(); // エラー時は空のMapを返して既存機能を継続
    }
  }

  /**
   * ユーザが特定ルームを閲覧したことを記録(既読処理)
   * すでにレコードが存在する場合はlast_read_atを現在時刻に更新
   *
   * @param userId
   * @param roomId
   * @return 更新成功時true
   */
  public boolean updateLastRead(String userId, String roomId) {
    
    // ルームを閲覧した時刻を更新する
    String sql =
    "INSERT INTO room_last_read (user_id, room_id, last_read_at) VALUES (?, ?, CURRENT_TIMESTAMP) ON CONFLICT (user_id, room_id) DO UPDATE SET last_read_at = CURRENT_TIMESTAMP";

    try {
      int result = jdbcTemplate.update(sql, userId, roomId);
      return result > 0;

    } catch (Exception e) {
      e.printStackTrace();
      return false; // エラーでも既存機能は継続
    }
  }
}
