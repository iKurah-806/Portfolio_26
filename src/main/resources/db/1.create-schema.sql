-- シーケンス作成
CREATE SEQUENCE IF NOT EXISTS userid_seq
    START 5
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9999
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS roomid_seq
    START 20
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9999
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS chatlogid_seq
    START 1
    INCREMENT 1;

-- テーブル作成
CREATE TABLE IF NOT EXISTS users (
    userid CHAR(5) PRIMARY KEY,
    username VARCHAR(40) NOT NULL,
    mailaddress VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(300) NOT NULL,
    userimgpath VARCHAR(500) DEFAULT '',
    role VARCHAR(20) DEFAULT 'USER',
    enabled CHAR(1) DEFAULT 'Y' NOT NULL,
    failed_attempts INT DEFAULT 0,
    account_locked CHAR(1) DEFAULT 'N',
    lock_time TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS rooms (
    roomid VARCHAR(5) PRIMARY KEY,
    roomname VARCHAR(50) UNIQUE NOT NULL,
    createduserid CHAR(5) NOT NULL,
    directed BOOLEAN DEFAULT FALSE,
    privated BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS joinroom (
    roomid VARCHAR(5),
    userid CHAR(5),
    PRIMARY KEY (roomid, userid)
);

CREATE TABLE IF NOT EXISTS chatlog (
    chatlogid INTEGER PRIMARY KEY,
    roomid VARCHAR(5) NOT NULL,
    userid CHAR(5) NOT NULL,
    message VARCHAR(500),
    created_at TIMESTAMP,
    star VARCHAR(10),
    FOREIGN KEY (roomid) REFERENCES rooms (roomid),
    FOREIGN KEY (userid) REFERENCES users (userid),
    imgpath VARCHAR(500) DEFAULT NULL
);


CREATE TABLE IF NOT EXISTS reactions  (
    reactionid SERIAL PRIMARY KEY,
    emojiname TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS message_reactions (
    chatlogid INTEGER NOT NULL REFERENCES chatlog(chatlogid),
    userid CHAR(5) NOT NULL REFERENCES users(userid),
    reactionid INT NOT NULL REFERENCES reactions(reactionid),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chatlogid, userid, reactionid)
);


-- テーブル作成（存在しなければ）
CREATE TABLE IF NOT EXISTS room_last_read (
    user_id VARCHAR(10) NOT NULL,
    room_id VARCHAR(10) NOT NULL,
    last_read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, room_id),
    CONSTRAINT uq_room_last_read UNIQUE (user_id, room_id)
);

-- インデックス作成（存在しなければ）
CREATE INDEX IF NOT EXISTS idx_room_last_read_user ON room_last_read(user_id);
CREATE INDEX IF NOT EXISTS idx_room_last_read_room ON room_last_read(room_id);
