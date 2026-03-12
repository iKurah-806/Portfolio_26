package com.swack.hcs.util;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * アプリケーション全体で発生する例外を処理するクラス.
 * 特定のタイプの例外が発生した場合のグローバルな処理を定義します。
 */
@ControllerAdvice
public class GlobalExceptionHandler implements Loggable {

  /**
   * IncorrectResultSizeDataAccessExceptionが発生した場合のハンドラー.
   * データベース更新時のエラーを処理し、ログに記録します。
   *
   * @param ex 発生した例外
   * @return ログアウト処理へのリダイレクト
   */
  @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
  public String handleIncorrectResultSizeDataAccessException(
      IncorrectResultSizeDataAccessException ex) {
    log().error("DB更新時エラー発生", ex);

    // 強制ログアウト
    return "redirect:/logout";
  }
}
