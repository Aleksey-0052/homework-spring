-- liquibase formatted sql

-- changeset asemenikhin:1
DROP TABLE IF EXISTS users;

CREATE TABLE users (
   id          BIGSERIAL     PRIMARY KEY,
   name        VARCHAR(255)  NOT NULL,
   email       VARCHAR(255)  NOT NULL
               CONSTRAINT uk_users_email UNIQUE,
   age         INT           NOT NULL,
   created_at  TIMESTAMP
);