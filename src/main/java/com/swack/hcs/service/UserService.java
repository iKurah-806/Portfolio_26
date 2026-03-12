package com.swack.hcs.service;

import com.swack.hcs.bean.UserData;
import com.swack.hcs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
public class UserService {

  private static final int INACTIVITY_DAYS_LIMIT = 21; // 3週間

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  /** ログイン処理 */
  @Transactional(noRollbackFor = RuntimeException.class)
  public UserData login(String mailAddress, String password) {

    UserData user = userRepository.selectOne(mailAddress);
    if (user == null)
      return null;

    // 1. ロック中チェック
    if ("Y".equalsIgnoreCase(user.locked())) {
      throw new RuntimeException("アカウントがロックされています。");
    }

    // 2. 未ログイン期間によるロック
    Timestamp lastLoginTs = user.lastLogin();

    if (lastLoginTs != null) {
        LocalDateTime lastLoginAt = lastLoginTs.toLocalDateTime();
        if (lastLoginAt.plusDays(INACTIVITY_DAYS_LIMIT).isBefore(LocalDateTime.now())) {
            userRepository.lockUser(mailAddress);  // ← DBを直接更新
            throw new RuntimeException("3週間ログインがなかったためアカウントがロックされました。");
        }
    }

    // 3. 認証
    if (!passwordEncoder.matches(password, user.password())) {
      increaseFailedAttempts(mailAddress);
      return null;
    }

    // 4. ログイン成功時
    userRepository.resetFailedLogin(mailAddress);
    userRepository.updateLastLogin(mailAddress);

    return user;
  }

  public List<UserData> selectAll(String loginedUserId) {
    return userRepository.findAll(loginedUserId);
  }

  /** 失敗回数増加＆必要ならロック */
  private void increaseFailedAttempts(String mailAddress) {

    userRepository.incrementFailedLogin(mailAddress);
  }


  /** 管理者によるアカウントロック解除 */
  public void unlockUser(String targetUserId, String currentUserId) {
    if (!userRepository.isAdmin(currentUserId)) {
      throw new RuntimeException("権限がありません（管理者のみ）");
    }
    userRepository.unlockUserById(targetUserId);
  }

  /** ユーザー削除（管理者のみ） */
  public void deleteUser(String targetUserId, String currentUserId) {
    if (!userRepository.isAdmin(currentUserId)) {
      throw new RuntimeException("権限がありません（管理者のみ）");
    }

    if (targetUserId.equals(currentUserId)) {
      throw new RuntimeException("自分自身は削除できません");
    }
    userRepository.deleteUser(targetUserId, currentUserId);
  }

  public void bulkDeleteUsers(List<String> userIds, String currentUserId) {
    // 1. 管理者チェック
    if (!userRepository.isAdmin(currentUserId)) {
      throw new RuntimeException("権限がありません（管理者のみ）");
    }
    userRepository.bulkDeleteUsers(userIds, currentUserId);
  }

  /** 全ユーザー取得 */
  public List<UserData> findAll(String currentUserId) {
    return userRepository.findAll(currentUserId);
  }

  /** ユーザー名一覧取得（モーダル用） */
  public List<String> getUsernames() {
    return userRepository.getUsernames();
  }

  /** パスワード変更 */
  public boolean changePassword(String userId, String oldPassword, String newPassword) {
    UserData user = userRepository.selectOneByUserId(userId); 
    if (user == null) {
      throw new RuntimeException("ユーザーが存在しません。");
    }

    // パスワード比較はハッシュに対応
    if (!passwordEncoder.matches(oldPassword, user.password())) {
      throw new RuntimeException("旧パスワードが間違っています。");
    }


    String hashedPassword = passwordEncoder.encode(newPassword);
    userRepository.updatePasswordById(userId, hashedPassword);
    return true;
  }

  /** ユーザーIDから管理者判定 */
  public boolean isAdmin(String userId) {
    return userRepository.isAdmin(userId);
  }

  // プロフィール画像あっぷでーと
  public void updateProfileImg(String userId, String imagePath) {
    userRepository.updateProfileImg(userId, imagePath);
  }

  // 権限変更
  public void updateRole(String userId, String role) {
    userRepository.updateRole(userId, role);
  }
}
