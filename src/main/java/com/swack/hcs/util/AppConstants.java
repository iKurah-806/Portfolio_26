package com.swack.hcs.util;

/**
 * アプリケーション全体の定数定義クラス.
 */
public class AppConstants {

  // プライベートコンストラクタ(new防止)
  private AppConstants() {
    throw new IllegalStateException("Utility class");
  }

  /** 予想更新件数(ハードコーディング防止用) */
  public static final int EXPECTED_UPDATE_COUNT = 1;

  /** システム全体 */
  public static final String MSG_ERR_SYSTEM = "システムエラーが発生しました";

  /** DB */
  public static final String MSG_ERR_DB_CONNECT = "データベースへの接続時にエラーが発生しました";
  public static final String MSG_ERR_DB_PROCESS = "データベースの処理中にエラーが発生しました";
  public static final String MSG_ERR_DB_CLOSE = "データベースからの切断時にエラーが発生しました";

  /** ログイン */
  public static final String MSG_ERR_SESSION_TIMEOUT = "ログイン情報の取得に失敗しました。再度ログインしてください。";
  public static final String MSG_ERR_LOGIN_PARAM_MISTAKE = "メールアドレス、またはパスワードに誤りがあります。入力項目を確認し、再度ログインしてください。";
  public static final String
  MSG_ERR_SIGNUP_PARAM_MISTAKE = "ユーザ登録に失敗しました。入力項目を確認し、再度登録してください。";
  public static final String MSG_INFO_BIGIN = "ワークスペースに参加済みの方はログインしてください。初めての方はワークスペースに参加してください。";

  /** ルーム管理 */
  public static final String MSG_ERR_ROOM_ISREGISTERED = "このルーム名は登録済みです。入力項目を確認し、登録し直してください。";

  /** ユーザ管理 */
  public static final String MSG_ERR_USERS_ISREGISTERED = "このユーザは登録済みです。入力項目を確認し、登録し直してください。";
  public static final String MSG_ERR_USERS_PARAM_MISTAKE = "ユーザ情報に誤りがあります。入力項目を確認し、再度登録してください。";
  public static final String MSG_INFO_USERS_ENTRY_SUCCESS = "ワークスペースに参加しました。ログインしてください。";
}
