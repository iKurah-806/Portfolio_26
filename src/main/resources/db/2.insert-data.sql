-- 管理者・ユーザー初期データ
INSERT INTO users (userid, username, mailaddress, password, role)
VALUES
('U0001','情報太郎','taro@swack.com','$2a$10$upDb5x4NvBUgXjRLezpRpuJZKfrCiPufoPAHZjZzBRYE6QYZo3cwO','ADMIN')
ON CONFLICT (userid) DO NOTHING;

INSERT INTO users (userid, username, mailaddress, password, role)
VALUES
('U0002','情報花子','hanako@swack.com','$2a$10$TgNLilzu3F/npKlXpNFjuuWgfU2NzstMGhKItHgN6CT/y5Y2UXf..','USER')
ON CONFLICT (userid) DO NOTHING;

INSERT INTO users (userid, username, mailaddress, password, role)
VALUES
('U0003','情報三郎','saburo@swack.com','$2a$10$1ncBUPjbXA1dPqKEJuZxyuUtE6udM6VkkdhhqGSbBHMmD2uUMN6Ve','USER')
ON CONFLICT (userid) DO NOTHING;

INSERT INTO users (userid, username, mailaddress, password, role)
VALUES
('U0004','情報四郎','shiro@swack.com','$2a$10$vI5OgbsL3e0f7qShxgbl2.c1pCrryKiNdnxKKo/r3zBz2FhUwc4ju','USER')
ON CONFLICT (userid) DO NOTHING;

-- 部屋データ
INSERT INTO rooms (roomid, roomname, createduserid, directed, privated)
VALUES
('R0000','everyone','U0000',FALSE,FALSE),
('R0001','random','U0000',FALSE,FALSE),
('R0002','連絡板','U0001',FALSE,TRUE),
('R0003','PU0001,U0002','U0000',TRUE,TRUE),
('R0004','PU0001,U0003','U0000',TRUE,TRUE),
('R0005','PU0002,U0003','U0000',TRUE,TRUE)
ON CONFLICT (roomid) DO NOTHING;

-- joinroom データ
INSERT INTO joinroom (roomid, userid)
VALUES
('R0000','U0001'),
('R0000','U0002'),
('R0000','U0003'),
('R0000','U0004'),
('R0001','U0001'),
('R0001','U0002'),
('R0002','U0001'),
('R0003','U0001'),
('R0003','U0002'),
('R0004','U0001'),
('R0004','U0003'),
('R0005','U0002'),
('R0005','U0003')
ON CONFLICT (roomid, userid) DO NOTHING;

-- chatlog データ
-- INSERT INTO chatlog (chatlogid, roomid, userid, message, created_at)
-- VALUES
-- (NEXTVAL('chatlogid_seq'),'R0000','U0001','Swackへようこそ！',CURRENT_TIMESTAMP),
-- (NEXTVAL('chatlogid_seq'),'R0000','U0002','こんにちは。よろしくお願いします。',CURRENT_TIMESTAMP),
-- (NEXTVAL('chatlogid_seq'),'R0001','U0001','雑談をはじめましょう！',CURRENT_TIMESTAMP),
-- (NEXTVAL('chatlogid_seq'),'R0002','U0001','連絡用の部屋です。',CURRENT_TIMESTAMP)
-- ON CONFLICT (chatlogid) DO NOTHING;


INSERT INTO reactions (emojiname)
VALUES
    ('thumbsup'),
    ('heart'),
    ('smile')
ON CONFLICT (emojiname) DO NOTHING;
