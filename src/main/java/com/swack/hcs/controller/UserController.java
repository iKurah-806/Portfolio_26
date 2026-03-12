package com.swack.hcs.controller;

import com.swack.hcs.bean.UserData;
import com.swack.hcs.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ユーザー管理コントローラ
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * 全ユーザー取得（管理者用）
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> getAllUsers(HttpSession session) {

    UserData loginUser = (UserData) session.getAttribute("loginUser");

    if (loginUser == null || !loginUser.isAdmin()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    String currentUserId = loginUser.userId();
    List<UserData> users = userService.findAll(currentUserId);

    Map<String, Object> response = new HashMap<>();
    response.put("users", users);
    response.put("currentUserId", loginUser.userId());

    return ResponseEntity.ok(response);
  }

  /** 全ユーザー名取得（モーダル用） */
  @GetMapping("/usernames")
  public ResponseEntity<List<String>> getUsernames() {

    List<String> usernames = userService.getUsernames();

    return ResponseEntity.ok(usernames);
  }

  /** ユーザー削除（管理者専用） */
  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String id, HttpSession session) {

    Map<String, String> response = new HashMap<>();
    UserData loginUser = (UserData) session.getAttribute("loginUser");

    if (loginUser == null) {
      response.put("message", "ログイン情報がありません。");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    if (id.equals(loginUser.userId())) {
      response.put("message", "自分自身は削除できません。");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    try {
      userService.deleteUser(id, loginUser.userId());
      response.put("message", "削除成功");
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

    } catch (Exception e) {
      response.put("message", "削除失敗");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @DeleteMapping("/bulk-delete")
  public ResponseEntity<Map<String, String>> bulkDeleteUsers(
    @RequestBody Map<String, List<String>> requestBody,
      HttpSession session) {

    Map<String, String> response = new HashMap<>();
    UserData loginUser = (UserData) session.getAttribute("loginUser");

    if (loginUser == null) {
      response.put("message", "ログイン情報がありません");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    if (!loginUser.isAdmin()) {
      response.put("message", "権限がありません（管理者のみ）");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    List<String> idsToDelete = requestBody.get("ids");
    if (idsToDelete == null || idsToDelete.isEmpty()) {
      response.put("message", "削除対象が選択されていません");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    String currentUserId = loginUser.userId();
    List<String> filteredIds = idsToDelete.stream()
        .filter(id -> !id.equals(currentUserId))
        .collect(Collectors.toList());

    if (filteredIds.isEmpty()) {
      if (idsToDelete.size() > 0) {
        response.put("message", "自分自身は一括削除できません");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }
      response.put("message", "削除対象がいません");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 削除処理の実行
    try {
      userService.bulkDeleteUsers(filteredIds, currentUserId);
      response.put("message", "選択されたユーザーを退会させました");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("message", "一括削除中にエラーが発生しました: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** ログイン */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestParam String mailAddress,
    @RequestParam String password,
      HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    try {
      UserData user = userService.login(mailAddress, password);
      if (user != null) {
        session.setAttribute("loginUser", user);
        response.put("user", user);
        return ResponseEntity.ok(response);
      } else {
        response.put("message", "メールアドレスまたはパスワードが違います");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

    } catch (RuntimeException e) {
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
  }

  /** 管理者専用：アカウントアンロック */
  @PostMapping("/unlock")
  public ResponseEntity<Map<String, String>> unlockUser(
    @RequestParam String targetUserId,
      HttpSession session) {

    Map<String, String> response = new HashMap<>();
    UserData loginUser = (UserData) session.getAttribute("loginUser");

    if (loginUser == null) {
      response.put("message", "ログイン情報がありません");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    try {
      userService.unlockUser(targetUserId, loginUser.userId());
      response.put("message", "アカウントのロックを解除しました");
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
  }

  /** パスワード変更 */
  @PostMapping("/change-password")
  public ResponseEntity<Map<String, String>> changePassword(
    @RequestBody Map<String, String> request,
      HttpSession session) {

    Map<String, String> response = new HashMap<>();
    UserData loginUser = (UserData) session.getAttribute("loginUser");

    if (loginUser == null) {
      response.put("message", "ログイン情報がありません");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    String oldPassword = request.get("oldPassword");
    String newPassword = request.get("newPassword");

    try {
      boolean success = userService.changePassword(loginUser.userId(), oldPassword, newPassword);
      if (success) {
        response.put("message", "パスワードを変更しました");
        return ResponseEntity.ok(response);
      } else {
        response.put("message", "現在のパスワードが正しくありません");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

    } catch (RuntimeException e) {
      response.put("message", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }


  /** 権限変更 (管理者専用) */
  @PostMapping("/update-role")
  public ResponseEntity<Map<String, String>> updateRole(
    @RequestBody Map<String, String> payload,
      HttpSession session) {

    Map<String, String> response = new HashMap<>();
    UserData loginUser = (UserData) session.getAttribute("loginUser");

    // ログイン・管理者チェック
    if (loginUser == null) {
      response.put("message", "ログイン情報がありません");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    if (!loginUser.isAdmin()) {
      response.put("message", "権限がありません");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    String targetUserId = payload.get("userId");
    String newRole = payload.get("role");

    try {
      //  Serviceを呼んで更新
      userService.updateRole(targetUserId, newRole);
      response.put("message", "権限を更新しました");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("message", "更新失敗: " + e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }
}
