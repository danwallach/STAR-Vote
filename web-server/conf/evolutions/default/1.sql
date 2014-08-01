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

create table supervisor_record (
  id                        bigint not null,
  record                    varchar(255),
  hash                      varchar(255),
  owner_id                  bigint,
  constraint pk_supervisor_record primary key (id))
;

create table user (
  username                  varchar(255) not null,
  name                      varchar(255),
  password                  varchar(255),
  constraint pk_user primary key (username))
;

create table voting_record (
  id                        bigint not null,
  precinct_id               varchar(255),
  is_conflicted             boolean,
  is_published              boolean,
  constraint pk_voting_record primary key (id))
;

create sequence cast_ballot_seq;

create sequence challenged_ballot_seq;

create sequence supervisor_record_seq;

create sequence user_seq;

create sequence voting_record_seq;

alter table supervisor_record add constraint fk_supervisor_record_owner_1 foreign key (owner_id) references voting_record (id) on delete restrict on update restrict;
create index ix_supervisor_record_owner_1 on supervisor_record (owner_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists cast_ballot;

drop table if exists challenged_ballot;

drop table if exists supervisor_record;

drop table if exists user;

drop table if exists voting_record;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists cast_ballot_seq;

drop sequence if exists challenged_ballot_seq;

drop sequence if exists supervisor_record_seq;

drop sequence if exists user_seq;

drop sequence if exists voting_record_seq;

