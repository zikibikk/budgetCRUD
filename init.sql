create database budget;

create table BUDGET(
                      id serial not null,
                      PRIMARY KEY(id),
                      comment character varying NOT NULL,
                      change_sum int NOT NULL,
                      change_date bigint NOT NULL
);

create table BALANCE(
                       id serial not null,
                       PRIMARY KEY(id),
                       balance_sum int NOT NULL,
                       balance_date bigint NOT NULL
);