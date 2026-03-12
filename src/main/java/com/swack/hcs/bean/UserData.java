package com.swack.hcs.bean;

import java.sql.Timestamp;

/**
 * ユーザ情報を管理するBean.
 * ユーザーの基本情報、権限、プロフィール画像などを保持します。
 */
public record UserData(
    /**
     * ユーザID.
     */
    String userId,
    /**
     * ユーザ名.
     */
    String userName,
    /**
     * メールアドレス.
     */
    String mailAddress,
    /**
     * パスワード.
     */
    String password,
    /**
     * ユーザ画像パス.
     */
    String userImgPath,
    /**
     * 権限.
     */
    String role,
    /**
     * ログイン失敗回数.
     */
    int failedLoginCount,
    /**
     * アカウントロック状態 ('Y' or 'N').
     */
    String locked,
    /**
     * 最終ログイン日時.
     */
    Timestamp lastLogin) {

  /** 管理者権限判定用文字列 */
  private static final String ROLE_ADMIN_KEY = "ADMIN";

  /**
   * 管理者権限を持っているかチェック.
   *
   * @return 管理者権限を持っている場合はtrue、持っていない場合はfalse
   */
  public boolean isAdmin() {
    return ROLE_ADMIN_KEY.equalsIgnoreCase(this.role());
  }

  /**
   * アカウントがロックされているか判定.
   *
   * @return ロック中ならtrue
   */
  public boolean isLocked() {
    return "Y".equalsIgnoreCase(this.locked);
  }

  /**
   * ログイン失敗回数が上限に達しているか判定.
   *
   * @param maxFailed 最大失敗回数
   * @return 上限に達していればtrue
   */
  public boolean isFailedMax(int maxFailed) {
    return this.failedLoginCount >= maxFailed;
  }
}
