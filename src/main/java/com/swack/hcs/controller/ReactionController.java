
package com.swack.hcs.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.swack.hcs.service.ReactionService;

@Controller
public class ReactionController {

  @Autowired
  private ReactionService reactionService;

  @PostMapping("/reaction/toggle")
  public String toggleReaction(
    @RequestParam("chatlogId") int chatlogId,
    @RequestParam("emoji") String emoji,
    @RequestParam("userId") String userId,
    @RequestHeader(value = "Referer", required = false) String referer) {

    // サービスに渡してトグル処理
    reactionService.toggle(chatlogId, userId, emoji);

    // 元のページにリダイレクト
    return "redirect:" + (referer != null ? referer : "/");
  }
}
