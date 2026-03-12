package com.swack.hcs.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.swack.hcs.service.SignupService;
import com.swack.hcs.util.AppConstants;
import com.swack.hcs.util.Loggable;

/**
 * ユーザ登録コントローラ.
 */
@Controller
public class SignupController implements Loggable {


  @Autowired
  private SignupService signupService;



  @GetMapping("/signup")
  public String signup() {
    return "signup";
  }

  /**
   * ログイン.
   * @param username 氏名
   * @param mailAddress メールアドレス
   * @param password    パスワード
   * @param model       モデル
   * @return チャット画面
   */
  @PostMapping("/signup/post")
  public String signup(
    @RequestParam(name = "username") String username,
    @RequestParam(name = "mailAddress") String mailAddress,
    @RequestParam(name = "password") String password, Model model) {

    boolean result = signupService.signup(username, mailAddress, password);

    if (result == true) {
      return "redirect:/login";
    } else {
      model.addAttribute("errorMsg", AppConstants.MSG_ERR_SIGNUP_PARAM_MISTAKE);
      return "signup";
    }
  }
}
