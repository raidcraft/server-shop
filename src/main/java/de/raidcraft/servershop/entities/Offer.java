package de.raidcraft.servershop.entities;

import io.ebean.annotation.DbDefault;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Material;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
@Entity
@Table(name = "rcss_offers")
public class Offer extends BaseEntity {

    @ManyToOne
    private ServerShop shop;
    private String item;
    private double sellPrice = -1;
    private double buyPrice = -1;
    @DbDefault("-1")
    private int sellLimit = -1;
    @OneToMany(cascade = CascadeType.REMOVE)
    private List<ShopSign> signs = new ArrayList<>();

    public Offer(ServerShop shop, Material material) {
        this.shop = shop;
        this.item = material.getKey().toString();
    }

    public Offer buyPrice(double price) {

        if (buyPrice == price) return this;
        this.buyPrice = price;
        signs().forEach(ShopSign::updateLines);

        return this;
    }

    public Offer sellPrice(double price) {

        if (sellPrice == price) return this;
        this.sellPrice = price;
        signs().forEach(ShopSign::updateLines);

        return this;
    }

    public Material material() {

        return Material.matchMaterial(item);
    }

    public boolean isBuying() {

        return buyPrice >= 0;
    }

    public boolean isSelling() {

        return sellPrice >= 0;
    }

    public boolean hasLimit() {

        return sellLimit() > -1;
    }
}
