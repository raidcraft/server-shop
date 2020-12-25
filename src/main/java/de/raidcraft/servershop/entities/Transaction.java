package de.raidcraft.servershop.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Accessors(fluent = true)
@Entity
@Table(name = "rcss_transactions")
public class Transaction extends BaseEntity {

    @ManyToOne
    private ShopPlayer player;
    @ManyToOne
    private ServerShop shop;
    @ManyToOne
    private Offer offer;
    private int amount;
    private double totalSellPrice;
    private double totalBuyPrice;
}
