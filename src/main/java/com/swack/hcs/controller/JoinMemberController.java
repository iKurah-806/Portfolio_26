package com.swack.hcs.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swack.hcs.bean.Room;
import com.swack.hcs.service.JoinMemberService;
import com.swack.hcs.service.RoomService;


@Controller
public class JoinMemberController {

  @Autowired
  private JoinMemberService joinMemberService;

  @Autowired
  private RoomService roomService;

  @GetMapping("/joinmember")
  public String get(
    @RequestParam(name = "roomId", required = false) String roomId,
      Model model) {

    Room room = joinMemberService.getRoom(roomId);
    model.addAttribute("room", room);

    return "joinmember";
  }

  /**
   * ルーム参加（ダイレクトメッセージ）.
   * @param roomId
   * @param mailAddress
   * @param redirectAttributes
   * @param model
   * @return
   */
  @PostMapping("/joinmember/direct")
  public String joinToDirect(
    @RequestParam(name = "roomId", required = false) String roomId,
    @RequestParam(name = "mailAddress", required = false) String mailAddress,
      RedirectAttributes redirectAttributes,
      Model model) {

      if (joinMemberService.addMemberToDirect(mailAddress)) {
        String directRoomId = joinMemberService.getDirectRoom(mailAddress);
        return "redirect:/?roomId=" + directRoomId;
      } else {
        model.addAttribute("errorMsg", "あなたはルームに参加していません。");
        redirectAttributes.addFlashAttribute("errorMsg","あなたはルームに参加していません。");
        return "redirect:/?roomId=" + "R0000";
      }
  }


  /**
   * ルーム参加（ルーム）.
   * @param roomId
   * @param invitedMails
   * @param redirectAttributes
   * @param model
   * @return
   */
  @PostMapping("/joinmember/room")
  public String joinToRoom(
    @RequestParam(name = "roomId", required = false) String roomId,
    @RequestParam(name = "inviteMails", required = false, defaultValue = "") List<String> invitedMails,
      RedirectAttributes redirectAttributes,
      Model model) {

    if (!invitedMails.isEmpty()) {
      roomService.addInvitedListByID(roomId, invitedMails);
    } else {
      model.addAttribute("errorMsg", "すでにルームに追加されているか、ユーザが存在しません。");
      Room room = joinMemberService.getRoom(roomId);
      model.addAttribute("room", room);
      redirectAttributes.addFlashAttribute("errorMsg", "すでにルームに追加されているか、ユーザが存在しません。");
      return "redirect:/?roomId=" + roomId;
    }

    return "redirect:/?roomId=" + roomId;
  }
}
