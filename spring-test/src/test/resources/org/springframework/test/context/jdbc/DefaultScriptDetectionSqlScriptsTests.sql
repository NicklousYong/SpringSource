DROP TABLE user IF EXISTS;

CREATE TABLE user
(
    name VARCHAR(20) NOT NULL,
    PRIMARY KEY (name)
);

INSERT INTO user
VALUES ('Dilbert');
INSERT INTO user
VALUES ('Dogbert');
