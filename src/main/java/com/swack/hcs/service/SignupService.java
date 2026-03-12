package com.swack.hcs.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.swack.hcs.bean.UserData;
import com.swack.hcs.repository.RoomRepository;
import com.swack.hcs.repository.UserRepository;

@Transactional
@Service
public class SignupService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoomRepository roomRepository;

  public boolean signup(String username, String mailAddress, String password) {


    if (username == "" || mailAddress == "" || password == "") {
      return false;
    }
    int signupCheck = userRepository.signup(username, mailAddress, password);
    if (signupCheck == 1) {
      // 新規登録後everyone参加
      UserData user = userRepository.selectOne(mailAddress);
      roomRepository.addMemberToJoinroom(user.userId(), "R0000");

      return true;

    } else {
      return false;
    }
  }
}
