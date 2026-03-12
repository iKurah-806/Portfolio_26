package com.swack.hcs.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swack.hcs.bean.ChatLog;
import com.swack.hcs.bean.MessageReactionData;
import com.swack.hcs.bean.Room;
import com.swack.hcs.bean.UserData;
import com.swack.hcs.service.ChatService;
import com.swack.hcs.service.LoginService;
import com.swack.hcs.service.ReactionService;
import com.swack.hcs.service.RoomService;
import com.swack.hcs.service.UserService;
import com.swack.hcs.util.Loggable;

import jakarta.servlet.http.HttpSession;


/**
 * メインコントローラ.
 */
@Controller
public class MainController implements Loggable {

  @Autowired
  private LoginService loginService;

  @Autowired
  private ChatService chatService;

  @Autowired
  private UserService userService;

  @Autowired
  private RoomService roomService;

  @Autowired
  private ReactionService reactionService;

  /** セッション情報 */
  @Autowired
  HttpSession session;



  /**
   * メイン画面表示.
   *
   * @param roomId 部屋ID
   * @param model  モデル
   * @return メイン画面
   */
  @GetMapping("/")
  public String chat(
    @RequestParam(name = "roomId", required = false) String roomId,
      Model model) {

    UserData user = loginService.getLoginedUserInfo();


    // roomId が未指定の場合は初期ルーム R0000
    if (roomId == null || roomId.isEmpty()) {
      roomId = "R0000"; // 初回ログイン時や未指定の場合
    }

    // ユーザーがアクセスできるルームか確認（セキュリティ）
    Room room = chatService.getRoom(roomId, user.userId());
    if (room == null) {
      // アクセス不可のroomIdだった場合も初期ルームにフォールバック
      roomId = "R0000";
      room = chatService.getRoom(roomId, user.userId());
    }

    String userImgPath = user.userImgPath();
    if (userImgPath == null) {
      // userImgPathに値が入っていない場合、初期アイコン
      userImgPath = "/images/profile/profile.png";
    }

    List<UserData> userList = userService.selectAll(user.userId());
    List<UserData> userListNotInRoom = roomService.findNotInRoom(roomId);
    List<UserData> userListWithoutDirect = roomService.selectUsersWithoutDirect(user.userId());

    List<Room> roomList = chatService.getRoomList(user.userId());
    List<Room> directList = chatService.getDirectList(user.userId());
    List<ChatLog> chatLogList = chatService.getChatlogList(user.userId(),
        roomId);
    // 未読管理
    Map<String, Integer> unreadMap = new HashMap<>();
    try {
      // 全ルームの未読数を一括取得
      unreadMap = chatService.getUnreadCountMap(user.userId());
      log().info("未読数を取得しました: " + unreadMap.size() + "件");

      // マップの中身を詳細に出力
      log().info("===== 未読数マップの詳細 =====");
      unreadMap.forEach((rid, count) -> {
        log().info("  RoomID: " + rid + " => 未読数: " + count);
      });
      log().info("============================");
    } catch (Exception e) {
      log().error("未読数の取得に失敗しました: " + e.getMessage());
      // エラーでも既存機能は継続（空のMapを使用）
    }

    try {
      // 現在のルームを既読にする
      chatService.markAsRead(user.userId(), roomId);
      log().info("既読処理を実行: roomId=" + roomId);
    } catch (Exception e) {
      log().error("既読処理に失敗しました: " + e.getMessage());
      // エラーでも既存機能は継続
    }

    List<Integer> chatLogIds = chatLogList.stream()
                                          .map(ChatLog::getChatLogId)
                                          .toList();
    Map<Integer, List<MessageReactionData>> reactionMap =
        reactionService.getReactionsForMessages(chatLogIds);

    List<UserData> roomUserList = roomService.selectUserInRoom(roomId);
    model.addAttribute("roomUserList", roomUserList);

    model.addAttribute("user", user);
    model.addAttribute("room", room);
    model.addAttribute("userImgPath", userImgPath);
    model.addAttribute("roomList", roomList);

    model.addAttribute("userList", userList);
    model.addAttribute("userListNotInRoom", userListNotInRoom);
    model.addAttribute("userListWithoutDirect", userListWithoutDirect);

    model.addAttribute("directList", directList);
    model.addAttribute("chatLogList", chatLogList);
    model.addAttribute("unreadMap", unreadMap);


    model.addAttribute("userListWithoutDirect", userListWithoutDirect);

    model.addAttribute("reactionMap", reactionMap);

    return "main";
  }

  /**
   * チャット送信.
   *
   * @param roomId  部屋ID
   * @param message メッセージ
   * @param image 添付画像
   * @return メイン画面
   */
  @PostMapping("/main")
  public String save(
    @RequestParam(name = "roomId") String roomId,
    @RequestParam(name = "message") String message,
    @RequestParam(name = "file") MultipartFile image,
      Model model,
      RedirectAttributes redirectAttributes,
      HttpSession session) throws IOException {
    log().info("[/:post]roomid:" + roomId + " message:" + message);

    String imgUrl = null;

    if (!image.isEmpty()) {
      String uploadDirectory = "uploads/chat/";
      String fileName = System.currentTimeMillis() + image.getOriginalFilename(); // 同じユーザーの画像がかぶってもいいようにタイムスタンプ
      Path savePath = Paths.get(uploadDirectory + fileName);
      Files.createDirectories(savePath.getParent());
      Files.write(savePath, image.getBytes());
      imgUrl = "/images/chatLog/" + fileName + "?v=" + System.currentTimeMillis();
    }

    UserData user = loginService.getLoginedUserInfo();

    if (roomService.ifUserExistInRoom(roomId, user.userId())) {
      chatService.saveChatLog(roomId, loginService.getLoginedUserId(), message, imgUrl);

    } else {
      model.addAttribute("errorMsg", "すでにルームに追加されているか、ユーザが存在しません");
      redirectAttributes.addFlashAttribute("errorMsg", "すでにルームに追加されているか、ユーザが存在しません");
      return "redirect:/?roomId=" + roomId;
    }

    return "redirect:/?roomId=" + roomId;
  }


