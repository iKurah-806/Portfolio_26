package com.swack.hcs.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swack.hcs.bean.UserData;
import com.swack.hcs.repository.RoomRepository;
import com.swack.hcs.repository.UserRepository;

@Transactional
@Service
public class RoomService {

  @Autowired
  private RoomRepository roomRepository;

  @Autowired
  private UserRepository userRepository;

  public boolean RoomDoubleCheck(String roomName) {
    return roomRepository.RoomDoubleCheck(roomName);
  }

  public void createRoom(String roomName, String userId, boolean privated) {
    roomRepository.createRoom(roomName, userId, privated);
  }

  public boolean addMember(String userId, String roomId) {
    int row = roomRepository.addMemberToJoinroom(userId, roomId);
    return row > 0;
  }

  public boolean privateCheck(String privated) {

    if (privated != null) {
      return true;
    } else {
      return false;
    }
  }

  public boolean addInvitedList(String roomName, List<String> invitedMails) {

    String roomId = roomRepository.getRoomIdByName(roomName);

    for (String mailAddress : invitedMails) {
      UserData user = userRepository.selectOne(mailAddress);
      roomRepository.addMemberToJoinroom(user.userId(), roomId);
    }
    return true;
  }

  public List<UserData> findNotInRoom(String roomId) {
    return roomRepository.findNotInRoom(roomId);
  }

  public boolean addInvitedListByID(String roomId, List<String> invitedMails) {

    for (String mailAddress : invitedMails) {
      UserData user = userRepository.selectOne(mailAddress);
      roomRepository.addMemberToJoinroom(user.userId(), roomId);
    }
    return true;
  }



  public List<UserData> selectUsersWithoutDirect(String userId) {
    return roomRepository.selectUsersWithoutDirect(userId);
  }

  public boolean ifUserExistInRoom(String roomId, String userId) {
    return roomRepository.ifUserExistInRoom(roomId, userId);
  }

  public List<UserData> selectUserInRoom(String roomId) {
    return roomRepository.selectUsersInRoom(roomId);
  }

  public String getRoomIdByName(String roomName) {
    return roomRepository.getRoomIdByName(roomName);
  }


}
