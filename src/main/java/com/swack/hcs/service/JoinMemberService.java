package com.swack.hcs.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.swack.hcs.bean.Room;
import com.swack.hcs.bean.UserData;
import com.swack.hcs.repository.ChatRepository;
import com.swack.hcs.repository.RoomRepository;
import com.swack.hcs.repository.UserRepository;

@Service
public class JoinMemberService {

  @Autowired
  private LoginService loginService;

  @Autowired
  private ChatRepository chatRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoomRepository roomRepository;

  public Room getRoom(String roomId) {

    Room room = chatRepository.getRoom(roomId);

    return room;
  }

  public boolean addMember(String mailAddress, String roomId) {

    UserData user = userRepository.selectOne(mailAddress);
    if (user == null) {
      return false;
    }



    boolean doubleCheck = roomRepository.doubleCheckOnJoinroom(user.userId(), roomId);
    if (!doubleCheck) {
      return false;
    }
    int result = roomRepository.addMemberToJoinroom(user.userId(), roomId);

    if (result != 1) {
      return false;
    }

    return true;
  }

  public boolean addMemberToDirect(String mailAddress) {

    UserData user = userRepository.selectOne(mailAddress);
    if (user == null) {
      return false;
    }

    boolean result = roomRepository.addMemberToDirect(loginService.getLoginedUserId(), user.userId());

    return result;

  }

  public String getDirectRoom(String mailAddress) {
    UserData user = userRepository.selectOne(mailAddress);

    String roomId = roomRepository.getDirectRoomIdByUserName(loginService.getLoginedUserId(), user.userId());

    return roomId;
  }
}
