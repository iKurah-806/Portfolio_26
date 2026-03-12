
package com.swack.hcs.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import com.swack.hcs.bean.MessageReactionData;

@Repository
public class ReactionRepository {

  @Autowired
  private NamedParameterJdbcTemplate jdbc;

  // --- reactionid を emojiname から取得 ---
  @SuppressWarnings("null")
  public Integer findReactionId(String emojiName) {
    final String sql = """
            SELECT reactionid
            FROM reactions
            WHERE emojiname = :emojiname
        """;

    Map<String, Object> params = Map.of("emojiname", emojiName);

    try {
      return jdbc.queryForObject(sql, params, Integer.class);

    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  // --- すでにリアクションしているかチェック ---
  public boolean exists(int chatlogId, String userId, int reactionId) {

    final String sql = """
            SELECT COUNT(*)
            FROM message_reactions
            WHERE chatlogid = :chatlogid
              AND userid    = :userid
              AND reactionid = :reactionid
        """;

    Map<String, Object> params = Map.of(
        "chatlogid", chatlogId,
        "userid", userId,
        "reactionid", reactionId);

    @SuppressWarnings("null")
    Integer count = jdbc.queryForObject(sql, params, Integer.class);

    return count != null && count > 0;
  }

  // --- リアクション追加 (INSERT) ---
  @SuppressWarnings("null")
  public int addReaction(int chatlogId, String userId, int reactionId) {

    final String sql = """
            INSERT INTO message_reactions (chatlogid, userid, reactionid)
            VALUES (:chatlogid, :userid, :reactionid)
        """;

    Map<String, Object> params = Map.of(
        "chatlogid", chatlogId,
        "userid", userId,
        "reactionid", reactionId);

    return jdbc.update(sql, params);
  }

  // --- リアクション解除 (DELETE) ---
  @SuppressWarnings("null")
  public int removeReaction(int chatlogId, String userId, int reactionId) {

    final String sql = """
            DELETE FROM message_reactions
            WHERE chatlogid = :chatlogid
              AND userid = :userid
              AND reactionid = :reactionid
        """;

    Map<String, Object> params = Map.of(
        "chatlogid", chatlogId,
        "userid", userId,
        "reactionid", reactionId);

    return jdbc.update(sql, params);
  }

  // --- トグル（追加 or 削除）---
  public boolean toggleReaction(int chatlogId, String userId, String emojiName) {

    Integer reactionId = findReactionId(emojiName);
    if (reactionId == null)
      return false;

    if (exists(chatlogId, userId, reactionId)) {
      // 解除
      removeReaction(chatlogId, userId, reactionId);
      return false; // false = OFFになった
    } else {
      addReaction(chatlogId, userId, reactionId);
      return true; // true = ONになった
    }
  }


  @SuppressWarnings("null")
  public List<MessageReactionData> findReactionsByChatlogIds(List<Integer> chatlogIds) {

    final String sql = """
            SELECT mr.chatlogid, r.emojiname, COUNT(*) AS cnt
            FROM message_reactions mr
            JOIN reactions r ON mr.reactionid = r.reactionid
            WHERE mr.chatlogid IN (:chatlogIds)
            GROUP BY mr.chatlogid, r.emojiname
            ORDER BY mr.chatlogid, r.emojiname
        """;

    Map<String, Object> params = Map.of("chatlogIds", chatlogIds);

    return jdbc.query(sql, params, (rs, rowNum) -> new MessageReactionData(
        rs.getInt("chatlogid"),
        rs.getString("emojiname"),
        rs.getInt("cnt")));
  }


  public boolean deleteReactionByChatLogId(int chatLogId) {
    final String sql = """
            DELETE FROM message_reactions
            WHERE chatlogid = :chatLogId
        """;

    Map<String, Object> params = new HashMap<>();
    params.put("chatLogId", chatLogId);

    jdbc.update(sql, params);

    // 削除対象がなくても OK
    return true;
  }

}
