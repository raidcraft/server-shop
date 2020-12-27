package de.raidcraft.servershop.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.OfflinePlayer;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Accessors(fluent = true)
@Entity
@Table(name = "rcss_transactions")
public class Transaction extends BaseEntity {

    public static Transaction create(OfflinePlayer player, Offer offer, int amount) {

        return new Transaction(ShopPlayer.getOrCreate(player), offer, amount);
    }

    @ManyToOne
    private ShopPlayer player;
    @ManyToOne
    private ServerShop shop;
    @ManyToOne
    private Offer offer;
    private int amount;
    private double totalSellPrice;
    private double totalBuyPrice;

    Transaction(ShopPlayer player, Offer offer, int amount) {
        this.player = player;
        this.offer = offer;
        this.amount = amount;
    }

    @Value
    public static class Result {

        Transaction transaction;
        String error;

        public Result(Transaction transaction) {
            this.transaction = transaction;
            this.error = null;
        }

        public Result(String error) {
            this.transaction = null;
            this.error = error;
        }

        public boolean success() {

            return transaction != null;
        }

        public boolean failure() {

            return !success();
        }
    }
}
