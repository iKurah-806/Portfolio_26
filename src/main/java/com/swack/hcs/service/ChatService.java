package com.swack.hcs.service;

import com.swack.hcs.bean.ChatLog;
import com.swack.hcs.bean.Room;
import com.swack.hcs.repository.ChatRepository;
import com.swack.hcs.repository.ReactionRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/**
 * チャット機能を実行するサービスクラス.
 * チャットルームの取得、チャットログの保存、編集、削除の管理などを行います。
 */
@Transactional
@Service
public class ChatService {

  @Autowired
  private ChatRepository chatRepository;


  @Autowired
  private ReactionRepository reactionRepository;

  /**
   * 指定されたルームIDとユーザーIDに基づいたチャットルーム情報取得.
   *
   * @param roomId チャットルームのID
   * @param userId ユーザーのID
   * @return 取得したチャットルームの情報
   */
  public Room getRoom(String roomId, String userId) {
    Room room = chatRepository.getRoom(roomId);

    if (room.directed()) {
      // ダイレクト用の追加部屋情報を取得
      room = chatRepository.getDirect(room, userId);
    }

    return room;
  }

  /**
   * 指定されたユーザーIDに関連するすべてのチャットルームリスト取得.
   *
   * @param userId ユーザーのID
   * @return チャットルームのリスト
   */
  public ArrayList<Room> getRoomList(String userId) {
    return chatRepository.getRoomList(userId);
  }

  /**
   * 指定されたユーザーIDに関連するダイレクトチャットリスト取得.
   *
   * @param userId ユーザーのID
   * @return ダイレクトチャットのリスト
   */
  public ArrayList<Room> getDirectList(String userId) {
    return chatRepository.getDirectList(userId);
  }

  /**
   * 指定されたユーザーIDとルームIDに基づいたチャットログリスト取得.
   *
   * @param userId ユーザーのID
   * @param roomId チャットルームのID
   * @return チャットログのリスト
   */
  public List<ChatLog> getChatlogList(String userId, String roomId) {
    List<ChatLog> chatLogList = chatRepository.getChatlogList(roomId);

    return chatLogList;
  }

  /**
   * チャットログ保存.
   *
   * @param roomId  チャットルームのID
   * @param userId  ユーザーのID
   * @param message メッセージの内容
   */
  public void saveChatLog(String roomId, String userId, String message, String imgPath) {
    chatRepository.saveChatlog(roomId, userId, message, imgPath);
  }

  public ArrayList<Room> getRoomList2(String userId) {
    return chatRepository.getRoomList2(userId);
  }


  /**
   * メッセージ削除
   * @param chatLogId
   * @return
   */
  public boolean deleteMessage(int chatLogId, String imageUrl) throws IOException {

    //存在する場合
    if (imageUrl != null && !imageUrl.isEmpty()) {
      deleteMsgImg(imageUrl);
    }

    reactionRepository.deleteReactionByChatLogId(chatLogId);

    System.out.println("deleteMessageまできたどん");
    return chatRepository.deleteMessage(chatLogId);
  }

  /**
   * メッセージ画像削除
   * @param imageUrl
   * @return
   */
  public boolean deleteMsgImg(String imageUrl) throws IOException {

    if (imageUrl == null || imageUrl.isEmpty()){
      return false;
    }

    // ファイル名だけ抽出
    String fileName = imageUrl.replace("/images/chatLog/", "");
    int index = fileName.indexOf("?");

    // クエリパラメータを消す
    if (index != -1) {
      fileName = fileName.substring(0, index);
    }

    if (fileName.isEmpty()) {
      return false;
    }

    Path path = Paths.get("uploads/chat/" + fileName);
    return Files.deleteIfExists(path);
  }

  /**
   * メッセージ編集
   * @param chatLogId
   * @param newMessage
   * @return
   */
  public boolean updateMessage(int chatLogId, String newMessage) {
    return chatRepository.updateMessage(chatLogId, newMessage);
  }


  /**
   * メッセージに星つける
   * @param chatLogId
   * @return ★つきメッセージ
   */
  public boolean starMessage(int chatLogId) {
    return chatRepository.starMessage(chatLogId);
  }


  /**
   * 星付きメッセージの一覧表示
   * @param userId
   * @return 一覧
   */
  public List<ChatLog> getStarredChatlogList(String userId) {
    return chatRepository.getStarredChatlogList(userId);
  }


  /**
   * 未読メッセージ
   *
   * @param userId
   * @param roomId
   * @return 未読メッセージ数
   */
  public int getUnreadCount(String userId, String roomId) {
    return chatRepository.getUnreadCount(userId, roomId);
  }


  /**
   * 全ルームの未読数を一括取得
   *
   * @param userId
   * @return roomidをキー、未読数を値とするmap
   */
  public Map<String, Integer> getUnreadCountMap(String userId) {
    return chatRepository.getUnreadCountMap(userId);
  }


  /**
   * 既読処理
   *
   * @param userId
   * @param roomId
   * @return 更新成功時true
   */
  public boolean markAsRead(String userId, String roomId) {
    return chatRepository.updateLastRead(userId, roomId);
  }
}
