package com.swack.hcs.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import com.swack.hcs.bean.Room;
import com.swack.hcs.bean.UserData;
import com.swack.hcs.service.ChatServiceDummy;
import com.swack.hcs.service.LoginService;
import com.swack.hcs.service.RoomService;
import com.swack.hcs.service.UserService;
import com.swack.hcs.util.AppConstants;
import com.swack.hcs.util.Loggable;

/**
 * 部屋作成コントローラ.
 * 本機能は管理者のみが利用可能です。
 */
@Controller
public class CreateRoomController implements Loggable {

  @Autowired
  private ChatServiceDummy chatService;

  @Autowired
  private LoginService loginService;

  @Autowired
  private RoomService roomService;

  @Autowired
  private UserService userService;

  /**
   * メイン画面表示.
   *
   * @param roomId 部屋ID
   * @param model  モデル
   * @return メイン画面
   */

  @GetMapping("/createroom")
  public String get(
    @RequestParam(name = "roomId", required = false) String roomId,
      Model model) {
    log().info("[/:get]roomid:" + roomId);

    if (roomId == null) {
      // 初期ルームをeveryoneにする
      roomId = "R0000";
    }

    UserData user = loginService.getLoginedUserInfo();

    Room room = chatService.getRoom(roomId, user.userId());
    List<Room> roomList = chatService.getRoomList(user.userId());
    List<Room> directList = chatService.getDirectList(user.userId());
    List<UserData> userList = userService.selectAll(user.userId());

    model.addAttribute("user", user);
    model.addAttribute("room", room);
    model.addAttribute("roomList", roomList);
    model.addAttribute("userList", userList);
    model.addAttribute("directList", directList);

    return "createroom";
  }

  /**
   * ルーm作成
   *
   * @param roomname  モデル
   * @return メイン画面
  */
  @PostMapping("/createroom")
  public String roomCreate(
    @RequestParam(name = "roomName", required = false) String roomname,
    @RequestParam(name = "privated", required = false) String privated,
    @RequestParam(name = "inviteMails", required = false) List<String> invitedMails,
      Model model) {

    @SuppressWarnings("null")
    String encoded = UriUtils.encode(roomname, StandardCharsets.UTF_8);

    // ルーム名が未入力
    if (roomname == null || roomname.trim().isEmpty()) {
      model.addAttribute("errorMsg", AppConstants.MSG_ERR_SYSTEM);
      return "/createroom";
    }

    // invitedMails が null の場合は空リストに変換
    if (invitedMails == null) {
      invitedMails = new ArrayList<>();
    }

    boolean privateChk = roomService.privateCheck(privated);
    UserData user = loginService.getLoginedUserInfo();

    model.addAttribute("user", user);
    boolean isAdmin = userService.isAdmin(user.userId());

    // プライベートルーム作成時に管理者でない場合はエラー
    if (privateChk && !isAdmin) {
      model.addAttribute("errorMsg", "管理者権限が必要です。");
      return "/createroom";
    }

    // ルーム名が重複している場合
    if (!roomService.RoomDoubleCheck(roomname)) {
      model.addAttribute("errorMsg", "既にルームが存在します。");
      return "/createroom";
    }

    // 管理者かつプライベートルーム
    if (privateChk && isAdmin) {
      roomService.createRoom(roomname, user.userId(), privateChk);
      if (!invitedMails.isEmpty()) {
        roomService.addInvitedList(roomname, invitedMails);
      }
      return "redirect:/?roomname=" + encoded;
    }

    // パブリックルーム
    if (!privateChk) {
      roomService.createRoom(roomname, user.userId(), privateChk);
      if (!invitedMails.isEmpty()) {
        roomService.addInvitedList(roomname, invitedMails);
      }
      return "redirect:/?roomname=" + encoded;
    }

    // それ以外
    model.addAttribute("errorMsg", "深刻なエラー");
    return "/createroom";
  }
}
