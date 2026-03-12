# Web アプリケーション構築 2 実習ワークスペース

## VSCode に最初にインストールする 拡張機能

### 入れ方

1. 左の「拡張機能」ボタンを押す
1. 検索ボックスに「@recommended」を入れる
1. 「ワークスペースの推奨事項」に表示されている Extension をインストール

## PostgreSQL DB への接続

### PostgreSQL 拡張機能の活用

1. VSCode 左側のアイコンの中から「Database」を開く

2. 「Create Connection」を押す

3. 「Connect」内で以下の設定を入力し、下部の「＋ Connect」ボタンを押す

- Name : DB
- Host : 127.0.0.1 ※デフォルト
- Port : 5432 ※デフォルト
- Username : postgres ※デフォルト
- Password : postgres
- Database : postgres ※デフォルト

4. PostgreSQL の DB に接続されれば準備完了

- ※エラーになった場合は、PostgreSQL のインストール状況を確認する

## Controller と Service で実装するロジックの責任分界点について

【引用】[3.2. ドメイン層の実装](https://terasolunaorg.github.io/guideline/current/ja/ImplementationAtEachLayer/DomainLayer.html#service)

本ガイドラインでは、Controller と Service で実装するロジックは、以下のルールに則って実装することを推奨する。
![Image](https://github.com/user-attachments/assets/95880034-3ce5-43b0-b5e6-afd37396c1d2)
- クライアントからリクエストされたデータに対する単項目チェック、相関項目チェックは Controller 側(Bean Validation または Spring Validator)で行う。
- Service に渡すデータへの変換処理(Bean 変換、型変換、形式変換など)は、Service ではなく Controller 側で行う。
- ビジネスルールに関わる処理は Service で行う。業務データへのアクセスは、Repository または O/R Mapper に委譲する。
- Service から Controller に返却するデータ（クライアントへレスポンスするデータ）に対する値の変換処理(型変換、形式変換など)は、Service ではなく、Controller 側（View クラスなど）で行う。


SELECT roomid, (SELECT username FROM users where userid = substring(roomname, 8, 12))
FROM rooms
WHERE roomid IN(SELECT roomid FROM joinroom WHERE userid = 'U0001') AND directed = true;


CREATE SEQUENCE userid_seq START 5 INCREMENT 1 MINVALUE 1 MAXVALUE 9999 CACHE 1;

CREATE SEQUENCE roomid_seq START 20 INCREMENT 1 MINVALUE 1 MAXVALUE 9999 CACHE 1;


アカウントロック機能追加に伴う操作権限
ALTER TABLE USERS ADD COLUMN FAILED_ATTEMPTS INT DEFAULT 0;
ALTER TABLE USERS ADD COLUMN ACCOUNT_LOCKED CHAR(1) DEFAULT 'N';
ALTER TABLE USERS ADD COLUMN LOCK_TIME TIMESTAMP NULL;
ALTER TABLE USERS ADD COLUMN LAST_LOGIN_AT TIMESTAMP NULL;

１ ログイン失敗回数
２ ロックされているかどうか N or Y
３ ロックされて日時
４ 最後にログインした日時

★つけるよう
ALTER TABLE chatLog ADD star VARCHAR(10)



上記まで記載済み


ALTER TABLE chatLog
ADD imgpath VARCHAR(500) DEFAULT NULL;

未読メッセージ用
CREATE TABLE IF NOT EXISTS room_last_read (
    user_id VARCHAR(10) NOT NULL,
    room_id VARCHAR(10) NOT NULL,
    last_read_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, room_id)
);

インデックス作成
CREATE INDEX idx_room_last_read_user ON room_last_read(user_id);
CREATE INDEX idx_room_last_read_room ON room_last_read(room_id);

ALTER TABLE room_last_read
ADD CONSTRAINT uq_room_last_read UNIQUE (user_id, room_id);

上記まで記載済み
