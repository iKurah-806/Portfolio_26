package com.swack.hcs.bean;

public class MessageReactionData {
  private int chatlogId; // メッセージID
  private String emoji; // 👍 / ❤️ / 😂 など
  private int count; // 何人からリアクションされたか

  public MessageReactionData(int chatlogId, String emoji, int count) {
    this.chatlogId = chatlogId;
    this.emoji = emoji;
    this.count = count;
  }

  // Getter
  public int getChatlogId() {
    return chatlogId;
  }

  public String getEmoji() {
    return emoji;
  }

  public int getCount() {
    return count;
  }

  // Setter（必要であれば）
  public void setChatlogId(int chatlogId) {
    this.chatlogId = chatlogId;
  }

  public void setEmoji(String emoji) {
    this.emoji = emoji;
  }

  public void setCount(int count) {
    this.count = count;
  }
}
