From tutorial:
http://www.vogella.com/tutorials/JavaPersistenceAPI/article.html

and:
http://java-persistence-performance.blogspot.ch/2013/05/batch-writing-and-dynamic-vs.html


Create Postgresql Database
==========================

Assumes you have a user "danny" with password "root".

```
dave$ sudo su postgres
postgres$ psql
psql (9.5.9)
Type "help" for help.

postgres=# create database batch_test;
CREATE DATABASE
postgres=# \connect batch_test
You are now connected to database "batch_test" as user "postgres".
```

Copy and paste the following:

```
CREATE TABLE child_a (
    child_a_key  BIGSERIAL    NOT NULL  PRIMARY KEY,
    child_a_id   VARCHAR(255) NOT NULL,
    description  VARCHAR(255) NULL,

    CONSTRAINT child_a_child_a_id_check
        CHECK (char_length(child_a_id) > 0)
);
CREATE INDEX child_a__child_a_id ON child_a(child_a_id);
```
```
CREATE TABLE child_b (
    child_b_key  BIGSERIAL    NOT NULL  PRIMARY KEY,
    child_b_id   VARCHAR(255) NOT NULL,
    description  VARCHAR(255) NULL,

    CONSTRAINT child_b_child_b_id_check
        CHECK (char_length(child_b_id) > 0)
);
CREATE INDEX child_b__child_b_id ON child_b(child_b_id);
```
```
CREATE TABLE parent (
    parent_key   BIGSERIAL    NOT NULL  PRIMARY KEY,
    parent_id    VARCHAR(255) NOT NULL,
    child_a_key  BIGINT       NULL,
    child_b_key  BIGINT       NULL,
    description  VARCHAR(255) NULL,

    CONSTRAINT parent_parent_id_check
        CHECK (char_length(parent_id) > 0),

    CONSTRAINT fk_parent_child_a_key
        FOREIGN KEY (child_a_key)
        REFERENCES child_a (child_a_key),

    CONSTRAINT fk_parent_child_b_key -- *DTA DOES THIS NEED INDEXED OR IS IT INDEXED AS A FOREIGN KEY?
        FOREIGN KEY (child_b_key)
        REFERENCES child_b (child_b_key)
);
```
When you run `\dt` you should see:
```
batch_test=# \dt
          List of relations
 Schema |  Name   | Type  |  Owner   
--------+---------+-------+----------
 public | child_a | table | postgres
 public | child_b | table | postgres
 public | parent  | table | postgres
(3 rows)
```


    