package com.swack.hcs.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.swack.hcs.bean.MessageReactionData;
import com.swack.hcs.repository.ReactionRepository;

@Service
public class ReactionService {

  @Autowired
  ReactionRepository reactionRepository;

  public boolean toggle(int chatlogId, String userId, String emojiName) {
    return reactionRepository.toggleReaction(chatlogId, userId, emojiName);
  }

  /**
   * 複数メッセージのリアクション集計
   */
  public Map<Integer, List<MessageReactionData>> getReactionsForMessages(List<Integer> chatlogIds) {

    if (chatlogIds == null || chatlogIds.isEmpty()) {
        return Collections.emptyMap();
    }

    List<MessageReactionData> list = reactionRepository.findReactionsByChatlogIds(chatlogIds);

    // chatlogId をキーにしてグループ化
    return list.stream().collect(Collectors.groupingBy(MessageReactionData::getChatlogId));
}

}
