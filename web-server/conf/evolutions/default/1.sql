# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table cast_ballot (
  id                        bigint not null,
  ballotid                  varchar(255),
  hash                      varchar(255),
  constraint pk_cast_ballot primary key (id))
;

create table challenged_ballot (
  id                        bigint not null,
  ballotid                  varchar(255),
  hash                      varchar(255),
  precinct                  varchar(255),
  decrypted_ballot          varchar(8000),
  constraint pk_challenged_ballot primary key (id))
;

create sequence cast_ballot_seq;

create sequence challenged_ballot_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists cast_ballot;

drop table if exists challenged_ballot;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists cast_ballot_seq;

drop sequence if exists challenged_ballot_seq;

