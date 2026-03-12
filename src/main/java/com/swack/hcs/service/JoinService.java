package com.swack.hcs.service;

import com.swack.hcs.bean.Room;
import com.swack.hcs.repository.ChatRepository;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class JoinService {
  @Autowired
  private ChatRepository chatRepository;

  public Room getRoom(String roomId, String userId) {
    Room room = chatRepository.getRoom(roomId);

    if (room.directed()) {
      // ダイレクト用の追加部屋情報を取得
      room = chatRepository.getDirect(room, userId);
    }
    return room;
  }

  public ArrayList<Room> getRoomList(String userId) {
    return chatRepository.getRoomList(userId);
  }
}
