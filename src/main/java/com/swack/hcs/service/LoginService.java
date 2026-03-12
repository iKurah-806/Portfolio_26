package com.swack.hcs.service;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.swack.hcs.bean.UserData;

/**
 * ログイン管理の業務ロジッククラス.
 * ログイン関連の操作を提供します。
 *
 * @author 情報太郎
 */
@Transactional
@Service
public class LoginService {

  /** セッションに格納するユーザーデータのキー */
     private final String SESSION_USER_DATA_KEY = "loginUser";

  /** セッション情報 */
     @Autowired
     HttpSession session;

     @Autowired
     private UserService userService;

     /**
      * ログイン処理
      */
     public boolean login(String mailAddress, String password) {

          UserData userData;
          try {
               userData = userService.login(mailAddress, password);
          } catch (RuntimeException e) {
               throw e;
          }

          if (userData == null) {
               return false;
          }

          // ログイン成功時はセッションにユーザーデータを格納
          setLoginedUserInfo(userData);
          return true;
     }
     /**
        * ログアウト処理.
        */
     public void logout() {
          // セッション情報を破棄
          session.invalidate();
     }
     /**
        * ログイン中ユーザーのユーザーIDを取得.
        *
        * @return ログイン中ユーザーのユーザーID
        */
     public String getLoginedUserId() {
          UserData userData = (UserData) session.getAttribute(SESSION_USER_DATA_KEY);
          if (userData == null) {
          return "Unknown User(セッション格納無し)";
          }
          return userData.userId();
     }
     /**
        * ログイン中ユーザーのユーザーデータを取得.
        *
        * @return ログイン中ユーザーのユーザーデータ(未ログインの場合はnull)
        */
     public UserData getLoginedUserInfo() {
          UserData userData = (UserData) session.getAttribute(SESSION_USER_DATA_KEY);
          return userData;
     }
     /**
        * ログイン中ユーザーのユーザーデータを設定.
        *
        * @param userData ユーザーデータ
        */
     public void setLoginedUserInfo(UserData userData) {
          session.setAttribute(SESSION_USER_DATA_KEY, userData);
     }
     /**
        * ログインチェック.
        *
        * @return ログイン中の場合はtrue、未ログインの場合はfalse
        */
     public boolean isLogin() {
          UserData userData = (UserData) session.getAttribute(SESSION_USER_DATA_KEY);
          if (userData == null) {
               return false;
          }
          return true;
     }
}
