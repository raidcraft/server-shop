-- apply changes
create table rcss_offers (
  id                            varchar(40) not null,
  shop_id                       varchar(40),
  item                          varchar(255),
  sell_price                    double not null,
  buy_price                     double not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcss_offers primary key (id)
);

create table rcss_shops (
  id                            varchar(40) not null,
  identifier                    varchar(255),
  name                          varchar(255),
  enabled                       tinyint(1) default 0 not null,
  restricted                    tinyint(1) default 0 not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint uq_rcss_shops_identifier unique (identifier),
  constraint pk_rcss_shops primary key (id)
);

create table rcss_players (
  id                            varchar(40) not null,
  name                          varchar(255),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcss_players primary key (id)
);

create table rcss_shop_signs (
  id                            varchar(40) not null,
  shop_id                       varchar(40),
  offer_id                      varchar(40),
  x                             integer not null,
  y                             integer not null,
  z                             integer not null,
  world_id                      varchar(40),
  world                         varchar(255),
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcss_shop_signs primary key (id)
);

create table rcss_transactions (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  shop_id                       varchar(40),
  offer_id                      varchar(40),
  amount                        integer not null,
  total_sell_price              double not null,
  total_buy_price               double not null,
  version                       bigint not null,
  when_created                  datetime(6) not null,
  when_modified                 datetime(6) not null,
  constraint pk_rcss_transactions primary key (id)
);

create index ix_rcss_offers_shop_id on rcss_offers (shop_id);
alter table rcss_offers add constraint fk_rcss_offers_shop_id foreign key (shop_id) references rcss_shops (id) on delete restrict on update restrict;

create index ix_rcss_shop_signs_shop_id on rcss_shop_signs (shop_id);
alter table rcss_shop_signs add constraint fk_rcss_shop_signs_shop_id foreign key (shop_id) references rcss_shops (id) on delete restrict on update restrict;

create index ix_rcss_shop_signs_offer_id on rcss_shop_signs (offer_id);
alter table rcss_shop_signs add constraint fk_rcss_shop_signs_offer_id foreign key (offer_id) references rcss_offers (id) on delete restrict on update restrict;

create index ix_rcss_transactions_player_id on rcss_transactions (player_id);
alter table rcss_transactions add constraint fk_rcss_transactions_player_id foreign key (player_id) references rcss_players (id) on delete restrict on update restrict;

create index ix_rcss_transactions_shop_id on rcss_transactions (shop_id);
alter table rcss_transactions add constraint fk_rcss_transactions_shop_id foreign key (shop_id) references rcss_shops (id) on delete restrict on update restrict;

create index ix_rcss_transactions_offer_id on rcss_transactions (offer_id);
alter table rcss_transactions add constraint fk_rcss_transactions_offer_id foreign key (offer_id) references rcss_offers (id) on delete restrict on update restrict;

