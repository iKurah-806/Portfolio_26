package com.swack.hcs.repository;

import com.swack.hcs.bean.UserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {

  @Autowired
  private NamedParameterJdbcTemplate jdbc;

  @Autowired
  private PasswordEncoder passwordEncoder;

   //全ユーザー取得 (自分を除外)
  public List<UserData> findAll(String currentUserId) {

    final String sql = """
        SELECT USERID, USERNAME, MAILADDRESS, PASSWORD, USERIMGPATH, ROLE,
              FAILED_ATTEMPTS, ACCOUNT_LOCKED, LAST_LOGIN_AT
        FROM USERS
        WHERE
            ENABLED = 'Y'
            AND USERID != :currentUserId -- ★ 1. SQLで :currentUserId を使用
        ORDER BY
            USERNAME ASC
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("currentUserId", currentUserId);

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


  /** 全ユーザーのユーザー名一覧取得 */
  public List<String> getUsernames() {

    final String sql = "SELECT USERNAME FROM USERS WHERE ENABLED = 'Y'";

    return jdbc.query(sql, (rs, rowNum) -> rs.getString("USERNAME"));
  }


  /** サインアップ */
  public int signup(String username, String mailAddress, String password) {

    final String doubleCheckSql = "SELECT USERNAME FROM USERS WHERE MAILADDRESS = :mailAddress";

    final String insertSql = """
        INSERT INTO USERS (USERID, USERNAME, MAILADDRESS, PASSWORD, FAILED_ATTEMPTS, ACCOUNT_LOCKED)
        VALUES('U' || LPAD(nextval('userid_seq')::TEXT, 4, '0'), :username, :mailAddress, :password, 0, 'N')
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("username", username);
    params.put("mailAddress", mailAddress);
    params.put("password", passwordEncoder.encode(password));

    if (!jdbc.queryForList(doubleCheckSql, params).isEmpty()) {
      return 0;
    }

    return jdbc.update(insertSql, params);
  }

  /** メールアドレスでユーザー取得 */
  public UserData selectOne(String mailAddress) {

    final String sql = """
        SELECT USERID, USERNAME, USERIMGPATH, ROLE, PASSWORD, FAILED_ATTEMPTS, ACCOUNT_LOCKED, LAST_LOGIN_AT
        FROM USERS
        WHERE MAILADDRESS = :mailAddress
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("mailAddress", mailAddress);

    List<Map<String, Object>> results = jdbc.queryForList(sql, params);
    if (results.size() != 1)
      return null;

    Map<String, Object> row = results.get(0);
    return new UserData(
        (String) row.get("USERID"),
        (String) row.get("USERNAME"),
        mailAddress,
        (String) row.get("PASSWORD"),
        (String) row.get("USERIMGPATH"),
        (String) row.get("ROLE"),
        row.get("FAILED_ATTEMPTS") != null ? ((Number) row.get("FAILED_ATTEMPTS")).intValue() : 0,
        (String) row.get("ACCOUNT_LOCKED"),
        (Timestamp) row.get("LAST_LOGIN_AT"));
  }


  /** ログイン */
  public UserData login(String mailAddress, String password) {

    UserData user = selectOne(mailAddress);

    if (user == null)
      return null;
    if (!passwordEncoder.matches(password, user.password()))
      return null;
    return user;
  }


  /** ロールチェック */
  public boolean roleCheck(String userId) {

    final String ROLE_CHECK = "SELECT role FROM users WHERE userId = :userId";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    String role = jdbc.queryForObject(ROLE_CHECK, params, String.class);

    return "ADMIN".equalsIgnoreCase(role);
  }


  /** ログイン失敗カウントアップ */
  public void incrementFailedLogin(String mailAddress) {

    final String sql = """
            UPDATE USERS
        SET FAILED_ATTEMPTS = FAILED_ATTEMPTS + 1,
            ACCOUNT_LOCKED = CASE WHEN FAILED_ATTEMPTS + 1 >= 5 THEN 'Y' ELSE ACCOUNT_LOCKED END
        WHERE MAILADDRESS = :mailAddress;

        """;

    Map<String, Object> params = new HashMap<>();
    params.put("mailAddress", mailAddress);

    jdbc.update(sql, params);
  }

  /** ログイン成功時リセット */
  public void resetFailedLogin(String mailAddress) {

    final String sql = """
            UPDATE USERS
            SET FAILED_ATTEMPTS = 0,
                ACCOUNT_LOCKED = 'N'
            WHERE MAILADDRESS = :mailAddress
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("mailAddress", mailAddress);

    jdbc.update(sql, params);
  }


  /** アカウントロック */
  public void lockUser(String mailAddress) {

    final String sql = "UPDATE USERS SET ACCOUNT_LOCKED = 'Y' WHERE MAILADDRESS = :mailAddress";

    Map<String, Object> params = new HashMap<>();
    params.put("mailAddress", mailAddress);

    jdbc.update(sql, params);
  }


  /** アンロック（管理者用） */
  public void unlockUser(String mailAddress) {

    final String sql = """
            UPDATE USERS
            SET ACCOUNT_LOCKED = 'N',
                FAILED_ATTEMPTS = 0
            WHERE MAILADDRESS = :mailAddress
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("mailAddress", mailAddress);

    jdbc.update(sql, params);
  }


  /** 最終ログイン日時更新 */
  public void updateLastLogin(String mailAddress) {

    final String sql = "UPDATE USERS SET LAST_LOGIN_AT = now() WHERE MAILADDRESS = :mailAddress";

    Map<String, Object> params = new HashMap<>();
    params.put("mailAddress", mailAddress);

    jdbc.update(sql, params);
  }


  /** ユーザー削除（管理者権限チェック含む） */
  public int deleteUser(String targetUserId, String currentUserId) {

    final String sqlDelete = "DELETE FROM USERS WHERE USERID = :targetUserId";

    Map<String, Object> deleteParams = new HashMap<>();
    deleteParams.put("targetUserId", targetUserId);

    return jdbc.update(sqlDelete, deleteParams);
  }


  // 一括削除
  public int bulkDeleteUsers(List<String> userIds, String currentUserId) {

    if (userIds == null || userIds.isEmpty()) {
      return 0;
    }

    final String DELETEFROMUSERS = "DELETE FROM USERS WHERE USERID IN (:userIds)";
    final String DELETEFROMJOINROOM = "DELETE FROM JOINROOM WHERE USERID IN (:userIds)";
    final String DELETEFROMCHATLOG = "DELETE FROM CHATLOG WHERE USERID IN (:userIds)";

    Map<String, Object> params = new HashMap<>();
    params.put("userIds", userIds);

    jdbc.update(DELETEFROMJOINROOM, params);
    jdbc.update(DELETEFROMCHATLOG, params);

    return jdbc.update(DELETEFROMUSERS, params);
  }


  /** 管理者判定 */
  public boolean isAdmin(String userId) {

    final String sql = "SELECT ROLE FROM USERS WHERE USERID = :userId";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    List<Map<String, Object>> results = jdbc.queryForList(sql, params);

    if (results.isEmpty())
      return false;

    String role = (String) results.get(0).get("ROLE");

    return "ADMIN".equalsIgnoreCase(role);
  }


  /** USERIDでユーザー取得 */
  public UserData selectOneByUserId(String userId) {

    final String sql = """
        SELECT USERID, USERNAME, MAILADDRESS, PASSWORD, USERIMGPATH, ROLE,
              FAILED_ATTEMPTS, ACCOUNT_LOCKED, LAST_LOGIN_AT
        FROM USERS
        WHERE USERID = :userId
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    List<Map<String, Object>> results = jdbc.queryForList(sql, params);

    if (results.size() != 1)
      return null;

    Map<String, Object> row = results.get(0);
    return new UserData(
        (String) row.get("USERID"),
        (String) row.get("USERNAME"),
        (String) row.get("MAILADDRESS"),
        (String) row.get("PASSWORD"),
        (String) row.get("USERIMGPATH"),
        (String) row.get("ROLE"),
        row.get("FAILED_ATTEMPTS") != null ? ((Number) row.get("FAILED_ATTEMPTS")).intValue() : 0,
        (String) row.get("ACCOUNT_LOCKED"),
        (Timestamp) row.get("LAST_LOGIN_AT"));
  }


     //パスワード更新（ユーザーID指定）
  public int updatePasswordById(String userId, String hashedPassword) {

    final String sql = "UPDATE USERS SET PASSWORD = :password WHERE USERID = :userId";
    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    params.put("password", hashedPassword);

    return jdbc.update(sql, params);
  }


  /** USERIDでアンロック */
  public void unlockUserById(String userId) {

    final String sql = "UPDATE USERS SET ACCOUNT_LOCKED = 'N', FAILED_ATTEMPTS = 0 WHERE USERID = :userId";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);

    jdbc.update(sql, params);
  }

  // プロフィール画像更新
  public int updateProfileImg(String userId, String userImgPath) {

    final String sql = "UPDATE users SET userImgPath = :userImgPath WHERE userId = :userId";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    params.put("userImgPath", userImgPath);
    jdbc.update(sql, params);

    return jdbc.update(sql, params);
  }

  // 権限変更
  public int updateRole(String userId, String role) {

    final String sql = "UPDATE USERS SET ROLE = :role WHERE USERID = :userId";

    Map<String, Object> params = new HashMap<>();
    params.put("userId", userId);
    params.put("role", role);

    return jdbc.update(sql, params);
  }
}
