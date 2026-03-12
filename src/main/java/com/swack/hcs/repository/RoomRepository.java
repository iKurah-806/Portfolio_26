package com.swack.hcs.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.swack.hcs.bean.UserData;
import com.swack.hcs.util.AppConstants;

/**
 * ルーム機能に関するDBアクセスを行う.
 */
@Repository
public class RoomRepository {

  @Autowired
  private NamedParameterJdbcTemplate jdbc;


  public boolean doubleCheckOnJoinroom(String userId, String roomId) {

    final String sqlDoubleCheck = "SELECT ROOMID FROM JOINROOM WHERE ROOMID = :roomId AND USERID = :userId";

    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);
    params.put("userId", userId);

    List<Map<String, Object>> resultList = jdbc.queryForList(sqlDoubleCheck, params);

    if (resultList.size() > 0) {
      return false;
    }
    return true;
  }


  public boolean ifUserExistInRoom(String roomId, String userId) {

    final String IFEXIST = "SELECT * FROM JOINROOM WHERE USERID = :userId AND ROOMID = :roomId";

    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);
    params.put("userId", userId);

    List<Map<String, Object>> resultList = jdbc.queryForList(IFEXIST, params);
    if (resultList.size() > 0) {
      return true;
    }

    return false;
  }


  public String getRoomIdByName(String roomName) {

    final String findIt = "SELECT ROOMID FROM ROOMS WHERE ROOMNAME = :roomName";

    Map<String, Object> params = new HashMap<>();
    params.put("roomName", roomName);

    return jdbc.queryForObject(findIt, params, String.class);
  }


  public int addMemberToJoinroom(String userId, String roomId) {

    final String sqlAddMember = "INSERT INTO JOINROOM(ROOMID, USERID) VALUES(:roomId, :userId)";

    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);
    params.put("userId", userId);

    return jdbc.update(sqlAddMember, params);
  }


  public String getDirectRoomIdByUserName(String loginedUserId, String userId) {

    final String DOUBLE_CHECK = "SELECT ROOMID FROM ROOMS WHERE roomname = 'P' || :loginedUserId || ',' || :userId OR roomname = 'P' || :userId || ',' || :loginedUserId";

    Map<String, Object> params = new HashMap<>();
    params.put("loginedUserId", loginedUserId);
    params.put("userId", userId);

    List<Map<String, Object>> resultList = jdbc.queryForList(DOUBLE_CHECK, params);

    return (String) resultList.get(0).get("ROOMID");
  }


  public boolean addMemberToDirect(String loginedUserId, String userId) {

    // 部屋を作成して roomid を返す
    final String INSERT_DIRECT_ROOM = """
            INSERT INTO rooms (roomid, roomname, createduserid, directed, privated)
            VALUES ('R' || LPAD(nextval('roomid_seq')::TEXT, 4, '0'),
                    'P' || :loginedUserId || ',' || :userId,
                    :loginedUserId, true, true)
            RETURNING roomid
        """;

    final String INSERT_JOINROOM = """
            INSERT INTO JOINROOM (roomid, userid)
            VALUES (:roomid, :loginedUserId),
                  (:roomid, :userId)
        """;

    final String DOUBLE_CHECK = """
            SELECT * FROM rooms
            WHERE roomname = 'P' || :loginedUserId || ',' || :userId
              OR roomname = 'P' || :userId || ',' || :loginedUserId
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("loginedUserId", loginedUserId);
    params.put("userId", userId);

    List<Map<String, Object>> resultList = jdbc.queryForList(DOUBLE_CHECK, params);

    if (!resultList.isEmpty()) {
      return false;
    }

    String roomId = jdbc.queryForObject(INSERT_DIRECT_ROOM, params, String.class);
    params.put("roomid", roomId);

    int inserted2 = jdbc.update(INSERT_JOINROOM, params);

    return inserted2 == 2;
  }


  /**
   * room作成の実行.
   */
  public boolean createRoom(String roomname, String createduserid, boolean privated) {

    final String INSERT_ROOM = "INSERT INTO rooms (roomid, roomname, createduserid, privated) VALUES('R' || LPAD(nextval('roomid_seq')::TEXT, 4, '0'), :roomname, :createduserid, :privated)"
        + "RETURNING roomid";

    Map<String, Object> roomParams = new HashMap<>();
    roomParams.put("roomname", roomname);
    roomParams.put("createduserid", createduserid);
    roomParams.put("privated", privated);

    // RETURNING roomidで帰ってきたroomidを変数に入れる
    String newRoomId = jdbc.queryForObject(INSERT_ROOM, roomParams, String.class);

    Map<String, Object> joinParams = new HashMap<>();
    joinParams.put("roomname", roomname);
    joinParams.put("createduserid", createduserid);
    joinParams.put("privated", privated);

    int row = addMemberToJoinroom(createduserid, newRoomId);

    if (row != AppConstants.EXPECTED_UPDATE_COUNT) {
      throw new IncorrectResultSizeDataAccessException(
          "更新に失敗しました",
          AppConstants.EXPECTED_UPDATE_COUNT);
    }

    return true;
  }

  public boolean RoomDoubleCheck(String roomname) {

    final String DOUBLE_CHECK = "SELECT * FROM ROOMS WHERE ROOMNAME = :roomName";

    Map<String, Object> params = new HashMap<>();
    params.put("roomName", roomname);

    List<Map<String, Object>> resultList = jdbc.queryForList(DOUBLE_CHECK, params);

    if (resultList.size() > 0) {
      return false;
    }

    return true;
  }


  public List<UserData> findNotInRoom(String roomId) {

    final String sql = """
            SELECT u.USERID, u.USERNAME, u.MAILADDRESS, u.PASSWORD, u.USERIMGPATH, u.ROLE,
                  u.FAILED_ATTEMPTS, u.ACCOUNT_LOCKED, u.LAST_LOGIN_AT
            FROM USERS u
            WHERE u.ENABLED = 'Y'
              AND u.USERID NOT IN (
                  SELECT j.USERID
                  FROM JOINROOM j
                  WHERE j.ROOMID = :roomId
              )
            ORDER BY u.USERNAME ASC
        """;

    // パラメータマップ
    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);

    // jdbc.query で List<UserData> を返す
    return jdbc.query(sql, params, (rs, rowNum) -> new UserData(
        rs.getString("USERID"),
        rs.getString("USERNAME"),
        rs.getString("MAILADDRESS"),
        rs.getString("PASSWORD"),
        rs.getString("USERIMGPATH"),
        rs.getString("ROLE"),
        rs.getInt("FAILED_ATTEMPTS"),
        rs.getString("ACCOUNT_LOCKED"),
        rs.getTimestamp("LAST_LOGIN_AT")));
  }


  public List<UserData> selectUsersWithoutDirect(String userId) {

    final String sql = """
        SELECT USERID, USERNAME, USERIMGPATH, ROLE, PASSWORD,
              FAILED_ATTEMPTS, ACCOUNT_LOCKED, LAST_LOGIN_AT, MAILADDRESS
        FROM USERS u
        WHERE u.USERID != :userId
          AND NOT EXISTS (
                SELECT 1 FROM rooms r
                WHERE (
                    r.roomname = 'P' || :userId || ',' || u.USERID
                    OR r.roomname = 'P' || u.USERID || ',' || :userId
                )
          )
        ORDER BY USERID
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

    List<UserData> result = new ArrayList<>();

    for (Map<String, Object> row : rows) {
      UserData user = new UserData(
          (String) row.get("USERID"),
          (String) row.get("USERNAME"),
          (String) row.get("MAILADDRESS"),
          (String) row.get("PASSWORD"),
          (String) row.get("USERIMGPATH"),
          (String) row.get("ROLE"),
          row.get("FAILED_ATTEMPTS") != null ? ((Number) row.get("FAILED_ATTEMPTS")).intValue() : 0,
          (String) row.get("ACCOUNT_LOCKED"),
          (Timestamp) row.get("LAST_LOGIN_AT"));
      result.add(user);
    }

    return result;
  }


  public List<UserData> selectUsersInRoom(String roomId) {

    final String sql = """
        SELECT
            u.USERID, u.USERNAME, u.MAILADDRESS, u.PASSWORD,
            u.USERIMGPATH, u.ROLE,
            u.FAILED_ATTEMPTS, u.ACCOUNT_LOCKED, u.LAST_LOGIN_AT
        FROM USERS u
        JOIN JOINROOM j ON u.USERID = j.USERID
        WHERE j.ROOMID = :roomId
        ORDER BY u.USERNAME ASC
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("roomId", roomId);

    List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

    List<UserData> result = new ArrayList<>();

    for (Map<String, Object> row : rows) {
      UserData user = new UserData(
          (String) row.get("USERID"),
          (String) row.get("USERNAME"),
          (String) row.get("MAILADDRESS"),
          (String) row.get("PASSWORD"),
          (String) row.get("USERIMGPATH"),
          (String) row.get("ROLE"),
          row.get("FAILED_ATTEMPTS") != null ? ((Number) row.get("FAILED_ATTEMPTS")).intValue() : 0,
          (String) row.get("ACCOUNT_LOCKED"),
          (Timestamp) row.get("LAST_LOGIN_AT"));
      result.add(user);
    }

    return result;
  }
}
