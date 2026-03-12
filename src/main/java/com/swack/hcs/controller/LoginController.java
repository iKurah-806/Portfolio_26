package com.swack.hcs.controller;

import com.swack.hcs.service.LoginService;
import com.swack.hcs.util.AppConstants;
import com.swack.hcs.util.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ログインコントローラ.
 */
@Controller
public class LoginController implements Loggable {

  @Autowired
  private LoginService loginService;

  /**
   * ログイン画面表示.
   *
   * @return ログイン画面
   */
  @GetMapping("/login")
  public String get() {
    return "login";
  }

  /**
   * ログイン.
   *
   * @param mailAddress メールアドレス
   * @param password    パスワード
   * @param model       モデル
   * @return チャット画面
   */
  @PostMapping("/login")
  public String login(@RequestParam(name = "mailAddress") String mailAddress,
    @RequestParam(name = "password") String password,
      Model model) {
    log().info("[login:post] mailAddress:" + mailAddress);

    // ログイン処理
    try {
      boolean result = loginService.login(mailAddress, password);

      if (!result) {
        model.addAttribute("mailAddress", mailAddress);
        model.addAttribute("errorMsg", AppConstants.MSG_ERR_LOGIN_PARAM_MISTAKE);
        return "login";
      }
      return "redirect:/";

    } catch (RuntimeException e) {
      log().warn("[login:post] ログイン例外発生: " + e.getMessage());
      model.addAttribute("mailAddress", mailAddress);
      model.addAttribute("errorMsg", e.getMessage());
      return "login";
    }
  }

  /**
   * ログアウト.
   *
   * @return ログイン画面
   */
  @GetMapping("/logout")
  public String logout() {
    String userId = loginService.getLoginedUserId();
    log().info("[logout:get] userId:" + userId);

    // ログインユーザ情報を破棄
    loginService.logout();

    return "redirect:/login";
  }
}
