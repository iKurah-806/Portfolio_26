package com.swack.hcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Bootの起動クラス.
 */
@SpringBootApplication
public class SwackApplication {

  /**
   * Spring Bootの起動.
   *
   * @param args 起動引数
   */
  public static void main(String[] args) {
    SpringApplication.run(SwackApplication.class, args);
  }
}
