package com.swack.hcs.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

  /** パスワードのエンコードに使用 */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Override
  public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/images/profile/**")
        .addResourceLocations("file:uploads/profile/") // 外部フォルダ
        .setCacheControl(CacheControl.noCache()); // キャッシュ無効

    registry.addResourceHandler("/images/chatLog/**")
        .addResourceLocations("file:uploads/chat/")
        .setCacheControl(CacheControl.noCache());
  }


}
