package com.swack.hcs.controller;
import com.swack.hcs.bean.Room;
import com.swack.hcs.bean.UserData;
import com.swack.hcs.service.ChatService;
import com.swack.hcs.service.LoginService;
import com.swack.hcs.service.RoomService;
import com.swack.hcs.util.Loggable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 部屋参加コントローラ.
 */
@Controller
@RequestMapping("/joinroom")
public class JoinRoomController implements Loggable {

  @Autowired
  private LoginService loginService;

  @Autowired
  private ChatService chatService;

  @Autowired
  private RoomService roomService;

  @GetMapping
  public String get(
    @RequestParam(name = "roomId", required = false) String roomId,
      Model model) {

    UserData user = loginService.getLoginedUserInfo();

    Room room = chatService.getRoom(roomId, user.userId());
    List<Room> roomList = chatService.getRoomList2(user.userId());

    model.addAttribute("user", user);
    model.addAttribute("room", room);
    model.addAttribute("roomList", roomList);

    return "joinroom";
  }

  /**
   * ルーム参加.
   * @param roomIdList
   * @param userId
   * @param redirectAttributes
   * @return
   */
  @PostMapping("/add")
  public String joinroom(
    @RequestParam(name = "roomId") List<String> roomIdList,
    @RequestParam(name = "userId") String userId,
      RedirectAttributes redirectAttributes) {

    boolean success;

    for (String roomId : roomIdList) {
      success = roomService.addMember(userId, roomId);
      if (!success) {
        redirectAttributes.addFlashAttribute("error", "ルームに参加できませんでした。");

        return "/joinroom";
      }
    }
    return "redirect:/";
  }
}
