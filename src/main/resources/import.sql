create table items
(
    id    bigint auto_increment,
    name  varchar(255),
    count varchar(255),
    primary key (id)
);

INSERT INTO items (name, count)
VALUES ('first', '3');
INSERT INTO items (name, count)
VALUES ('second', '5');
INSERT INTO items (name, count)
VALUES ('third', '2');
