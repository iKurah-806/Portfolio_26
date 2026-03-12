-- 3.create-if-not-exists.sql

CREATE SEQUENCE IF NOT EXISTS userid_seq
    START 5
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9999
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS chatlogid_seq
    START 1
    INCREMENT 1
    CACHE 1;