  /**
   * メッセージ削除.
   *
   * @param roomId  部屋ID
   * @param chatLogId
   * @return メイン画面
   */
  @PostMapping("/main/deleteMessage")
  public String deleteMessage(
    @RequestParam(name = "chatLogId") int chatLogId,
    @RequestParam(name = "roomId") String roomId,
    @RequestParam(name = "imageUrl") String imageUrl,
      Model model)throws IOException {

    if (chatService.deleteMessage(chatLogId, imageUrl)) {
      return "redirect:/?roomId=" + roomId;
    }

    model.addAttribute("errorMsg", "メッセージの削除に失敗しました。");

    return "redirect:/?roomId=" + roomId;
  }

  /**
   * メッセージ編集.
   *
   * @param roomId    部屋ID
   * @param chatLogId
   * @param newMessage
   * @return メイン画面
   */
  @PostMapping("/main/updateMessage")
  public String updateMessage(
      @RequestParam(name = "chatLogId") int chatLogId,
      @RequestParam(name = "roomId") String roomId,
      @RequestParam(name = "newMessage") String newMessage,
        Model model) {

    if (chatService.updateMessage(chatLogId, newMessage)) {
      return "redirect:/?roomId=" + roomId;
    }

    model.addAttribute("errorMsg", "メッセージの編集に失敗しました。");
    return "redirect:/?roomId=" + roomId;
  }

  /**
   *メッセージに★つける
   *
   * @param roomId
   * @param chatLogId
   * @return リダイレクト先画面
   */
  @PostMapping("/main/starMessage")
  public String starMessage(@RequestParam(name = "chatLogId") int chatLogId,
    @RequestParam(name = "roomId") String roomId,
    @RequestParam(name = "fromStarred" ,defaultValue = "false") String fromStarred,
      Model model) {

        // 星をつけるDB処理
    if (chatService.starMessage(chatLogId)) {
      // スター付きメッセージのページから来た時
      if ("true".equals(fromStarred)) {
        // スター付き一覧に戻る
        return "redirect:/starred";
      }
      // 元のルームに戻る
      return "redirect:/?roomId=" + roomId;
    } else {
      model.addAttribute("errorMsg", "メッセージの★付けに失敗しました。");
      // 一覧画面からの操作時
      if ("true".equals(fromStarred)) {
        return "redirect:/starred";
      }
      // 元のルームへ
      return "redirect:/?roomId=" + roomId;
    }
  }

  /**
   * 星付きメッセージの一覧
   *
   * @param model
   * @return main
   */
  @GetMapping("/starred")
  public String starredMessage(Model model) {
    UserData user = loginService.getLoginedUserInfo();

    // 星付きメッセージのみ取得
    List<ChatLog> starredChatLogList = chatService.getStarredChatlogList(user.userId());

    // ダミールームの情報
    Room starRoom = new Room("STAR", "★付きメッセージ", "", false, false, 0);

    List<Room> roomList = chatService.getRoomList(user.userId());
    List<Room> directList = chatService.getDirectList(user.userId());

    // 未読数管理
    Map<String, Integer> unreadMap = new HashMap<>();
    try {
      unreadMap = chatService.getUnreadCountMap(user.userId());
    } catch (Exception e) {
      log().error("未読数の取得に失敗しました: " + e.getMessage());
    }

    List<Integer> chatLogIds = starredChatLogList.stream()
        .map(ChatLog::getChatLogId)
        .toList();
    Map<Integer, List<MessageReactionData>> reactionMap = reactionService.getReactionsForMessages(chatLogIds);

    model.addAttribute("user", user);
    model.addAttribute("room", starRoom);
    model.addAttribute("roomList", roomList);
    model.addAttribute("directList", directList);
    model.addAttribute("chatLogList", starredChatLogList);
    // 未知数map追加
    model.addAttribute("unreadMap", unreadMap);

    // 一覧画面ではリアクションはつかないがエラーが出るため空map使用
    model.addAttribute("reactionMap", reactionMap);

    return "main";
  }

  /**
  プロフィール画像のアップ
   * @param image
   */
  @PostMapping("/profile/upload")
  public String uploadProfileImg(
    @RequestParam(name = "image") MultipartFile image,
    HttpSession session) throws IOException {
    UserData user = loginService.getLoginedUserInfo();

    if (image.getSize() > 20 * 1024 * 1024) {
      return "redirect:/";
    }

    String uploadDirectory = "uploads/profile/";
    String fileName = user.userId() + ".png"; //ユーザーID由来で固定
    Path savePath = Paths.get(uploadDirectory + fileName);
    Files.createDirectories(savePath.getParent());
    Files.write(savePath, image.getBytes());
    String imageUrl = "/images/profile/" + fileName + "?v=" + System.currentTimeMillis();

    UserData newUser = new UserData(
        user.userId(),
        user.userName(),
        user.mailAddress(),
        user.password(),
        imageUrl,
        user.role(),
        user.failedLoginCount(),
        user.locked(),
        user.lastLogin());

    session.setAttribute("loginUser", newUser);
    userService.updateProfileImg(user.userId(), imageUrl);
    return "redirect:/";
  }
}
