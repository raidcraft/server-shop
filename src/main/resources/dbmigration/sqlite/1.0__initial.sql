-- apply changes
create table rcss_offers (
  id                            varchar(40) not null,
  shop_id                       varchar(40),
  item                          varchar(255),
  sell_price                    double not null,
  buy_price                     double not null,
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcss_offers primary key (id),
  foreign key (shop_id) references rcss_shops (id) on delete restrict on update restrict
);

create table rcss_shops (
  id                            varchar(40) not null,
  identifier                    varchar(255),
  name                          varchar(255),
  enabled                       int default 0 not null,
  restricted                    int default 0 not null,
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint uq_rcss_shops_identifier unique (identifier),
  constraint pk_rcss_shops primary key (id)
);

create table rcss_players (
  id                            varchar(40) not null,
  name                          varchar(255),
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
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
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcss_shop_signs primary key (id),
  foreign key (shop_id) references rcss_shops (id) on delete restrict on update restrict,
  foreign key (offer_id) references rcss_offers (id) on delete restrict on update restrict
);

create table rcss_transactions (
  id                            varchar(40) not null,
  player_id                     varchar(40),
  shop_id                       varchar(40),
  offer_id                      varchar(40),
  amount                        integer not null,
  total_sell_price              double not null,
  total_buy_price               double not null,
  version                       integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_rcss_transactions primary key (id),
  foreign key (player_id) references rcss_players (id) on delete restrict on update restrict,
  foreign key (shop_id) references rcss_shops (id) on delete restrict on update restrict,
  foreign key (offer_id) references rcss_offers (id) on delete restrict on update restrict
);

