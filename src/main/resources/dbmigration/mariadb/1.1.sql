-- apply changes
alter table rcss_offers add column sell_limit integer default -1 not null;

